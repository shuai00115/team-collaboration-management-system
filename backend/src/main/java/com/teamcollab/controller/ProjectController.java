package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.project.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "项目管理", description = "项目创建、查询、编辑、删除接口")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "获取团队项目列表")
    @GetMapping("/api/v1/teams/{teamId}/projects")
    public Result<PageResult<ProjectBriefResponse>> getTeamProjects(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ProjectBriefResponse> page = projectService.getTeamProjects(teamId, userId, pageNum, pageSize);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "创建项目")
    @PostMapping("/api/v1/teams/{teamId}/projects")
    public Result<Map<String, Object>> createProject(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long teamId,
            @Valid @RequestBody CreateProjectRequest request) {
        return Result.success("项目创建成功", projectService.createProject(teamId, userId, request));
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/api/v1/projects/{projectId}")
    public Result<ProjectDetailResponse> getProjectDetail(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long projectId) {
        return Result.success(projectService.getProjectDetail(projectId, userId));
    }

    @Operation(summary = "更新项目信息")
    @PutMapping("/api/v1/projects/{projectId}")
    public Result<Void> updateProject(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(projectId, userId, request);
        return Result.success("项目信息更新成功");
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/api/v1/projects/{projectId}")
    public Result<Void> deleteProject(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long projectId) {
        projectService.deleteProject(projectId, userId);
        return Result.success("项目已删除");
    }
}
