package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.common.util.SqlUtils;
import com.teamcollab.dto.task.*;
import com.teamcollab.entity.Task;
import com.teamcollab.entity.TaskList;
import com.teamcollab.mapper.TaskListMapper;
import com.teamcollab.mapper.TaskMapper;
import com.teamcollab.service.NotificationService;
import com.teamcollab.service.ProjectService;
import com.teamcollab.service.TaskService;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 * <p>
 * 提供看板中任务卡片的创建、查询、更新、删除、移动和排序功能。
 * 任务是看板中的基本卡片单元，任意团队成员均可创建和编辑任务。
 * </p>
 *
 * <p><b>SQL 注入防护：</b></p>
 * 在 getTasksByListId 方法中，sortBy 参数通过 SqlUtils.validateSortField
 * 进行白名单校验，仅允许 position、priority、due_date、created_at 等字段，
 * 防止恶意排序字段注入 SQL。
 *
 * <p><b>通知机制：</b></p>
 * <ul>
 *     <li>创建任务时如果指定了负责人，通知负责人</li>
 *     <li>更新任务时如果负责人发生变化，通知新的负责人</li>
 *     <li>移动任务到其他列表时发送通知</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    /** 任务 Mapper */
    private final TaskMapper taskMapper;

    /** 任务列表 Mapper */
    private final TaskListMapper taskListMapper;

    /** 项目服务，获取项目所属团队以进行权限校验 */
    private final ProjectService projectService;

    /** 团队服务，用于成员身份校验 */
    private final TeamService teamService;

    /** 通知服务，用于发送任务分配通知 */
    private final NotificationService notificationService;

    /** 日期时间格式化器 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 查询任务列表 ====================

    /**
     * 获取任务列表中的任务（看板列任务，分页，支持多条件筛选）
     * <p>
     * 分页查询指定任务列表下的所有任务，支持以下筛选条件：
     * <ul>
     *     <li>sortBy —— 排序字段（需经 SqlUtils 白名单校验）</li>
     *     <li>priority —— 按优先级过滤（high/medium/low）</li>
     *     <li>stageId —— 按阶段过滤</li>
     *     <li>assigneeId —— 按负责人过滤</li>
     *     <li>keyword —— 按标题关键词模糊搜索</li>
     * </ul>
     * </p>
     *
     * @param listId     任务列表 ID
     * @param pageNum    当前页码，从 1 开始
     * @param pageSize   每页记录数
     * @param sortBy     排序方式，可为 null 表示默认排序
     * @param priority   优先级筛选，可为 null
     * @param stageId    阶段筛选，可为 null
     * @param assigneeId 负责人筛选，可为 null
     * @param keyword    关键词搜索，可为 null
     * @return 分页的任务简要响应列表
     */
    @Override
    public Page<TaskBriefResponse> getTasksByListId(Long listId, int pageNum, int pageSize,
                                                     String sortBy, String priority,
                                                     Long stageId, Long assigneeId, String keyword) {
        // SQL 注入防护：校验排序字段白名单
        if (sortBy != null && !sortBy.isEmpty()) {
            SqlUtils.validateSortField(sortBy, SqlUtils.getTaskSortFields());
        }

        // 执行分页查询
        Page<Task> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = taskMapper.selectByListId(
                queryPage, listId, sortBy, priority, stageId, assigneeId, keyword);

        // 转换为 DTO 列表
        List<TaskBriefResponse> records = resultPage.getRecords().stream().map(row -> {
            TaskBriefResponse response = new TaskBriefResponse();
            response.setTaskId(MapUtils.getLong(row, "task_id"));
            response.setTitle((String) row.get("title"));
            response.setDescription((String) row.get("description"));
            response.setPriority((String) row.get("priority"));
            response.setListId(MapUtils.getLong(row, "list_id"));
            response.setListName((String) row.get("list_name"));
            response.setStageId(row.get("stage_id") != null ? MapUtils.getLong(row, "stage_id") : null);
            response.setStageName((String) row.get("stage_name"));
            response.setAssigneeId(row.get("assignee_id") != null
                    ? MapUtils.getLong(row, "assignee_id") : null);
            response.setAssigneeName((String) row.get("assignee_name"));
            response.setAssigneeAvatar((String) row.get("assignee_avatar"));
            response.setCreatorId(MapUtils.getLong(row, "creator_id"));
            response.setCreatorName((String) row.get("creator_name"));
            if (row.get("due_date") != null) {
                response.setDueDate((LocalDateTime) row.get("due_date"));
            }
            response.setPosition(((Number) row.get("position")).intValue());
            if (row.get("created_at") != null) {
                response.setCreatedAt((LocalDateTime) row.get("created_at"));
            }
            if (row.get("updated_at") != null) {
                response.setUpdatedAt((LocalDateTime) row.get("updated_at"));
            }
            return response;
        }).collect(Collectors.toList());

        // 构建分页响应
        Page<TaskBriefResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询任务列表: listId={}, 过滤条件[sortBy={}, priority={}, stageId={}, assigneeId={}, keyword={}], 总数={}",
                listId, sortBy, priority, stageId, assigneeId, keyword, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 创建任务 ====================

    /**
     * 创建新任务（团队成员均可操作）
     * <p>
     * 完整创建流程：
     * <ol>
     *     <li>通过任务列表 → 项目 → 团队链校验用户是该团队成员</li>
     *     <li>计算新任务的排序位置（当前列表最大 position + 1）</li>
     *     <li>插入任务记录，创建者为当前用户</li>
     *     <li>如果指定了负责人（assigneeId），向负责人发送通知</li>
     * </ol>
     * </p>
     *
     * @param listId  任务列表 ID
     * @param userId  创建者用户 ID
     * @param request 创建任务请求，包含标题、描述、优先级、负责人等
     * @return 包含 taskId、title、priority、position、listName 的键值对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTask(Long listId, Long userId, CreateTaskRequest request) {
        // 1. 校验任务列表存在
        TaskList taskList = taskListMapper.selectById(listId);
        if (taskList == null) {
            throw new BusinessException(ErrorCode.TASK_LIST_NOT_FOUND);
        }

        // 2. 通过项目 → 团队链校验用户是该团队成员
        Long teamId = projectService.getProjectTeamId(taskList.getProjectId());
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        teamService.checkIsMember(teamId, userId);

        // 3. 计算排序位置
        int maxPos = taskMapper.maxPosition(listId);
        int position = maxPos >= 0 ? maxPos + 1 : 0;

        // 4. 构建任务实体
        Task task = new Task();
        task.setListId(listId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "medium");
        // 解析截止日期字符串
        if (request.getDueDate() != null && !request.getDueDate().isBlank()) {
            task.setDueDate(LocalDateTime.parse(request.getDueDate(), DATE_TIME_FORMATTER));
        }
        task.setAssigneeId(request.getAssigneeId());
        task.setStageId(request.getStageId());
        task.setCreatorId(userId);
        task.setPosition(position);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        // 5. 如果指定了负责人，发送通知
        if (request.getAssigneeId() != null) {
            notificationService.sendNotification(
                    request.getAssigneeId(),
                    "task",
                    "您被分配了新的任务",
                    "任务「" + task.getTitle() + "」已分配给您。",
                    "task",
                    task.getTaskId()
            );
            log.info("已向负责人发送任务分配通知: taskId={}, assigneeId={}",
                    task.getTaskId(), request.getAssigneeId());
        }

        log.info("任务已创建: taskId={}, title={}, listId={}, creatorId={}",
                task.getTaskId(), task.getTitle(), listId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getTaskId());
        result.put("title", task.getTitle());
        result.put("priority", task.getPriority());
        result.put("position", task.getPosition());
        result.put("listName", taskList.getName());
        return result;
    }

    // ==================== 任务详情 ====================

    /**
     * 获取任务完整详情
     * <p>
     * 通过 taskMapper.selectTaskDetail 执行复杂的多表 JOIN 查询，
     * 获取任务的完整信息，包括所属列表、阶段、项目、团队以及
     * 负责人和创建者的详细信息。
     * </p>
     *
     * @param taskId 任务 ID
     * @return 任务详情响应，包含完整的上下文信息
     * @throws BusinessException 当任务不存在时（ErrorCode.TASK_NOT_FOUND）
     */
    @Override
    public TaskDetailResponse getTaskDetail(Long taskId) {
        Map<String, Object> detailRow = taskMapper.selectTaskDetail(taskId);
        if (detailRow == null || detailRow.get("task_id") == null) {
            log.warn("任务不存在: taskId={}", taskId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 组装完整响应
        TaskDetailResponse response = new TaskDetailResponse();
        response.setTaskId(MapUtils.getLong(detailRow, "task_id"));
        response.setTitle((String) detailRow.get("title"));
        response.setDescription((String) detailRow.get("description"));
        response.setPriority((String) detailRow.get("priority"));
        response.setListId(MapUtils.getLong(detailRow, "list_id"));
        response.setListName((String) detailRow.get("list_name"));
        response.setStageId(detailRow.get("stage_id") != null
                ? MapUtils.getLong(detailRow, "stage_id") : null);
        response.setStageName((String) detailRow.get("stage_name"));
        response.setProjectId(MapUtils.getLong(detailRow, "project_id"));
        response.setProjectName((String) detailRow.get("project_name"));
        response.setTeamId(MapUtils.getLong(detailRow, "team_id"));
        response.setTeamName((String) detailRow.get("team_name"));
        response.setAssigneeId(detailRow.get("assignee_id") != null
                ? MapUtils.getLong(detailRow, "assignee_id") : null);
        response.setAssigneeName((String) detailRow.get("assignee_name"));
        response.setAssigneeAvatar((String) detailRow.get("assignee_avatar"));
        response.setCreatorId(MapUtils.getLong(detailRow, "creator_id"));
        response.setCreatorName((String) detailRow.get("creator_name"));
        if (detailRow.get("due_date") != null) {
            response.setDueDate((LocalDateTime) detailRow.get("due_date"));
        }
        response.setPosition(((Number) detailRow.get("position")).intValue());
        if (detailRow.get("created_at") != null) {
            response.setCreatedAt((LocalDateTime) detailRow.get("created_at"));
        }
        if (detailRow.get("updated_at") != null) {
            response.setUpdatedAt((LocalDateTime) detailRow.get("updated_at"));
        }

        return response;
    }

    // ==================== 更新任务 ====================

    /**
     * 更新任务字段（团队成员均可操作）
     * <p>
     * 允许修改任务的标题、描述、优先级、截止日期、负责人和所属阶段。
     * 所有字段均为可选（仅更新非 null 字段）。
     * 如果负责人发生变化，向新的负责人发送任务分配通知。
     * </p>
     *
     * @param taskId  任务 ID
     * @param userId  当前操作用户 ID
     * @param request 更新请求，包含要修改的任务字段
     * @throws BusinessException 当任务不存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(Long taskId, Long userId, UpdateTaskRequest request) {
        // 1. 校验任务存在
        Task task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 2. 记录原负责人 ID（用于变更通知）
        Long oldAssigneeId = task.getAssigneeId();

        // 3. 构建更新对象（仅设置非 null 字段）
        Task update = new Task();
        update.setTaskId(taskId);
        if (request.getTitle() != null) {
            update.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            update.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            update.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null && !request.getDueDate().isBlank()) {
            update.setDueDate(LocalDateTime.parse(request.getDueDate(), DATE_TIME_FORMATTER));
        }
        if (request.getAssigneeId() != null) {
            update.setAssigneeId(request.getAssigneeId());
        }
        if (request.getStageId() != null) {
            update.setStageId(request.getStageId());
        }
        update.setUpdatedAt(LocalDateTime.now());
        this.updateById(update);

        // 4. 负责人变更通知
        if (request.getAssigneeId() != null && !request.getAssigneeId().equals(oldAssigneeId)) {
            notificationService.sendNotification(
                    request.getAssigneeId(),
                    "task",
                    "您被分配了任务",
                    "任务「" + task.getTitle() + "」已分配给您。",
                    "task",
                    taskId
            );
            log.info("负责人变更通知已发送: taskId={}, newAssigneeId={}", taskId, request.getAssigneeId());
        }

        log.info("任务已更新: taskId={}, operatorId={}", taskId, userId);
    }

    // ==================== 删除任务 ====================

    /**
     * 删除任务（团队成员均可操作）
     * <p>
     * 直接通过 MyBatis-Plus 的 removeById 删除任务记录。
     * </p>
     *
     * @param taskId 任务 ID
     * @param userId 当前操作用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long userId) {
        Task task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        this.removeById(taskId);
        log.info("任务已删除: taskId={}, title={}, operatorId={}",
                taskId, task.getTitle(), userId);
    }

    // ==================== 移动任务 ====================

    /**
     * 移动任务到其他列表（看板卡片拖拽）
     * <p>
     * 将任务移动到另一个任务列表（看板列），可同时指定在目标列表中的位置。
     * 移动前会校验目标列表存在且属于同一项目。
     * 如果指定了 targetPosition，任务将被放置在目标位置；
     * 如果未指定，则放置在目标列表末尾。
     * </p>
     *
     * @param taskId  任务 ID
     * @param userId  当前操作用户 ID
     * @param request 移动请求，包含目标列表 ID 和可选的目标位置
     * @throws BusinessException 当任务或目标列表不存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTask(Long taskId, Long userId, MoveTaskRequest request) {
        // 1. 校验任务存在
        Task task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 2. 校验目标列表存在
        TaskList targetList = taskListMapper.selectById(request.getTargetListId());
        if (targetList == null) {
            throw new BusinessException(ErrorCode.TASK_LIST_NOT_FOUND);
        }

        // 3. 确定目标位置（未指定则放在列表末尾）
        int targetPosition = request.getTargetPosition() != null
                ? request.getTargetPosition()
                : taskMapper.maxPosition(request.getTargetListId()) + 1;

        // 4. 更新任务的列表归属和排序位置
        taskMapper.updatePosition(taskId, targetPosition, request.getTargetListId());

        log.info("任务已移动: taskId={}, fromListId={}, toListId={}, targetPosition={}",
                taskId, task.getListId(), request.getTargetListId(), targetPosition);
    }

    // ==================== 任务排序 ====================

    /**
     * 重新排序任务（同一列表内的拖拽排序）
     * <p>
     * 接收一个排序项列表，每个排序项包含 taskId 和新的 position。
     * 逐条调用 taskMapper.updatePosition 更新各任务的归属列表和排序位置。
     * 适用于同一看板列内卡片的拖拽排序场景。
     * </p>
     *
     * @param userId  当前操作用户 ID
     * @param request 排序请求，包含列表 ID 和排序项列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderTasks(Long userId, TaskReorderRequest request) {
        Long listId = request.getListId();
        for (TaskReorderRequest.TaskOrderItem item : request.getOrders()) {
            taskMapper.updatePosition(item.getTaskId(), item.getPosition(), listId);
        }

        log.info("任务排序已更新: listId={}, count={}, operatorId={}",
                listId, request.getOrders().size(), userId);
    }

    // ==================== 辅助查询 ====================

    /**
     * 获取任务所属的项目 ID
     * <p>
     * 沿任务 → 任务列表 → 项目的关联链查询，共两次数据库查询：
     * <ol>
     *     <li>通过 taskId 查询 task.listId</li>
     *     <li>通过 listId 查询 taskList.projectId</li>
     * </ol>
     * 常用于权限校验场景。
     * </p>
     *
     * @param taskId 任务 ID
     * @return 任务所属的项目 ID，任务或列表不存在时返回 null
     */
    @Override
    public Long getTaskProjectId(Long taskId) {
        Task task = this.getById(taskId);
        if (task == null) {
            return null;
        }
        TaskList taskList = taskListMapper.selectById(task.getListId());
        return taskList != null ? taskList.getProjectId() : null;
    }
}
