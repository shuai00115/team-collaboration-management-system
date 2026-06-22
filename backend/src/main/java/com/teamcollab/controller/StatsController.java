package com.teamcollab.controller;

import com.teamcollab.common.response.Result;
import com.teamcollab.dto.stats.MemberStatsResponse;
import com.teamcollab.dto.stats.TeamStatsResponse;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "统计分析", description = "团队进度与个人贡献度统计接口")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "获取团队进度统计")
    @GetMapping
    public Result<TeamStatsResponse> getTeamStats(
            @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "项目ID（可选，默认统计全部）") @RequestParam(required = false) Long projectId) {
        return Result.success(statsService.getTeamStats(teamId, userId, projectId));
    }

    @Operation(summary = "获取个人贡献度统计")
    @GetMapping("/members/{memberId}")
    public Result<MemberStatsResponse> getMemberStats(
            @CurrentUser Long userId,
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "成员用户ID") @PathVariable Long memberId) {
        return Result.success(statsService.getMemberStats(teamId, memberId, userId));
    }
}
