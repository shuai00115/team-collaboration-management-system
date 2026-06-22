package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.team.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "团队管理", description = "团队创建、查询、编辑、解散及成员管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "获取招募中的团队")
    @GetMapping
    public Result<PageResult<TeamCardResponse>> getRecruitingTeams(@Valid TeamQueryRequest query) {
        Page<TeamCardResponse> page = teamService.getRecruitingTeams(query);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "创建团队")
    @PostMapping
    public Result<Map<String, Object>> createTeam(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody CreateTeamRequest request) {
        return Result.success("团队创建成功", teamService.createTeam(userId, request));
    }

    @Operation(summary = "获取团队详情")
    @GetMapping("/{teamId}")
    public Result<TeamDetailResponse> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamDetail(teamId));
    }

    @Operation(summary = "更新团队信息")
    @PutMapping("/{teamId}")
    public Result<Void> updateTeam(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long teamId,
            @Valid @RequestBody UpdateTeamRequest request) {
        teamService.updateTeam(teamId, userId, request);
        return Result.success("团队信息更新成功");
    }

    @Operation(summary = "解散团队")
    @DeleteMapping("/{teamId}")
    public Result<Void> disbandTeam(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long teamId,
            @RequestParam(required = false) String confirm) {
        teamService.disbandTeam(teamId, userId, confirm);
        return Result.success("团队已解散");
    }

    @Operation(summary = "获取团队成员")
    @GetMapping("/{teamId}/members")
    public Result<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamMembers(teamId));
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/{teamId}/members/{memberId}")
    public Result<Void> removeMember(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        teamService.removeMember(teamId, userId, memberId);
        return Result.success("成员已移除");
    }
}
