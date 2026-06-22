package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.stage.*;
import com.teamcollab.entity.Stage;
import com.teamcollab.entity.StageTemplate;
import com.teamcollab.mapper.StageMapper;
import com.teamcollab.mapper.StageTemplateMapper;
import com.teamcollab.service.ProjectService;
import com.teamcollab.service.StageService;
import com.teamcollab.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 阶段服务实现类
 * <p>
 * 提供项目阶段的创建、查询、更新、删除以及模板管理功能。
 * 阶段是项目的时间分片单元，用于按时间段组织任务和管理项目进度。
 * </p>
 *
 * <p><b>阶段状态流转：</b></p>
 * <pre>
 *   not_started  -->  in_progress  -->  completed
 * </pre>
 *
 * <p><b>超期判断：</b></p>
 * 当当前日期超过阶段的 endDate 且阶段状态不是 "completed" 时，
 * 该阶段被标记为超期（isOverdue = true）。
 * </p>
 *
 * <p><b>阶段模板：</b></p>
 * 系统提供预定义的阶段模板（如"标准软件开发流程"），
 * 队长可将模板应用到项目中，批量创建阶段结构。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StageServiceImpl extends ServiceImpl<StageMapper, Stage> implements StageService {

    /** 阶段 Mapper */
    private final StageMapper stageMapper;

    /** 阶段模板 Mapper */
    private final StageTemplateMapper stageTemplateMapper;

    /** 项目服务，通过项目获取团队信息进行权限校验 */
    private final ProjectService projectService;

    /** 团队服务，用于权限校验 */
    private final TeamService teamService;

    /** Jackson ObjectMapper，用于解析模板中的 JSON 数据 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 查询阶段列表 ====================

    /**
     * 获取项目的所有阶段（含任务统计和超期状态）
     * <p>
     * 查询指定项目下的所有阶段，每个阶段附带：
     * <ul>
     *     <li>任务总数和已完成数</li>
     *     <li>完成率百分比</li>
     *     <li>是否超期（当前日期 > 结束日期 且 阶段未完成）</li>
     * </ul>
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID
     * @return 阶段响应列表，按 orderIndex 排序
     */
    @Override
    public List<StageResponse> getProjectStages(Long projectId, Long userId) {
        // 1. 获取项目所属团队
        Long teamId = projectService.getProjectTeamId(projectId);
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }

        // 2. 查询阶段列表（含任务统计）
        List<Map<String, Object>> stageRows = stageMapper.selectByProjectId(projectId);

        // 3. 转换为响应 DTO 列表
        List<StageResponse> stages = stageRows.stream().map(row -> {
            StageResponse response = new StageResponse();
            response.setStageId(MapUtils.getLong(row, "stage_id"));
            response.setProjectId(MapUtils.getLong(row, "project_id"));
            response.setName((String) row.get("name"));
            response.setDescription((String) row.get("description"));
            response.setStartDate(MapUtils.getLocalDate(row, "start_date"));
            response.setEndDate(MapUtils.getLocalDate(row, "end_date"));
            response.setOrderIndex(((Number) row.get("order_index")).intValue());
            response.setStatus((String) row.get("status"));

            // 3.1 计算任务统计
            int total = row.get("task_count") != null
                    ? ((Number) row.get("task_count")).intValue() : 0;
            int completed = row.get("completed_count") != null
                    ? ((Number) row.get("completed_count")).intValue() : 0;
            StageResponse.TaskStatsVO stats = new StageResponse.TaskStatsVO();
            stats.setTotal(total);
            stats.setCompleted(completed);
            stats.setCompletionRate(total > 0
                    ? Math.round(completed * 1000.0 / total) / 1000.0 : 0.0);
            response.setTaskStats(stats);

            // 3.2 判断是否超期
            //     条件：当前日期 > 结束日期 且 阶段状态不是已完成
            LocalDate endDate = response.getEndDate();
            response.setIsOverdue(endDate != null
                    && LocalDate.now().isAfter(endDate)
                    && !"completed".equals(response.getStatus()));

            return response;
        }).collect(Collectors.toList());

        log.info("查询项目阶段: projectId={}, 阶段数={}", projectId, stages.size());
        return stages;
    }

    // ==================== 创建阶段 ====================

    /**
     * 创建新阶段（仅队长可操作）
     * <p>
     * 新阶段的 orderIndex 自动设置为当前项目下最大 orderIndex + 1，
     * 初始状态为 "not_started"（未开始）。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID（需为队长）
     * @param request   创建阶段请求，包含名称、描述、起止日期
     * @return 包含 stageId、name、orderIndex、status 的键值对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createStage(Long projectId, Long userId, CreateStageRequest request) {
        // 1. 获取团队 ID 并校验队长权限
        Long teamId = projectService.getProjectTeamId(projectId);
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        teamService.checkIsLeader(teamId, userId);

        // 2. 计算排序索引（当前项目下最大 orderIndex + 1）
        int maxOrder = stageMapper.maxOrderIndex(projectId);
        int orderIndex = maxOrder >= 0 ? maxOrder + 1 : 0;

        // 3. 插入阶段记录
        Stage stage = new Stage();
        stage.setProjectId(projectId);
        stage.setName(request.getName());
        stage.setDescription(request.getDescription());
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        stage.setOrderIndex(orderIndex);
        stage.setStatus("not_started");      // 初始状态：未开始
        stageMapper.insert(stage);

        log.info("阶段已创建: stageId={}, name={}, orderIndex={}, projectId={}",
                stage.getStageId(), stage.getName(), orderIndex, projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("stageId", stage.getStageId());
        result.put("name", stage.getName());
        result.put("orderIndex", stage.getOrderIndex());
        result.put("status", stage.getStatus());
        return result;
    }

    // ==================== 更新阶段 ====================

    /**
     * 更新阶段信息（仅队长可操作）
     * <p>
     * 允许修改阶段的名称、描述、开始日期和结束日期。
     * 所有字段均为可选（仅更新非 null 字段）。
     * </p>
     *
     * @param stageId 阶段 ID
     * @param userId  当前操作用户 ID（需为队长）
     * @param request 更新请求，包含可选的 name、description、startDate、endDate
     * @throws BusinessException 当阶段不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStage(Long stageId, Long userId, UpdateStageRequest request) {
        // 1. 校验阶段存在
        Stage stage = this.getById(stageId);
        if (stage == null) {
            throw new BusinessException(ErrorCode.STAGE_NOT_FOUND);
        }

        // 2. 校验队长权限
        Long teamId = projectService.getProjectTeamId(stage.getProjectId());
        teamService.checkIsLeader(teamId, userId);

        // 3. 更新字段（仅更新非 null 字段）
        Stage update = new Stage();
        update.setStageId(stageId);
        if (request.getName() != null) {
            update.setName(request.getName());
        }
        if (request.getDescription() != null) {
            update.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            update.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            update.setEndDate(request.getEndDate());
        }
        this.updateById(update);

        log.info("阶段已更新: stageId={}, operatorId={}", stageId, userId);
    }

    // ==================== 删除阶段 ====================

    /**
     * 删除阶段（仅队长可操作）
     * <p>
     * 删除前会检查该阶段下是否存在未完成的任务。
     * 如果存在未完成的任务，抛出 STAGE_HAS_TASKS 异常，拒绝删除。
     * 注意：这里直接统计该阶段下的任务数，如果 > 0 即拒绝删除。
     * </p>
     *
     * @param stageId 阶段 ID
     * @param userId  当前操作用户 ID（需为队长）
     * @throws BusinessException 阶段不存在、用户不是队长、
     *                           或阶段下有未完成任务时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStage(Long stageId, Long userId) {
        // 1. 校验阶段存在
        Stage stage = this.getById(stageId);
        if (stage == null) {
            throw new BusinessException(ErrorCode.STAGE_NOT_FOUND);
        }

        // 2. 校验队长权限
        Long teamId = projectService.getProjectTeamId(stage.getProjectId());
        teamService.checkIsLeader(teamId, userId);

        // 3. 检查阶段下是否有任务
        int taskCount = stageMapper.countTasksByStageId(stageId);
        if (taskCount > 0) {
            log.warn("删除阶段被拒绝 - 阶段下仍有 {} 个任务: stageId={}", taskCount, stageId);
            throw new BusinessException(ErrorCode.STAGE_HAS_TASKS);
        }

        // 4. 删除阶段
        this.removeById(stageId);
        log.info("阶段已删除: stageId={}, name={}, operatorId={}",
                stageId, stage.getName(), userId);
    }

    // ==================== 更新阶段状态 ====================

    /**
     * 更新阶段状态（仅队长可操作）
     * <p>
     * 修改阶段的执行状态。支持的状态值：
     * "not_started"、"in_progress"、"completed"。
     * </p>
     *
     * @param stageId 阶段 ID
     * @param userId  当前操作用户 ID（需为队长）
     * @param request 更新状态请求，包含目标状态
     * @throws BusinessException 当阶段不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStageStatus(Long stageId, Long userId, UpdateStageStatusRequest request) {
        // 1. 校验阶段存在
        Stage stage = this.getById(stageId);
        if (stage == null) {
            throw new BusinessException(ErrorCode.STAGE_NOT_FOUND);
        }

        // 2. 校验队长权限
        Long teamId = projectService.getProjectTeamId(stage.getProjectId());
        teamService.checkIsLeader(teamId, userId);

        // 3. 更新状态
        Stage update = new Stage();
        update.setStageId(stageId);
        update.setStatus(request.getStatus());
        this.updateById(update);

        log.info("阶段状态已更新: stageId={}, oldStatus={}, newStatus={}",
                stageId, stage.getStatus(), request.getStatus());
    }

    // ==================== 阶段模板 ====================

    /**
     * 获取所有阶段模板
     * <p>
     * 查询系统中预设的阶段模板，解析模板中存储的 JSON 阶段配置数据，
     * 组装为 StageTemplateResponse 列表返回。用于前端模板选择界面。
     * </p>
     *
     * @return 阶段模板响应列表，包含模板名称、阶段数量和阶段项列表
     */
    @Override
    public List<StageTemplateResponse> getStageTemplates() {
        List<Map<String, Object>> templateRows = stageTemplateMapper.selectAllWithCount();
        List<StageTemplateResponse> templates = templateRows.stream().map(row -> {
            StageTemplateResponse response = new StageTemplateResponse();
            response.setTemplateId(MapUtils.getLong(row, "template_id"));
            response.setTemplateName((String) row.get("template_name"));
            response.setStageCount(row.get("stage_count") != null
                    ? ((Number) row.get("stage_count")).intValue() : 0);

            // 解析 stages_json 字段中的 JSON 数组
            try {
                String stagesJson = (String) row.get("stages_json");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stageItems = objectMapper.readValue(stagesJson, List.class);
                List<StageTemplateResponse.StageTemplateItem> items = stageItems.stream().map(item -> {
                    StageTemplateResponse.StageTemplateItem templateItem =
                            new StageTemplateResponse.StageTemplateItem();
                    templateItem.setName((String) item.get("name"));
                    templateItem.setDescription((String) item.get("description"));
                    templateItem.setOrderIndex(item.get("orderIndex") != null
                            ? ((Number) item.get("orderIndex")).intValue() : 0);
                    return templateItem;
                }).collect(Collectors.toList());
                response.setStages(items);
            } catch (Exception e) {
                log.warn("模板 JSON 解析失败: templateId={}", response.getTemplateId(), e);
                response.setStages(Collections.emptyList());
            }

            return response;
        }).collect(Collectors.toList());

        log.info("查询阶段模板: 共 {} 个模板", templates.size());
        return templates;
    }

    /**
     * 应用阶段模板批量创建阶段（仅队长可操作）
     * <p>
     * 根据选定的模板，在项目中批量创建阶段。
     * 通过解析模板中的 stages_json 字段（JSON 数组），
     * 为每个模板项创建一个 Stage 实体并插入数据库。
     * 各阶段的 orderIndex 在当前项目最大排序索引基础上递增。
     * </p>
     *
     * @param projectId 项目 ID
     * @param userId    当前操作用户 ID（需为队长）
     * @param request   应用模板请求，包含要使用的模板 ID
     * @return 批量创建的各阶段基本信息列表
     * @throws BusinessException 模板不存在或用户不是队长时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> applyTemplate(Long projectId, Long userId,
                                                    ApplyTemplateRequest request) {
        // 1. 校验队长权限
        Long teamId = projectService.getProjectTeamId(projectId);
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        teamService.checkIsLeader(teamId, userId);

        // 2. 查询模板
        StageTemplate template = stageTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "阶段模板不存在");
        }

        // 3. 解析模板 JSON 并批量创建阶段
        List<Map<String, Object>> createdStages = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stageItems = objectMapper.readValue(
                    template.getStagesJson(), List.class);

            // 获取当前项目下的最大排序索引
            int maxOrderIndex = stageMapper.maxOrderIndex(projectId);

            for (int i = 0; i < stageItems.size(); i++) {
                Map<String, Object> item = stageItems.get(i);
                Stage stage = new Stage();
                stage.setProjectId(projectId);
                stage.setName((String) item.get("name"));
                stage.setDescription((String) item.getOrDefault("description", null));
                stage.setOrderIndex(maxOrderIndex + 1 + i);  // 递增排序索引
                stage.setStatus("not_started");               // 初始状态
                stageMapper.insert(stage);

                Map<String, Object> resultItem = new HashMap<>();
                resultItem.put("stageId", stage.getStageId());
                resultItem.put("name", stage.getName());
                resultItem.put("orderIndex", stage.getOrderIndex());
                createdStages.add(resultItem);
            }
        } catch (Exception e) {
            log.error("模板解析或阶段创建失败: templateId={}, projectId={}",
                    request.getTemplateId(), projectId, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "模板应用失败: " + e.getMessage());
        }

        log.info("模板批量创建阶段完成: projectId={}, templateId={}, 创建数={}",
                projectId, request.getTemplateId(), createdStages.size());
        return createdStages;
    }

    // ==================== 辅助查询 ====================

    /**
     * 获取阶段所属的项目 ID
     * <p>
     * 通过阶段 ID 查询 stages 表，返回其关联的 project_id。
     * 常用于权限校验场景（根据阶段找到项目，再找到团队，再校验用户权限）。
     * </p>
     *
     * @param stageId 阶段 ID
     * @return 阶段所属的项目 ID，阶段不存在时返回 null
     */
    @Override
    public Long getStageProjectId(Long stageId) {
        Stage stage = this.getById(stageId);
        return stage != null ? stage.getProjectId() : null;
    }
}
