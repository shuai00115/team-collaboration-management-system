package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.project.*;
import com.teamcollab.entity.Project;
import com.teamcollab.entity.TaskList;
import com.teamcollab.mapper.ProjectMapper;
import com.teamcollab.mapper.TaskListMapper;
import com.teamcollab.service.ProjectService;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目服务实现类
 * <p>
 * 提供团队项目的创建、查询、更新、删除功能。
 * 项目是团队下的二级组织单元，包含阶段划分、任务列表和具体任务。
 * </p>
 *
 * <p><b>默认任务列表：</b></p>
 * 创建项目时会自动生成三个系统默认的任务列表（看板列）：
 * <ul>
 *     <li>待办（position=0, isDefault=1）</li>
 *     <li>进行中（position=1, isDefault=1）</li>
 *     <li>已完成（position=2, isDefault=1）</li>
 * </ul>
 * 这三个默认列表不可删除，确保看板视图始终可用。
 *
 * <p><b>权限控制：</b></p>
 * <ul>
 *     <li>查看项目列表和详情 —— 团队成员均可</li>
 *     <li>创建、更新、删除项目 —— 仅队长可操作</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    /** 项目 Mapper */
    private final ProjectMapper projectMapper;

    /** 团队服务，用于权限校验 */
    private final TeamService teamService;

    /** 任务列表 Mapper，用于创建默认列表 */
    private final TaskListMapper taskListMapper;

    // ==================== 获取项目列表 ====================

    /**
     * 获取团队的项目列表（分页，含任务统计）
     * <p>
     * 查询指定团队下的所有项目，每个项目附带任务总数和已完成数的统计。
     * 需要团队成员权限。
     * </p>
     *
     * @param teamId   团队 ID
     * @param userId   当前操作用户 ID
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @return 分页的项目简要响应列表
     */
    @Override
    public Page<ProjectBriefResponse> getTeamProjects(Long teamId, Long userId, int pageNum, int pageSize) {
        // 1. 分页查询项目列表（含任务统计）
        Page<Project> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = projectMapper.selectByTeamId(queryPage, teamId);

        // 2. 转换每一行为 ProjectBriefResponse
        List<ProjectBriefResponse> records = resultPage.getRecords().stream().map(row -> {
            ProjectBriefResponse response = new ProjectBriefResponse();
            response.setProjectId(MapUtils.getLong(row, "project_id"));
            response.setTeamId(MapUtils.getLong(row, "team_id"));
            response.setName((String) row.get("name"));
            response.setDescription((String) row.get("description"));
            if (row.get("created_at") != null) {
                response.setCreatedAt((LocalDateTime) row.get("created_at"));
            }
            if (row.get("updated_at") != null) {
                response.setUpdatedAt((LocalDateTime) row.get("updated_at"));
            }

            // 计算任务统计
            int total = row.get("total_tasks") != null
                    ? ((Number) row.get("total_tasks")).intValue() : 0;
            int completed = row.get("completed_tasks") != null
                    ? ((Number) row.get("completed_tasks")).intValue() : 0;
            ProjectBriefResponse.TaskStatsVO stats = new ProjectBriefResponse.TaskStatsVO();
            stats.setTotal(total);
            stats.setCompleted(completed);
            stats.setCompletionRate(total > 0
                    ? Math.round(completed * 1000.0 / total) / 1000.0 : 0.0);
            response.setTaskStats(stats);

            return response;
        }).collect(Collectors.toList());

        // 3. 构建分页响应
        Page<ProjectBriefResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询团队项目列表: teamId={}, 总数={}", teamId, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 创建项目 ====================

    /**
     * 创建新项目（仅队长可操作）
     * <p>
     * 创建项目后自动生成三个默认任务列表（待办、进行中、已完成），
     * 为看板视图提供基础列结构。整个过程在事务中执行。
     * </p>
     *
     * @param teamId  团队 ID
     * @param userId  当前操作用户 ID（需为队长）
     * @param request 创建项目请求，包含项目名称和描述
     * @return 包含 projectId、name 的键值对
     * @throws BusinessException 当用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createProject(Long teamId, Long userId, CreateProjectRequest request) {
        // 1. 队长权限校验
        teamService.checkIsLeader(teamId, userId);

        // 2. 插入项目记录
        Project project = new Project();
        project.setTeamId(teamId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.insert(project);
        log.info("项目已创建: projectId={}, name={}, teamId={}",
                project.getProjectId(), project.getName(), teamId);

        // 3. 自动创建三个默认任务列表（看板的基础列结构）
        //    格式：{列表名称, 排序位置}
        String[][] defaultLists = {
                {"待办", "0"},
                {"进行中", "1"},
                {"已完成", "2"}
        };
        for (String[] listDef : defaultLists) {
            TaskList taskList = new TaskList();
            taskList.setProjectId(project.getProjectId());
            taskList.setName(listDef[0]);
            taskList.setPosition(Integer.parseInt(listDef[1]));
            taskList.setIsDefault(1);        // 标记为系统默认列表（不可删除）
            taskList.setCreatedAt(LocalDateTime.now());
            taskListMapper.insert(taskList);
        }
        log.info("已为项目创建 3 个默认任务列表: projectId={}", project.getProjectId());

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", project.getProjectId());
        result.put("name", project.getName());
        result.put("teamId", teamId);

        return result;
    }

    // ==================== 项目详情 ====================

    /**
     * 获取项目完整详情
     * <p>
     * 整合三个数据源：
     * <ul>
     *     <li>项目基本信息 —— projectMapper.selectProjectDetail（含团队名称和任务统计）</li>
     *     <li>阶段列表 —— projectMapper.selectStagesByProjectId（含各阶段任务完成率）</li>
     * </ul>
     * 组装为完整的 ProjectDetailResponse。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID
     * @return ProjectDetailResponse 包含完整项目信息和阶段列表
     * @throws BusinessException 当项目不存在或用户不是团队成员时
     */
    @Override
    public ProjectDetailResponse getProjectDetail(Long projectId, Long userId) {
        // 1. 查询项目详情
        Map<String, Object> detailRow = projectMapper.selectProjectDetail(projectId);
        if (detailRow == null || detailRow.get("project_id") == null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 组装项目基本信息
        Long teamId = MapUtils.getLong(detailRow, "team_id");
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setProjectId(MapUtils.getLong(detailRow, "project_id"));
        response.setTeamId(teamId);
        response.setTeamName((String) detailRow.get("team_name"));
        response.setName((String) detailRow.get("name"));
        response.setDescription((String) detailRow.get("description"));
        if (detailRow.get("created_at") != null) {
            response.setCreatedAt((LocalDateTime) detailRow.get("created_at"));
        }
        if (detailRow.get("updated_at") != null) {
            response.setUpdatedAt((LocalDateTime) detailRow.get("updated_at"));
        }

        // 3. 组装任务统计信息
        int total = detailRow.get("total_tasks") != null
                ? ((Number) detailRow.get("total_tasks")).intValue() : 0;
        int completed = detailRow.get("completed_tasks") != null
                ? ((Number) detailRow.get("completed_tasks")).intValue() : 0;
        int inProgress = detailRow.get("in_progress_tasks") != null
                ? ((Number) detailRow.get("in_progress_tasks")).intValue() : 0;
        int todo = detailRow.get("todo_tasks") != null
                ? ((Number) detailRow.get("todo_tasks")).intValue() : 0;

        ProjectDetailResponse.TaskStatsFullVO stats = new ProjectDetailResponse.TaskStatsFullVO();
        stats.setTotal(total);
        stats.setCompleted(completed);
        stats.setInProgress(inProgress);
        stats.setTodo(todo);
        stats.setCompletionRate(total > 0
                ? Math.round(completed * 1000.0 / total) / 1000.0 : 0.0);
        response.setTaskStats(stats);

        // 4. 组装阶段列表（含各阶段完成率）
        List<Map<String, Object>> stageRows = projectMapper.selectStagesByProjectId(projectId);
        List<ProjectDetailResponse.StageBriefVO> stages = stageRows.stream().map(s -> {
            ProjectDetailResponse.StageBriefVO vo = new ProjectDetailResponse.StageBriefVO();
            vo.setStageId(MapUtils.getLong(s, "stage_id"));
            vo.setName((String) s.get("name"));
            vo.setStatus((String) s.get("status"));
            vo.setOrderIndex(((Number) s.get("order_index")).intValue());
            vo.setStartDate(MapUtils.getLocalDate(s, "start_date"));
            vo.setEndDate(MapUtils.getLocalDate(s, "end_date"));
            int stageTotal = s.get("stage_tasks") != null
                    ? ((Number) s.get("stage_tasks")).intValue() : 0;
            int stageCompleted = s.get("stage_completed") != null
                    ? ((Number) s.get("stage_completed")).intValue() : 0;
            vo.setCompletionRate(stageTotal > 0
                    ? Math.round(stageCompleted * 1000.0 / stageTotal) / 1000.0 : 0.0);
            return vo;
        }).collect(Collectors.toList());
        response.setStages(stages);

        log.info("查询项目详情: projectId={}, stages={}", projectId, stages.size());
        return response;
    }

    // ==================== 更新项目 ====================

    /**
     * 更新项目信息（仅队长可操作）
     * <p>
     * 允许修改项目的名称和描述，所有字段均为可选（仅更新非 null 字段）。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID（需为队长）
     * @param request   更新请求，包含可选的 name 和 description
     * @throws BusinessException 当项目不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request) {
        // 1. 校验项目存在
        Project project = this.getById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 队长权限校验
        teamService.checkIsLeader(project.getTeamId(), userId);

        // 3. 更新字段（仅更新非 null 的字段）
        Project update = new Project();
        update.setProjectId(projectId);
        if (request.getName() != null) {
            update.setName(request.getName());
        }
        if (request.getDescription() != null) {
            update.setDescription(request.getDescription());
        }
        update.setUpdatedAt(LocalDateTime.now());
        this.updateById(update);

        log.info("项目已更新: projectId={}, operatorId={}", projectId, userId);
    }

    // ==================== 删除项目 ====================

    /**
     * 删除项目（仅队长可操作）
     * <p>
     * 删除项目会级联删除其下的所有阶段、任务列表和任务
     * （通过数据库外键 ON DELETE CASCADE 实现）。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID（需为队长）
     * @throws BusinessException 当项目不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId, Long userId) {
        // 1. 校验项目存在
        Project project = this.getById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 队长权限校验
        teamService.checkIsLeader(project.getTeamId(), userId);

        // 3. 级联删除
        this.removeById(projectId);
        log.info("项目已删除: projectId={}, name={}, operatorId={}",
                projectId, project.getName(), userId);
    }

    // ==================== 辅助查询 ====================

    /**
     * 获取项目所属的团队 ID
     * <p>
     * 通过项目 ID 查询 projects 表，返回其关联的 team_id。
     * 常用于权限校验场景（根据项目找到团队，再校验用户权限）。
     * </p>
     *
     * @param projectId 项目 ID
     * @return 项目所属的团队 ID，项目不存在时返回 null
     */
    @Override
    public Long getProjectTeamId(Long projectId) {
        Project project = this.getById(projectId);
        return project != null ? project.getTeamId() : null;
    }
}
