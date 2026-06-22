package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.stage.*;
import com.teamcollab.entity.Stage;

import java.util.List;
import java.util.Map;

/**
 * 阶段服务接口
 * <p>
 * 提供项目阶段的创建、查询、更新、删除以及模板管理功能。
 * 阶段是项目的时间分片单元，包含截止日期和状态信息。
 * 阶段级操作需要队长权限。
 * </p>
 *
 * @author teamcollab
 */
public interface StageService extends IService<Stage> {

    /**
     * 获取项目的所有阶段
     * <p>
     * 查询指定项目下的所有阶段，每个阶段附带任务统计和逾期状态信息。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID
     * @return 阶段响应列表，包含任务统计和逾期状态
     */
    List<StageResponse> getProjectStages(Long projectId, Long userId);

    /**
     * 创建阶段
     * <p>
     * 在指定项目下创建新阶段，仅队长有权执行。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID（需为队长）
     * @param request   创建阶段请求，包含阶段名称、截止日期等
     * @return 包含新创建阶段基本信息的键值对（如阶段ID、名称等）
     */
    Map<String, Object> createStage(Long projectId, Long userId, CreateStageRequest request);

    /**
     * 更新阶段信息
     * <p>
     * 修改阶段的基本信息，仅队长有权执行。
     * </p>
     *
     * @param stageId 阶段ID
     * @param userId  当前操作用户ID（需为队长）
     * @param request 更新阶段请求，包含要修改的阶段字段
     */
    void updateStage(Long stageId, Long userId, UpdateStageRequest request);

    /**
     * 删除阶段
     * <p>
     * 删除指定阶段，仅队长有权执行。
     * 如果阶段下存在未完成的任务，则不允许删除。
     * </p>
     *
     * @param stageId 阶段ID
     * @param userId  当前操作用户ID（需为队长）
     */
    void deleteStage(Long stageId, Long userId);

    /**
     * 更新阶段状态
     * <p>
     * 修改阶段的状态（如未开始、进行中、已完成），仅队长有权执行。
     * </p>
     *
     * @param stageId 阶段ID
     * @param userId  当前操作用户ID（需为队长）
     * @param request 更新阶段状态请求，包含目标状态
     */
    void updateStageStatus(Long stageId, Long userId, UpdateStageStatusRequest request);

    /**
     * 获取阶段模板列表
     * <p>
     * 查询系统中预设的阶段模板，供创建项目时快速应用。
     * </p>
     *
     * @return 阶段模板响应列表
     */
    List<StageTemplateResponse> getStageTemplates();

    /**
     * 应用阶段模板批量创建阶段
     * <p>
     * 根据选定的模板在项目中批量创建阶段，仅队长有权执行。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID（需为队长）
     * @param request   应用模板请求，包含要使用的模板ID列表
     * @return 批量创建的各阶段基本信息列表
     */
    List<Map<String, Object>> applyTemplate(Long projectId, Long userId, ApplyTemplateRequest request);

    /**
     * 获取阶段所属项目ID
     * <p>
     * 通过阶段ID查询其所属的项目ID，常用于权限校验场景。
     * </p>
     *
     * @param stageId 阶段ID
     * @return 阶段所属的项目ID
     */
    Long getStageProjectId(Long stageId);
}
