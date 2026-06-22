package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.project.*;
import com.teamcollab.entity.Project;

import java.util.Map;

/**
 * 项目服务接口
 * <p>
 * 提供团队项目的创建、查询、更新、删除功能。
 * 创建项目时会自动生成三个默认任务列表（待办、进行中、已完成）。
 * 项目级操作需要队长权限。
 * </p>
 *
 * @author teamcollab
 */
public interface ProjectService extends IService<Project> {

    /**
     * 获取团队项目列表（分页）
     * <p>
     * 查询指定团队下的所有项目，每个项目附带任务统计信息。
     * </p>
     *
     * @param teamId   团队ID
     * @param userId   当前操作用户ID
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @return 分页的项目简要响应列表，包含任务统计信息
     */
    Page<ProjectBriefResponse> getTeamProjects(Long teamId, Long userId, int pageNum, int pageSize);

    /**
     * 创建项目
     * <p>
     * 在指定团队下创建新项目，仅队长有权执行。
     * 创建成功后将自动生成三个默认任务列表：待办、进行中、已完成。
     * </p>
     *
     * @param teamId 团队ID
     * @param userId 当前操作用户ID（需为队长）
     * @param request 创建项目请求，包含项目名称、描述等信息
     * @return 包含新创建项目基本信息的键值对（如项目ID、名称等）
     */
    Map<String, Object> createProject(Long teamId, Long userId, CreateProjectRequest request);

    /**
     * 获取项目详情
     * <p>
     * 查询项目的完整信息，包含阶段划分和各阶段的任务统计。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID
     * @return 项目详情响应，包含阶段和任务统计信息
     */
    ProjectDetailResponse getProjectDetail(Long projectId, Long userId);

    /**
     * 更新项目信息
     * <p>
     * 修改项目的基本信息，仅队长有权执行。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID（需为队长）
     * @param request   更新项目请求，包含要修改的项目字段
     */
    void updateProject(Long projectId, Long userId, UpdateProjectRequest request);

    /**
     * 删除项目
     * <p>
     * 删除指定项目，仅队长有权执行。
     * 删除操作会级联删除该项目下的所有阶段、任务列表和任务。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID（需为队长）
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * 获取项目所属团队ID
     * <p>
     * 通过项目ID查询其所属的团队ID，常用于权限校验场景。
     * </p>
     *
     * @param projectId 项目ID
     * @return 项目所属的团队ID
     */
    Long getProjectTeamId(Long projectId);
}
