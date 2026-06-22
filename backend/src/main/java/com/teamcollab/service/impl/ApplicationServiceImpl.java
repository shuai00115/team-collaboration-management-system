package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.application.ApplicationResponse;
import com.teamcollab.entity.JoinRequest;
import com.teamcollab.entity.Team;
import com.teamcollab.entity.TeamMember;
import com.teamcollab.mapper.JoinRequestMapper;
import com.teamcollab.mapper.TeamMapper;
import com.teamcollab.mapper.TeamMemberMapper;
import com.teamcollab.mapper.UserSkillMapper;
import com.teamcollab.service.ApplicationService;
import com.teamcollab.service.NotificationService;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 入队申请服务实现类
 * <p>
 * 提供用户加入团队的申请提交、队长审核申请、申请状态查询等功能。
 * 审核操作使用数据库行级锁（SELECT FOR UPDATE）确保并发安全，
 * 防止超募（团队满员后仍有申请被批准）。
 * </p>
 *
 * <p><b>并发控制策略：</b></p>
 * <ul>
 *     <li>审核时先对 join_requests 加行锁，校验申请状态为 pending</li>
 *     <li>批准时对 teams 加行锁，校验当前成员数未超过上限</li>
 *     <li>插入 team_members 后再次检查是否满员，满员则自动关闭招募</li>
 *     <li>整个审核流程在一个事务中执行，任何一步失败均回滚</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends ServiceImpl<JoinRequestMapper, JoinRequest> implements ApplicationService {

    /** 入队申请 Mapper */
    private final JoinRequestMapper joinRequestMapper;

    /** 团队 Mapper */
    private final TeamMapper teamMapper;

    /** 团队成员关联 Mapper */
    private final TeamMemberMapper teamMemberMapper;

    /** 用户技能关联 Mapper，查询申请人的技能列表 */
    private final UserSkillMapper userSkillMapper;

    /** 团队服务，用于权限校验 */
    private final TeamService teamService;

    /** 通知服务，用于发送审核结果通知 */
    private final NotificationService notificationService;

    // ==================== 提交申请 ====================

    /**
     * 提交入队申请
     * <p>
     * 完整业务校验链：
     * <ol>
     *     <li>校验团队存在且处于招募中状态</li>
     *     <li>校验申请人不是团队成员（防止重复入队）</li>
     *     <li>校验团队未满员</li>
     *     <li>校验不存在待处理的重复申请</li>
     *     <li>插入申请记录并通知队长</li>
     * </ol>
     * </p>
     *
     * @param teamId  目标团队 ID
     * @param userId  申请人用户 ID
     * @param message 申请留言（可选）
     * @return 包含 requestId 的键值对
     * @throws BusinessException 团队不存在、团队已关闭招募、已是成员、
     *                           团队已满员、已有待处理申请时分别抛出对应异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> apply(Long teamId, Long userId, String message) {
        log.info("提交入队申请: teamId={}, userId={}", teamId, userId);

        // 1. 校验团队存在且处于招募中
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        if ("closed".equals(team.getStatus())) {
            log.warn("申请被拒绝 - 团队已关闭招募: teamId={}, userId={}", teamId, userId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该团队已关闭招募");
        }

        // 2. 校验申请人不是团队成员
        if (teamMemberMapper.existsByTeamAndUser(teamId, userId) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_MEMBER);
        }

        // 3. 校验团队未满员
        int currentMemberCount = teamMemberMapper.countByTeamId(teamId);
        if (currentMemberCount >= team.getMaxMembers()) {
            log.warn("申请被拒绝 - 团队已满员: teamId={}, userId={}, 当前成员={}/{}",
                    teamId, userId, currentMemberCount, team.getMaxMembers());
            throw new BusinessException(ErrorCode.TEAM_FULL);
        }

        // 4. 校验不存在待处理的重复申请
        if (joinRequestMapper.countByUserAndTeam(userId, teamId) > 0) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        // 5. 插入申请记录
        JoinRequest request = new JoinRequest();
        request.setTeamId(teamId);
        request.setUserId(userId);
        request.setMessage(message);
        request.setStatus("pending");       // 初始状态为待审核
        request.setCreatedAt(LocalDateTime.now());
        joinRequestMapper.insert(request);

        // 6. 通知团队队长（有新申请需要审核）
        // relatedId 传 teamId，前端点击通知跳转到 /teams/{teamId}
        notificationService.sendToTeamLeader(
                teamId,
                "application",
                "收到新的入队申请",
                "有用户申请加入您的团队「" + team.getName() + "」",
                "join_request",
                teamId,
                null  // projectId: 入队申请不关联项目
        );

        log.info("入队申请提交成功: requestId={}, teamId={}, userId={}",
                request.getRequestId(), teamId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("requestId", request.getRequestId());
        result.put("status", request.getStatus());
        return result;
    }

    // ==================== 查询申请列表 ====================

    /**
     * 获取团队的入队申请列表（分页，仅队长可查看）
     * <p>
     * 调用 joinRequestMapper.selectByTeamId 查询申请列表，
     * 支持按申请状态（pending/approved/rejected）进行过滤。
     * 对每条申请额外查询申请人的技能列表，帮助队长做出审核决策。
     * </p>
     *
     * @param teamId   团队 ID
     * @param userId   操作者用户 ID（需为队长）
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param status   申请状态筛选（pending/approved/rejected），可为 null
     * @return 分页的申请响应列表，包含申请人技能信息
     * @throws BusinessException 当用户不是队长时
     */
    @Override
    public Page<ApplicationResponse> getApplications(Long teamId, Long userId, int pageNum,
                                                      int pageSize, String status) {
        // 1. 队长权限校验
        teamService.checkIsLeader(teamId, userId);

        // 2. 分页查询申请列表
        Page<JoinRequest> queryPage = new Page<>(pageNum, pageSize);
        Page<Map<String, Object>> resultPage = joinRequestMapper.selectByTeamId(
                queryPage, teamId, status);

        // 3. 逐条组装响应（含申请人技能查询）
        List<ApplicationResponse> records = resultPage.getRecords().stream().map(row -> {
            ApplicationResponse response = new ApplicationResponse();
            response.setRequestId(MapUtils.getLong(row, "request_id"));
            response.setUserId(MapUtils.getLong(row, "user_id"));
            response.setUsername((String) row.get("username"));
            response.setAvatar((String) row.get("avatar"));
            response.setMessage((String) row.get("message"));
            response.setStatus((String) row.get("status"));
            if (row.get("created_at") != null) {
                response.setCreatedAt((LocalDateTime) row.get("created_at"));
            }
            if (row.get("reviewed_at") != null) {
                response.setReviewedAt((LocalDateTime) row.get("reviewed_at"));
            }

            // 查询申请人的技能列表
            Long applicantId = MapUtils.getLong(row, "user_id");
            List<Map<String, Object>> skillRows = userSkillMapper.selectUserSkills(applicantId);
            List<ApplicationResponse.UserSkillVO> userSkills = skillRows.stream().map(s -> {
                ApplicationResponse.UserSkillVO vo = new ApplicationResponse.UserSkillVO();
                vo.setSkillId(MapUtils.getLong(s, "skill_id"));
                vo.setSkillName((String) s.get("skill_name"));
                vo.setLevel((String) s.get("level"));
                return vo;
            }).collect(Collectors.toList());
            response.setUserSkills(userSkills);

            return response;
        }).collect(Collectors.toList());

        // 4. 构建分页响应
        Page<ApplicationResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询入队申请列表: teamId={}, status={}, 总数={}", teamId, status, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 审核申请 ====================

    /**
     * 审核入队申请（批准或拒绝）
     * <p>
     * 核心业务流程（含并发控制）：
     * <ol>
     *     <li>队长权限校验</li>
     *     <li>行级锁查询申请记录（SELECT FOR UPDATE），防止并发审批</li>
     *     <li>校验申请属于该团队且状态为 pending</li>
     *     <li><b>批准分支：</b>
     *         <ul>
     *             <li>行级锁查询团队信息（防止超募）</li>
     *             <li>再次校验团队未满员</li>
     *             <li>插入团队成员记录，角色为 member</li>
     *             <li>如果加入后满员，自动将团队状态改为 closed</li>
     *             <li>更新申请状态为 approved，记录审核时间</li>
     *             <li>向申请人发送"申请已通过"通知</li>
     *         </ul>
     *     </li>
     *     <li><b>拒绝分支：</b>
     *         <ul>
     *             <li>更新申请状态为 rejected，记录审核时间</li>
     *             <li>向申请人发送"申请已拒绝"通知</li>
     *         </ul>
     *     </li>
     * </ol>
     * </p>
     *
     * @param teamId    团队 ID
     * @param requestId 申请记录 ID
     * @param userId    操作者用户 ID（需为队长）
     * @param action    审核动作："approve"（批准）或 "reject"（拒绝）
     * @throws BusinessException 多种业务异常场景
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long teamId, Long requestId, Long userId, String action) {
        log.info("审核入队申请: requestId={}, action={}, operatorId={}", requestId, action, userId);

        // 1. 队长权限校验
        teamService.checkIsLeader(teamId, userId);

        // 2. 行级锁查询申请记录（SELECT FOR UPDATE，防止并发审批同一申请）
        JoinRequest request = joinRequestMapper.selectForUpdate(requestId);
        if (request == null || !request.getTeamId().equals(teamId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请记录不存在");
        }

        // 3. 校验申请状态为 pending（防止重复审批）
        if (!"pending".equals(request.getStatus())) {
            log.warn("审核被拒绝 - 申请已处理: requestId={}, currentStatus={}",
                    requestId, request.getStatus());
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        // 4. 设置审核时间
        request.setReviewedAt(LocalDateTime.now());

        // 5. 根据审核动作执行分支逻辑
        if ("approve".equals(action)) {
            // ===== 批准分支 =====

            // 5.1 行级锁查询团队（防止并发审批导致超募）
            Team team = teamMapper.selectForUpdate(teamId);
            int currentMemberCount = teamMemberMapper.countByTeamId(teamId);

            // 5.2 再次校验未满员（二次确认，防止 TOCTOU 问题）
            if (currentMemberCount >= team.getMaxMembers()) {
                log.warn("审批被拒绝 - 团队已满员: teamId={}, 当前成员={}/{}",
                        teamId, currentMemberCount, team.getMaxMembers());
                throw new BusinessException(ErrorCode.TEAM_FULL);
            }

            // 5.3 更新申请状态为已通过
            request.setStatus("approved");
            joinRequestMapper.updateById(request);

            // 5.4 添加为团队成员
            TeamMember member = new TeamMember();
            member.setTeamId(teamId);
            member.setUserId(request.getUserId());
            member.setRole("member");      // 新成员角色为普通成员
            member.setJoinedAt(LocalDateTime.now());
            teamMemberMapper.insert(member);

            // 5.5 加入后若满员，自动关闭团队招募
            if (currentMemberCount + 1 >= team.getMaxMembers()) {
                Team updateTeam = new Team();
                updateTeam.setTeamId(teamId);
                updateTeam.setStatus("closed");
                teamMapper.updateById(updateTeam);
                log.info("团队满员，自动关闭招募: teamId={}, 成员数={}", teamId, currentMemberCount + 1);
            }

            // 5.6 向申请人发送通过通知
            notificationService.sendNotification(
                    request.getUserId(),
                    "application",
                    "入队申请已通过",
                    "恭喜！您申请加入团队「" + team.getName() + "」的请求已通过审核，欢迎加入！",
                    "team",
                    teamId,
                    null  // projectId: 入队申请不关联项目
            );

            log.info("入队申请已批准: requestId={}, applicantId={}, teamId={}",
                    requestId, request.getUserId(), teamId);

        } else if ("reject".equals(action)) {
            // ===== 拒绝分支 =====

            // 更新申请状态为已拒绝
            request.setStatus("rejected");
            joinRequestMapper.updateById(request);

            // 向申请人发送拒绝通知
            Team team = teamMapper.selectById(teamId);  // 仅读取，不需要行锁
            String teamName = team != null ? team.getName() : "未知团队";
            notificationService.sendNotification(
                    request.getUserId(),
                    "application",
                    "入队申请已被拒绝",
                    "很遗憾，您申请加入团队「" + teamName + "」的请求已被拒绝。",
                    "team",
                    teamId,
                    null  // projectId: 入队申请不关联项目
            );

            log.info("入队申请已拒绝: requestId={}, applicantId={}", requestId, request.getUserId());

        } else {
            // 无效的审核动作
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的审核动作，仅支持 approve 或 reject");
        }
    }

    // ==================== 申请状态查询 ====================

    /**
     * 检查用户是否已有待处理的入队申请
     * <p>
     * 通过 joinRequestMapper.countByUserAndTeam 查询该用户向指定团队
     * 提交的、状态为 pending 的申请数量。用于防止重复提交申请。
     * </p>
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     * @return true 表示存在待处理的申请，false 表示不存在
     */
    @Override
    public boolean hasPendingApplication(Long teamId, Long userId) {
        int count = joinRequestMapper.countByUserAndTeam(userId, teamId);
        return count > 0;
    }
}
