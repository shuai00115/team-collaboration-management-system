package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.application.ApplicationResponse;
import com.teamcollab.dto.application.ApplyRequest;
import com.teamcollab.dto.application.ReviewRequest;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "申请管理", description = "加入团队申请与审批接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/teams/{teamId}/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "提交加入申请", description = "当前用户向指定团队提交加入申请")
    @PostMapping
    public Result<Map<String, Object>> apply(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Valid @RequestBody ApplyRequest request) {
        Map<String, Object> result = applicationService.apply(teamId, userId, request.getMessage());
        return Result.success("申请已提交，请等待队长审核", result);
    }

    @Operation(summary = "获取申请列表", description = "队长分页查看团队收到的加入申请")
    @GetMapping
    public Result<PageResult<ApplicationResponse>> getApplications(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "申请状态筛选") @RequestParam(required = false) String status) {
        Page<ApplicationResponse> page = applicationService.getApplications(teamId, userId, pageNum, pageSize, status);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "审批申请", description = "队长审批指定申请（通过或拒绝）")
    @PutMapping("/{requestId}")
    public Result<Void> reviewApplication(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "申请ID") @PathVariable Long requestId,
            @Valid @RequestBody ReviewRequest request) {
        applicationService.reviewApplication(teamId, requestId, userId, request.getAction());
        if ("approve".equals(request.getAction())) {
            return Result.success("申请已通过，申请人已加入团队");
        } else {
            return Result.success("申请已拒绝");
        }
    }
}
