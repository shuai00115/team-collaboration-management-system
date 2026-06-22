package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.admin.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "管理员", description = "管理员系统管理接口（需 admin 角色）")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<PageResult<AdminUserResponse>> getUserList(
            @CurrentUser Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "用户名/邮箱模糊搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role) {
        Page<AdminUserResponse> page = adminService.getUserList(pageNum, pageSize, keyword, role);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{targetUserId}")
    public Result<Void> updateUserStatus(
            @CurrentUser Long userId,
            @Parameter(description = "目标用户ID") @PathVariable Long targetUserId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        adminService.updateUserStatus(targetUserId, request);
        return Result.success("用户状态已更新");
    }

    @Operation(summary = "获取所有团队列表")
    @GetMapping("/teams")
    public Result<PageResult<AdminTeamResponse>> getTeamList(
            @CurrentUser Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "团队名称模糊搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        Page<AdminTeamResponse> page = adminService.getTeamList(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "强制解散团队")
    @DeleteMapping("/teams/{teamId}")
    public Result<Void> forceDisbandTeam(
            @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId) {
        adminService.forceDisbandTeam(teamId);
        return Result.success("团队已被强制解散");
    }

    @Operation(summary = "添加技能标签")
    @PostMapping("/skills")
    public Result<Map<String, Object>> addSkill(
            @CurrentUser Long userId,
            @Valid @RequestBody CreateSkillRequest request) {
        return Result.success("技能标签添加成功", adminService.addSkill(request));
    }

    @Operation(summary = "删除技能标签")
    @DeleteMapping("/skills/{skillId}")
    public Result<Void> deleteSkill(
            @CurrentUser Long userId,
            @Parameter(description = "技能ID") @PathVariable Long skillId) {
        adminService.deleteSkill(skillId);
        return Result.success("技能标签已删除");
    }
}
