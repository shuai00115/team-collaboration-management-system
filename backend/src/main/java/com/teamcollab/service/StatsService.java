package com.teamcollab.service;

import com.teamcollab.dto.stats.MemberStatsResponse;
import com.teamcollab.dto.stats.TeamStatsResponse;

/**
 * 统计服务接口
 * <p>
 * 提供团队维度和成员维度的进度统计与贡献分析功能。
 * 不继承IService，为纯业务统计接口。
 * </p>
 *
 * @author teamcollab
 */
public interface StatsService {

    /**
     * 获取团队进度统计
     * <p>
     * 查询指定团队的整体进度统计数据，包含各成员的任务完成情况分解。
     * 可按项目进行过滤，仅查看单个项目的统计。
     * </p>
     *
     * @param teamId    团队ID
     * @param userId    当前操作用户ID
     * @param projectId 项目ID过滤，可为null表示统计全部项目
     * @return 团队统计响应，包含整体进度和成员贡献分解
     */
    TeamStatsResponse getTeamStats(Long teamId, Long userId, Long projectId);

    /**
     * 获取个人成员贡献统计
     * <p>
     * 查询指定成员在团队中的个人贡献统计数据，包括任务完成数、完成率等指标。
     * </p>
     *
     * @param teamId   团队ID
     * @param memberId 目标成员用户ID
     * @param userId   当前操作用户ID
     * @return 成员统计响应，包含该成员的任务贡献指标
     */
    MemberStatsResponse getMemberStats(Long teamId, Long memberId, Long userId);
}
