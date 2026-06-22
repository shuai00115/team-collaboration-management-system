package com.teamcollab.service.impl;

import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.stats.MemberStatsResponse;
import com.teamcollab.dto.stats.TeamStatsResponse;
import com.teamcollab.mapper.TaskMapper;
import com.teamcollab.mapper.TeamMapper;
import com.teamcollab.mapper.TeamMemberMapper;
import com.teamcollab.service.StatsService;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 * <p>
 * 提供团队维度和成员维度的任务进度统计与贡献分析功能。
 * 这是一个纯业务统计服务，不继承 MyBatis-Plus 的 ServiceImpl，
 * 因为它不直接操作单一实体。统计数据通过 TaskMapper 的聚合查询获取。
 * </p>
 *
 * <p><b>统计指标说明：</b></p>
 * <ul>
 *     <li><b>完成率</b> = 已完成任务数 / 总任务数（精确到小数点后3位）</li>
 *     <li><b>贡献度</b> = 该成员的已完成任务数 / 团队的总已完成任务数</li>
 *     <li><b>优先度分布</b> = 按高/中/低三个优先级统计各等级任务数量</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    /** 任务 Mapper，提供团队和成员维度的统计查询 */
    private final TaskMapper taskMapper;

    /** 团队 Mapper，用于获取团队基本信息 */
    private final TeamMapper teamMapper;

    /** 团队成员 Mapper */
    private final TeamMemberMapper teamMemberMapper;

    /** 团队服务，用于校验成员身份 */
    private final TeamService teamService;

    // ==================== 团队统计 ====================

    /**
     * 获取团队进度统计
     * <p>
     * 查询指定团队的整体任务统计概览，包括：
     * <ul>
     *     <li><b>概览（Overview）</b> —— 总任务数、已完成、进行中、待办、完成率</li>
     *     <li><b>优先级分布（PriorityBreakdown）</b> —— 高/中/低各优先级的总数和已完成数</li>
     *     <li><b>成员贡献（MemberStats）</b> —— 各成员的任务分配数、完成数、超期数、完成率、贡献度</li>
     * </ul>
     * 可按项目进行过滤，仅查看单个项目的统计。
     * </p>
     *
     * @param teamId    团队 ID
     * @param userId    当前操作用户 ID（需为团队成员）
     * @param projectId 项目 ID 过滤，可为 null 表示统计全部项目
     * @return 团队统计响应，包含整体进度和成员贡献分解
     * @throws BusinessException 当用户不是团队成员时
     */
    @Override
    public TeamStatsResponse getTeamStats(Long teamId, Long userId, Long projectId) {
        // 1. 校验团队成员权限
        teamService.checkIsMember(teamId, userId);

        // 2. 查询团队级别的任务统计
        Map<String, Object> overview = taskMapper.selectTeamTaskStats(teamId, projectId);
        if (overview == null || overview.isEmpty()) {
            log.warn("团队无统计数据: teamId={}, projectId={}", teamId, projectId);
            // 返回空统计
            TeamStatsResponse emptyResponse = new TeamStatsResponse();
            emptyResponse.setTeamId(teamId);
            emptyResponse.setOverview(new TeamStatsResponse.Overview());
            emptyResponse.setPriorityBreakdown(new TeamStatsResponse.PriorityBreakdown());
            emptyResponse.setMemberStats(List.of());
            return emptyResponse;
        }

        // 3. 构建概览数据
        TeamStatsResponse.Overview ov = new TeamStatsResponse.Overview();
        ov.setTotalTasks(toInt(overview.get("total_tasks")));
        ov.setCompletedTasks(toInt(overview.get("completed_tasks")));
        ov.setInProgressTasks(toInt(overview.get("in_progress_tasks")));
        ov.setTodoTasks(toInt(overview.get("todo_tasks")));
        ov.setCompletionRate(ov.getTotalTasks() > 0
                ? calcRate(ov.getCompletedTasks(), ov.getTotalTasks()) : 0.0);

        // 4. 构建优先级分布统计
        TeamStatsResponse.PriorityBreakdown pb = new TeamStatsResponse.PriorityBreakdown();
        pb.setHigh(buildPriorityStats(overview, "high"));
        pb.setMedium(buildPriorityStats(overview, "medium"));
        pb.setLow(buildPriorityStats(overview, "low"));

        // 5. 查询各成员的任务统计
        List<Map<String, Object>> memberRows = taskMapper.selectMemberStats(teamId, projectId);
        int teamCompleted = ov.getCompletedTasks();

        List<TeamStatsResponse.MemberStats> memberStatsList = memberRows.stream().map(row -> {
            TeamStatsResponse.MemberStats ms = new TeamStatsResponse.MemberStats();
            ms.setUserId(MapUtils.getLong(row, "user_id"));
            ms.setUsername((String) row.get("username"));
            ms.setAvatar((String) row.get("avatar"));
            ms.setAssignedTasks(toInt(row.get("assigned_tasks")));
            ms.setCompletedTasks(toInt(row.get("completed_tasks")));
            ms.setOverdueTasks(toInt(row.get("overdue_tasks")));

            // 个人完成率
            ms.setCompletionRate(ms.getAssignedTasks() > 0
                    ? calcRate(ms.getCompletedTasks(), ms.getAssignedTasks()) : 0.0);

            // 贡献度 = 该成员完成数 / 团队总完成数
            ms.setContribution(teamCompleted > 0
                    ? Math.round(ms.getCompletedTasks() * 1000.0 / teamCompleted) / 1000.0 : 0.0);

            return ms;
        }).collect(Collectors.toList());

        // 6. 组装完整的团队统计响应
        TeamStatsResponse response = new TeamStatsResponse();
        response.setTeamId(teamId);
        response.setTeamName((String) overview.getOrDefault("team_name", ""));
        response.setOverview(ov);
        response.setPriorityBreakdown(pb);
        response.setMemberStats(memberStatsList);

        log.info("团队统计查询完成: teamId={}, projectId={}, 总任务={}, 完成率={}",
                teamId, projectId, ov.getTotalTasks(), ov.getCompletionRate());
        return response;
    }

    // ==================== 成员统计 ====================

    /**
     * 获取个人成员贡献统计
     * <p>
     * 查询指定成员在团队中的个人任务贡献统计数据，包括：
     * <ul>
     *     <li>分配任务数、已完成数、进行中数、待办数、超期数</li>
     *     <li>个人完成率和团队贡献度</li>
     *     <li>按优先级分类的任务统计（高/中/低各自的分配数和完成数）</li>
     * </ul>
     * </p>
     *
     * @param teamId   团队 ID
     * @param memberId 目标成员用户 ID
     * @param userId   当前操作用户 ID（需为团队成员）
     * @return 成员统计响应，包含该成员的任务贡献指标
     * @throws BusinessException 当用户不是团队成员或成员无统计记录时
     */
    @Override
    public MemberStatsResponse getMemberStats(Long teamId, Long memberId, Long userId) {
        // 1. 校验团队成员权限
        teamService.checkIsMember(teamId, userId);

        // 2. 查询该成员的详细统计
        Map<String, Object> detail = taskMapper.selectMemberDetailStats(teamId, memberId);
        if (detail == null || detail.get("user_id") == null) {
            log.warn("未找到成员统计信息: teamId={}, memberId={}", teamId, memberId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该成员的统计信息");
        }

        // 3. 构建基本指标
        MemberStatsResponse response = new MemberStatsResponse();
        response.setUserId(MapUtils.getLong(detail, "user_id"));
        response.setUsername((String) detail.get("username"));
        response.setTeamId(teamId);

        int assigned = toInt(detail.get("assigned_tasks"));
        int completed = toInt(detail.get("completed_tasks"));
        int inProgress = toInt(detail.get("in_progress_tasks"));
        int todo = toInt(detail.get("todo_tasks"));
        int overdue = toInt(detail.get("overdue_tasks"));

        response.setAssignedTasks(assigned);
        response.setCompletedTasks(completed);
        response.setInProgressTasks(inProgress);
        response.setTodoTasks(todo);
        response.setOverdueTasks(overdue);

        // 4. 计算完成率
        response.setCompletionRate(assigned > 0
                ? calcRate(completed, assigned) : 0.0);

        // 5. 计算贡献度（需要团队总完成数作为分母）
        Map<String, Object> teamOverview = taskMapper.selectTeamTaskStats(teamId, null);
        int teamCompleted = teamOverview != null
                ? toInt(teamOverview.get("completed_tasks")) : 0;
        response.setContribution(teamCompleted > 0
                ? Math.round(completed * 1000.0 / teamCompleted) / 1000.0 : 0.0);

        // 6. 按优先级分类统计
        MemberStatsResponse.PriorityTasks pt = new MemberStatsResponse.PriorityTasks();
        pt.setHigh(buildPriorityCount(detail, "high"));
        pt.setMedium(buildPriorityCount(detail, "medium"));
        pt.setLow(buildPriorityCount(detail, "low"));
        response.setTasksByPriority(pt);

        // 7. 补充团队名称
        response.setTeamName((String) detail.getOrDefault("team_name", ""));

        log.info("成员统计查询完成: teamId={}, memberId={}, assigned={}, completed={}, completionRate={}",
                teamId, memberId, assigned, completed, response.getCompletionRate());
        return response;
    }

    // ==================== 辅助方法 ====================

    /**
     * 安全地将 Object 转换为 int（null 安全）
     *
     * @param val 待转换的值，可能为 null
     * @return 整数值，null 时返回 0
     */
    private int toInt(Object val) {
        if (val == null) {
            return 0;
        }
        return ((Number) val).intValue();
    }

    /**
     * 计算比率（part / total），结果精确到小数点后 3 位
     * <p>
     * 返回值如 0.625 表示 62.5%。
     * </p>
     *
     * @param part  部分值（分子）
     * @param total 总值（分母）
     * @return 比率值，范围 [0.0, 1.0]
     */
    private double calcRate(int part, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / total) / 1000.0;
    }

    /**
     * 从统计 Map 中提取指定优先级的统计数据
     * <p>
     * 从 Map 中读取形如 "{prefix}_total" 和 "{prefix}_completed" 的键值，
     * 构建 PriorityStats 对象。例如 prefix="high" 时读取 "high_total" 和 "high_completed"。
     * </p>
     *
     * @param data   统计数据 Map
     * @param prefix 前缀（high/medium/low）
     * @return 该优先级的统计数据
     */
    private TeamStatsResponse.PriorityStats buildPriorityStats(Map<String, Object> data, String prefix) {
        TeamStatsResponse.PriorityStats ps = new TeamStatsResponse.PriorityStats();
        ps.setTotal(toInt(data.get(prefix + "_total")));
        ps.setCompleted(toInt(data.get(prefix + "_completed")));
        return ps;
    }

    /**
     * 从统计 Map 中提取指定优先级的成员任务计数
     * <p>
     * 从 Map 中读取形如 "{prefix}_assigned" 和 "{prefix}_completed" 的键值。
     * </p>
     *
     * @param data   统计数据 Map
     * @param prefix 前缀（high/medium/low）
     * @return 该优先级的任务计数
     */
    private MemberStatsResponse.PriorityCount buildPriorityCount(Map<String, Object> data, String prefix) {
        MemberStatsResponse.PriorityCount pc = new MemberStatsResponse.PriorityCount();
        pc.setAssigned(toInt(data.get(prefix + "_assigned")));
        pc.setCompleted(toInt(data.get(prefix + "_completed")));
        return pc;
    }
}
