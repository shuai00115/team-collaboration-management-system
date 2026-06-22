package com.teamcollab.controller;

import com.teamcollab.common.response.Result;
import com.teamcollab.dto.stage.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.StageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "阶段管理", description = "项目阶段 CRUD 接口")
@RestController
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;

    // ==================== /api/v1/projects/{projectId}/stages ====================
    @Operation(summary = "获取项目阶段列表")
    @GetMapping("/api/v1/projects/{projectId}/stages")
    public Result<List<StageResponse>> getProjectStages(
            @CurrentUser Long userId,
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        return Result.success(stageService.getProjectStages(projectId, userId));
    }

    @Operation(summary = "创建阶段")
    @PostMapping("/api/v1/projects/{projectId}/stages")
    public Result<java.util.Map<String, Object>> createStage(
            @CurrentUser Long userId,
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid @RequestBody CreateStageRequest request) {
        return Result.success("阶段创建成功", stageService.createStage(projectId, userId, request));
    }

    @Operation(summary = "使用模板批量创建阶段")
    @PostMapping("/api/v1/projects/{projectId}/stages/template")
    public Result<List<java.util.Map<String, Object>>> applyTemplate(
            @CurrentUser Long userId,
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid @RequestBody ApplyTemplateRequest request) {
        List<java.util.Map<String, Object>> list = stageService.applyTemplate(projectId, userId, request);
        return Result.success("已按模板创建 " + list.size() + " 个阶段", list);
    }

    // ==================== /api/v1/stages ====================
    @Operation(summary = "编辑阶段信息")
    @PutMapping("/api/v1/stages/{stageId}")
    public Result<Void> updateStage(
            @CurrentUser Long userId,
            @Parameter(description = "阶段ID") @PathVariable Long stageId,
            @Valid @RequestBody UpdateStageRequest request) {
        stageService.updateStage(stageId, userId, request);
        return Result.success("阶段信息更新成功");
    }

    @Operation(summary = "删除阶段")
    @DeleteMapping("/api/v1/stages/{stageId}")
    public Result<Void> deleteStage(
            @CurrentUser Long userId,
            @Parameter(description = "阶段ID") @PathVariable Long stageId) {
        stageService.deleteStage(stageId, userId);
        return Result.success("阶段已删除");
    }

    @Operation(summary = "更新阶段状态")
    @PutMapping("/api/v1/stages/{stageId}/status")
    public Result<Void> updateStageStatus(
            @CurrentUser Long userId,
            @Parameter(description = "阶段ID") @PathVariable Long stageId,
            @Valid @RequestBody UpdateStageStatusRequest request) {
        stageService.updateStageStatus(stageId, userId, request);
        return Result.success("阶段状态已更新");
    }

    // ==================== /api/v1/stage-templates ====================
    @Operation(summary = "获取阶段模板列表")
    @GetMapping("/api/v1/stage-templates")
    public Result<List<StageTemplateResponse>> getStageTemplates() {
        return Result.success(stageService.getStageTemplates());
    }
}
