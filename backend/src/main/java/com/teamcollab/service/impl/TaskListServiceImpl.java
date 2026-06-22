package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.tasklist.*;
import com.teamcollab.entity.TaskList;
import com.teamcollab.mapper.TaskListMapper;
import com.teamcollab.mapper.TaskMapper;
import com.teamcollab.service.ProjectService;
import com.teamcollab.service.TaskListService;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务列表服务实现类
 * <p>
 * 提供看板视图中任务列表（列）的查询、创建、更新、删除和排序功能。
 * 任务列表是看板中的一列，包含多个任务卡片。
 * </p>
 *
 * <p><b>默认列表保护：</b></p>
 * 系统创建的三个默认列表（待办、进行中、已完成）标记为 isDefault=1，
 * 这些列表不允许被删除，以确保看板视图的基础结构完整。
 *
 * <p><b>权限控制：</b></p>
 * <ul>
 *     <li>查看列表 —— 无需特殊权限（通过项目→团队链校验成员身份）</li>
 *     <li>创建/更新/删除/排序 —— 仅队长可操作</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskListServiceImpl extends ServiceImpl<TaskListMapper, TaskList> implements TaskListService {

    /** 任务列表 Mapper */
    private final TaskListMapper taskListMapper;

    /** 任务 Mapper，用于统计列表下的任务数 */
    private final TaskMapper taskMapper;

    /** 项目服务，获取项目所属团队以进行权限校验 */
    private final ProjectService projectService;

    /** 团队服务，用于权限校验 */
    private final TeamService teamService;

    // ==================== 查询任务列表 ====================

    /**
     * 获取项目的所有任务列表（看板列）
     * <p>
     * 查询指定项目下的所有任务列表，按 position 排序。
     * 每个列表附带任务数量统计和是否为默认列表的标记。
     * </p>
     *
     * @param projectId 项目 ID
     * @return 任务列表响应列表，按 position 升序排列
     */
    @Override
    public List<TaskListResponse> getProjectLists(Long projectId) {
        List<Map<String, Object>> listRows = taskListMapper.selectByProjectId(projectId);
        List<TaskListResponse> responses = listRows.stream().map(row -> {
            TaskListResponse response = new TaskListResponse();
            response.setListId(MapUtils.getLong(row, "list_id"));
            response.setName((String) row.get("name"));
            response.setPosition(((Number) row.get("position")).intValue());
            // is_default=1 表示系统默认列表（兼容 Boolean/Number 两种 JDBC 返回类型）
            Object isDef = row.get("is_default");
            response.setIsDefault(isDef != null && (
                (isDef instanceof Boolean && (Boolean) isDef) ||
                (isDef instanceof Number && ((Number) isDef).intValue() == 1)
            ));
            // 任务数量统计
            response.setTaskCount(row.get("task_count") != null
                    ? ((Number) row.get("task_count")).intValue() : 0);
            return response;
        }).collect(Collectors.toList());

        log.info("查询任务列表: projectId={}, 列表数={}", projectId, responses.size());
        return responses;
    }

    // ==================== 创建任务列表 ====================

    /**
     * 创建自定义任务列表（仅队长可操作）
     * <p>
     * 新列表的 position 自动设置为当前项目下最大 position + 1，
     * 确保新列表排在最后。自定义列表的 isDefault 为 0（可删除）。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID（需为队长）
     * @param request   创建请求，包含列表名称
     * @return 包含 listId、name、position、isDefault 的键值对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTaskList(Long projectId, Long userId, CreateTaskListRequest request) {
        // 1. 获取团队 ID 并校验队长权限
        Long teamId = projectService.getProjectTeamId(projectId);
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        teamService.checkIsLeader(teamId, userId);

        // 2. 计算排序位置（当前最大 position + 1）
        int maxPos = taskListMapper.maxPosition(projectId);
        int position = maxPos >= 0 ? maxPos + 1 : 0;

        // 3. 插入任务列表记录
        TaskList taskList = new TaskList();
        taskList.setProjectId(projectId);
        taskList.setName(request.getName());
        taskList.setPosition(position);
        taskList.setIsDefault(0);           // 自定义列表，非默认
        taskListMapper.insert(taskList);

        log.info("任务列表已创建: listId={}, name={}, position={}, projectId={}",
                taskList.getListId(), taskList.getName(), position, projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("listId", taskList.getListId());
        result.put("name", taskList.getName());
        result.put("position", taskList.getPosition());
        result.put("isDefault", false);
        return result;
    }

    // ==================== 更新任务列表 ====================

    /**
     * 更新任务列表名称（仅队长可操作）
     * <p>
     * 仅允许修改列表的名称，不修改排序位置和是否为默认列表的属性。
     * </p>
     *
     * @param listId  任务列表 ID
     * @param userId  当前操作用户 ID（需为队长）
     * @param request 更新请求，包含新的列表名称
     * @throws BusinessException 当列表不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskList(Long listId, Long userId, UpdateTaskListRequest request) {
        // 1. 校验列表存在
        TaskList taskList = this.getById(listId);
        if (taskList == null) {
            throw new BusinessException(ErrorCode.TASK_LIST_NOT_FOUND);
        }

        // 2. 校验队长权限
        Long teamId = projectService.getProjectTeamId(taskList.getProjectId());
        teamService.checkIsLeader(teamId, userId);

        // 3. 更新名称
        TaskList update = new TaskList();
        update.setListId(listId);
        update.setName(request.getName());
        taskListMapper.updateById(update);

        log.info("任务列表名称已更新: listId={}, newName={}", listId, request.getName());
    }

    // ==================== 删除任务列表 ====================

    /**
     * 删除任务列表（仅队长可操作）
     * <p>
     * 删除规则：
     * <ul>
     *     <li>系统默认列表（isDefault=1）不可删除，抛出 DEFAULT_LIST_CANNOT_DELETE 异常</li>
     *     <li>删除操作会级联删除该列表下的所有任务</li>
     * </ul>
     * </p>
     *
     * @param listId 任务列表 ID
     * @param userId 当前操作用户 ID（需为队长）
     * @throws BusinessException 当列表不存在、用户不是队长、或试图删除默认列表时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskList(Long listId, Long userId) {
        // 1. 校验列表存在
        TaskList taskList = this.getById(listId);
        if (taskList == null) {
            throw new BusinessException(ErrorCode.TASK_LIST_NOT_FOUND);
        }

        // 2. 系统默认列表不可删除
        if (taskList.getIsDefault() != null && taskList.getIsDefault() == 1) {
            log.warn("删除被拒绝 - 默认列表不可删除: listId={}, name={}", listId, taskList.getName());
            throw new BusinessException(ErrorCode.DEFAULT_LIST_CANNOT_DELETE);
        }

        // 3. 校验队长权限
        Long teamId = projectService.getProjectTeamId(taskList.getProjectId());
        teamService.checkIsLeader(teamId, userId);

        // 4. 级联删除
        this.removeById(listId);
        log.info("任务列表已删除: listId={}, name={}, operatorId={}",
                listId, taskList.getName(), userId);
    }

    // ==================== 任务列表排序 ====================

    /**
     * 重新排序任务列表（拖拽排序，仅队长可操作）
     * <p>
     * 接收一个排序项列表，每个排序项包含 listId 和新的 position。
     * 逐条调用 taskListMapper.updatePosition 更新各列表的排序位置。
     * 适用于看板列拖拽排序的场景。
     * </p>
     *
     * @param userId  当前操作用户 ID（需为队长）
     * @param request 排序请求，包含排序项列表（listId + position）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderLists(Long userId, ReorderRequest request) {
        // 逐条更新各列表的排序位置
        for (ReorderRequest.OrderItem item : request.getOrders()) {
            taskListMapper.updatePosition(item.getListId(), item.getPosition());
        }

        log.info("任务列表排序已更新: count={}, operatorId={}",
                request.getOrders().size(), userId);
    }

    // ==================== 辅助查询 ====================

    /**
     * 获取任务列表所属的项目 ID
     * <p>
     * 通过列表 ID 查询 task_lists 表，返回其关联的 project_id。
     * 常用于权限校验场景（根据列表找到项目，再找到团队，再校验用户权限）。
     * </p>
     *
     * @param listId 任务列表 ID
     * @return 列表所属的项目 ID，列表不存在时返回 null
     */
    @Override
    public Long getTaskListProjectId(Long listId) {
        TaskList taskList = this.getById(listId);
        return taskList != null ? taskList.getProjectId() : null;
    }
}
