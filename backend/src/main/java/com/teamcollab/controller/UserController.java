package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.user.AddSkillRequest;
import com.teamcollab.dto.user.ChangePasswordRequest;
import com.teamcollab.dto.user.MyApplicationResponse;
import com.teamcollab.dto.user.MyTaskResponse;
import com.teamcollab.dto.user.MyTeamResponse;
import com.teamcollab.dto.user.UpdateProfileRequest;
import com.teamcollab.dto.user.UserInfoResponse;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "当前用户个人信息、团队、任务、申请及技能管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUserInfo() {
        UserInfoResponse response = userService.getCurrentUserInfo();
        return Result.success(response);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/me")
    public Result<Void> updateProfile(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userId, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.success("密码修改成功");
    }

    @Operation(summary = "获取我的团队")
    @GetMapping("/me/teams")
    public Result<PageResult<MyTeamResponse>> getMyTeams(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<MyTeamResponse> page = userService.getMyTeams(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "获取我的任务")
    @GetMapping("/me/tasks")
    public Result<PageResult<MyTaskResponse>> getMyTasks(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long listId,
            @RequestParam(required = false) Long teamId) {
        Page<MyTaskResponse> page = userService.getMyTasks(userId, pageNum, pageSize, priority, listId, teamId);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "获取我的申请")
    @GetMapping("/me/applications")
    public Result<PageResult<MyApplicationResponse>> getMyApplications(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Page<MyApplicationResponse> page = userService.getMyApplications(userId, pageNum, pageSize, status);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "添加技能")
    @PostMapping("/me/skills")
    public Result<Void> addSkill(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody AddSkillRequest request) {
        userService.addSkill(userId, request.getSkillId(), request.getLevel());
        return Result.success("技能添加成功");
    }

    @Operation(summary = "删除技能")
    @DeleteMapping("/me/skills/{skillId}")
    public Result<Void> deleteSkill(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Parameter(description = "技能ID") @PathVariable Long skillId) {
        userService.deleteSkill(userId, skillId);
        return Result.success("技能删除成功");
    }
}
