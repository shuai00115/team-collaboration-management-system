package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.team.*;
import com.teamcollab.entity.Team;
import com.teamcollab.entity.TeamMember;
import com.teamcollab.entity.TeamRequiredSkill;
import com.teamcollab.mapper.TeamMapper;
import com.teamcollab.mapper.TeamMemberMapper;
import com.teamcollab.mapper.TeamRequiredSkillMapper;
import com.teamcollab.service.NotificationService;
import com.teamcollab.common.util.MapUtils;
import com.teamcollab.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 团队服务实现类
 * <p>
 * 提供团队的完整生命周期管理，包括创建、查询、更新、解散、
 * 成员管理和权限校验等功能。这是系统中最核心的服务之一。
 * </p>
 *
 * <p><b>权限模型：</b></p>
 * <ul>
 *     <li><b>队长（leader）</b> —— 创建者自动成为队长，拥有团队的完全管理权</li>
 *     <li><b>成员（member）</b> —— 可查看团队信息、参与项目协作</li>
 *     <li><b>非成员</b> —— 仅可查看招募中的团队卡片信息，可提交入队申请</li>
 * </ul>
 *
 * <p><b>关键业务规则：</b></p>
 * <ul>
 *     <li>创建团队时创建者自动成为队长</li>
 *     <li>更新团队、解散团队、移除成员均需队长权限</li>
 *     <li>不能移除队长</li>
 *     <li>团队状态为 "recruiting" 时才接受新成员申请</li>
 *     <li>更新团队所需技能时采用全量替换策略（先删后插）</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    /** 团队 Mapper，提供团队相关的数据库操作 */
    private final TeamMapper teamMapper;

    /** 团队成员关联 Mapper */
    private final TeamMemberMapper teamMemberMapper;

    /** 团队所需技能关联 Mapper */
    private final TeamRequiredSkillMapper teamRequiredSkillMapper;

    /** 通知服务，用于向团队成员发送各类通知 */
    private final NotificationService notificationService;

    // ==================== 招募团队查询 ====================

    /**
     * 获取正在招募的团队列表（分页，支持多维筛选）
     * <p>
     * 调用 teamMapper.selectRecruitingTeams 执行分页查询，支持按团队状态、
     * 所需技能ID、关键词、成员数范围等条件进行过滤。
     * 对于每条查询结果，额外查询该团队的所需技能列表，组装完整的 TeamCardResponse。
     * </p>
     *
     * @param query 团队查询请求，包含分页参数和多重筛选条件
     * @return 分页的团队卡片响应列表，每张卡片包含团队摘要和所需技能
     */
    @Override
    public Page<TeamCardResponse> getRecruitingTeams(TeamQueryRequest query) {
        // 1. 执行分页查询
        Page<Team> queryPage = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Map<String, Object>> resultPage = teamMapper.selectRecruitingTeams(
                queryPage,
                query.getStatus(),
                query.getSkillId(),
                query.getKeyword(),
                query.getMinMembers(),
                query.getMaxMembers()
        );

        // 2. 逐条组装响应（含所需技能查询）
        List<TeamCardResponse> records = resultPage.getRecords().stream().map(row -> {
            Long teamId = MapUtils.getLong(row, "team_id");

            // 2.1 查询该团队的所需技能详情
            List<Map<String, Object>> skillRows = teamRequiredSkillMapper.selectByTeamId(teamId);
            List<TeamCardResponse.RequiredSkillVO> requiredSkills = skillRows.stream().map(sr -> {
                TeamCardResponse.RequiredSkillVO vo = new TeamCardResponse.RequiredSkillVO();
                vo.setSkillId(MapUtils.getLong(sr, "skill_id"));
                vo.setSkillName(MapUtils.getStr(sr, "skill_name"));
                vo.setCategory(MapUtils.getStr(sr, "category"));
                return vo;
            }).collect(Collectors.toList());

            // 2.2 构建团队卡片响应
            TeamCardResponse card = new TeamCardResponse();
            card.setTeamId(teamId);
            card.setName(MapUtils.getStr(row, "name"));
            card.setDescription(MapUtils.getStr(row, "description"));
            card.setStatus(MapUtils.getStr(row, "status"));
            card.setCurrentMembers(MapUtils.getInt(row, "current_members"));
            card.setMaxMembers(MapUtils.getInt(row, "max_members"));
            card.setCreatorName(MapUtils.getStr(row, "creator_name"));
            card.setRequiredSkills(requiredSkills);
            card.setCreatedAt(MapUtils.getDateTime(row, "created_at"));
            return card;
        }).collect(Collectors.toList());

        // 3. 构建分页响应
        Page<TeamCardResponse> responsePage = new Page<>(query.getPageNum(), query.getPageSize());
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询招募中团队列表: page={}/{}, 筛选条件[status={}, skillId={}, keyword={}], 结果总数={}",
                query.getPageNum(), query.getPageSize(), query.getStatus(),
                query.getSkillId(), query.getKeyword(), resultPage.getTotal());
        return responsePage;
    }

    // ==================== 团队创建 ====================

    /**
     * 创建新团队
     * <p>
     * 在事务中执行以下步骤：
     * <ol>
     *     <li>插入团队基本信息（名称、描述、最大成员数、创建者ID），默认状态为 "recruiting"</li>
     *     <li>将创建者添加为团队成员，角色设置为 "leader"（队长）</li>
     *     <li>如果请求中包含所需技能ID列表，批量插入 team_required_skills 关联记录</li>
     * </ol>
     * 任何一步失败都会触发事务回滚。
     * </p>
     *
     * @param creatorId 团队创建者（未来的队长）的用户 ID
     * @param request   创建团队请求，包含名称、描述、最大成员数、所需技能ID列表
     * @return 包含 teamId、name、status 的键值对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTeam(Long creatorId, CreateTeamRequest request) {
        log.info("开始创建团队: creatorId={}, name={}", creatorId, request.getName());

        // 1. 插入团队基本信息
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 10);
        team.setStatus("recruiting");        // 新建团队默认为招募中
        team.setCreatorId(creatorId);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        teamMapper.insert(team);
        log.info("团队基本信息入库: teamId={}", team.getTeamId());

        // 2. 将创建者添加为队长
        TeamMember leader = new TeamMember();
        leader.setTeamId(team.getTeamId());
        leader.setUserId(creatorId);
        leader.setRole("leader");            // 创建者角色为队长
        leader.setJoinedAt(LocalDateTime.now());
        teamMemberMapper.insert(leader);
        log.info("创建者已设置为队长: teamId={}, userId={}", team.getTeamId(), creatorId);

        // 3. 批量插入所需技能关联（如果提供了）
        List<Long> skillIds = request.getRequiredSkillIds();
        if (skillIds != null && !skillIds.isEmpty()) {
            for (Long skillId : skillIds) {
                TeamRequiredSkill teamSkill = new TeamRequiredSkill();
                teamSkill.setTeamId(team.getTeamId());
                teamSkill.setSkillId(skillId);
                teamRequiredSkillMapper.insert(teamSkill);
            }
            log.info("已添加 {} 个所需技能: teamId={}", skillIds.size(), team.getTeamId());
        }

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("teamId", team.getTeamId());
        result.put("name", team.getName());
        result.put("status", team.getStatus());

        log.info("团队创建成功: teamId={}, name={}", team.getTeamId(), team.getName());
        return result;
    }

    // ==================== 团队详情 ====================

    /**
     * 获取团队完整详情
     * <p>
     * 整合三个数据源：
     * <ul>
     *     <li>团队基本信息 —— teamMapper.selectTeamDetail（含创建者名称、成员数量）</li>
     *     <li>所需技能列表 —— teamRequiredSkillMapper.selectByTeamId</li>
     *     <li>成员列表 —— teamMapper.selectTeamMembers（队长排在首位）</li>
     * </ul>
     * </p>
     *
     * @param teamId 团队 ID
     * @return 包含完整信息的 TeamDetailResponse
     * @throws BusinessException 当团队不存在时（ErrorCode.TEAM_NOT_FOUND）
     */
    @Override
    public TeamDetailResponse getTeamDetail(Long teamId) {
        // 1. 查询团队基本信息
        Map<String, Object> detailRow = teamMapper.selectTeamDetail(teamId);
        if (detailRow == null || detailRow.get("team_id") == null) {
            log.warn("团队不存在: teamId={}", teamId);
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }

        // 2. 查询所需技能
        List<Map<String, Object>> skillRows = teamRequiredSkillMapper.selectByTeamId(teamId);
        List<TeamDetailResponse.RequiredSkillVO> requiredSkills = skillRows.stream().map(sr -> {
            TeamDetailResponse.RequiredSkillVO vo = new TeamDetailResponse.RequiredSkillVO();
            vo.setSkillId(MapUtils.getLong(sr, "skill_id"));
            vo.setSkillName(MapUtils.getStr(sr, "skill_name"));
            vo.setCategory(MapUtils.getStr(sr, "category"));
            return vo;
        }).collect(Collectors.toList());

        // 3. 查询成员列表
        List<Map<String, Object>> memberRows = teamMapper.selectTeamMembers(teamId);
        List<TeamDetailResponse.MemberVO> members = memberRows.stream().map(mr -> {
            TeamDetailResponse.MemberVO vo = new TeamDetailResponse.MemberVO();
            vo.setUserId(MapUtils.getLong(mr, "user_id"));
            vo.setUsername(MapUtils.getStr(mr, "username"));
            vo.setAvatar(MapUtils.getStr(mr, "avatar"));
            vo.setRole(MapUtils.getStr(mr, "role"));
            vo.setJoinedAt(MapUtils.getDateTime(mr, "joined_at"));
            return vo;
        }).collect(Collectors.toList());

        // 4. 组装完整响应
        TeamDetailResponse response = new TeamDetailResponse();
        response.setTeamId(MapUtils.getLong(detailRow, "team_id"));
        response.setName(MapUtils.getStr(detailRow, "name"));
        response.setDescription(MapUtils.getStr(detailRow, "description"));
        response.setStatus(MapUtils.getStr(detailRow, "status"));
        response.setCurrentMembers(MapUtils.getInt(detailRow, "current_members") != null
                ? MapUtils.getInt(detailRow, "current_members") : 0);
        response.setMaxMembers(MapUtils.getInt(detailRow, "max_members"));
        response.setCreatorId(MapUtils.getLong(detailRow, "creator_id"));
        response.setCreatorName(MapUtils.getStr(detailRow, "creator_name"));
        response.setRequiredSkills(requiredSkills);
        response.setMembers(members);
        response.setCreatedAt(MapUtils.getDateTime(detailRow, "created_at"));
        response.setUpdatedAt(MapUtils.getDateTime(detailRow, "updated_at"));

        log.info("查询团队详情: teamId={}, 成员数={}", teamId, members.size());
        return response;
    }

    // ==================== 团队更新 ====================

    /**
     * 更新团队信息（仅队长可操作）
     * <p>
     * 允许修改团队名称、描述、最大成员数和状态。
     * 如果请求中 requiredSkillIds 不为 null，则采用全量替换策略：
     * <ol>
     *     <li>先删除团队现有的所有所需技能关联</li>
     *     <li>再批量插入新的所需技能关联列表</li>
     * </ol>
     * 如果 requiredSkillIds 为 null，则不修改技能需求（保留原设置）。
     * </p>
     *
     * @param teamId  团队 ID
     * @param userId  操作者用户 ID（需为队长）
     * @param request 更新请求，所有字段均为可选
     * @throws BusinessException 当用户不是队长时（ErrorCode.NOT_TEAM_LEADER）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeam(Long teamId, Long userId, UpdateTeamRequest request) {
        // 1. 队长权限校验
        checkIsLeader(teamId, userId);

        // 2. 更新团队基本信息（仅更新非 null 字段）
        Team team = new Team();
        team.setTeamId(teamId);
        if (request.getName() != null) {
            team.setName(request.getName());
        }
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }
        if (request.getMaxMembers() != null) {
            team.setMaxMembers(request.getMaxMembers());
        }
        if (request.getStatus() != null) {
            team.setStatus(request.getStatus());
        }
        team.setUpdatedAt(LocalDateTime.now());
        this.updateById(team);

        // 3. 更新所需技能（全量替换：先删后插）
        List<Long> skillIds = request.getRequiredSkillIds();
        if (skillIds != null) {
            teamRequiredSkillMapper.deleteByTeamId(teamId);
            if (!skillIds.isEmpty()) {
                for (Long skillId : skillIds) {
                    TeamRequiredSkill teamSkill = new TeamRequiredSkill();
                    teamSkill.setTeamId(teamId);
                    teamSkill.setSkillId(skillId);
                    teamRequiredSkillMapper.insert(teamSkill);
                }
                log.info("团队所需技能已更新: teamId={}, 技能数={}", teamId, skillIds.size());
            } else {
                log.info("团队所需技能已清空: teamId={}", teamId);
            }
        }

        log.info("团队信息更新成功: teamId={}, operatorId={}", teamId, userId);
    }

    // ==================== 团队解散 ====================

    /**
     * 解散团队（仅队长可操作，需名称确认）
     * <p>
     * 为防止误操作，需要传入团队名称作为确认参数。
     * 只有当 confirm 字符串与数据库中的团队名称完全一致时，才执行解散。
     * 解散操作通过 MyBatis-Plus 的 removeById 执行，会级联删除
     * 团队成员、所需技能、项目、任务等关联数据（依赖数据库外键级联）。
     * </p>
     *
     * @param teamId  团队 ID
     * @param userId  操作者用户 ID（需为队长）
     * @param confirm 团队名称确认字符串，必须与数据库中的团队名称一致
     * @throws BusinessException 当用户不是队长或 confirm 不匹配时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disbandTeam(Long teamId, Long userId, String confirm) {
        // 1. 队长权限校验
        checkIsLeader(teamId, userId);

        // 2. 获取团队信息并校验名称确认
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        if (confirm == null || !confirm.equals(team.getName())) {
            log.warn("解散团队被拒绝 - 名称确认不匹配: teamId={}, input={}, actual={}",
                    teamId, confirm, team.getName());
            throw new BusinessException(ErrorCode.BAD_REQUEST, "团队名称确认不匹配，解散操作已取消");
        }

        // 3. 执行解散（级联删除关联数据）
        this.removeById(teamId);
        log.info("团队已解散: teamId={}, name={}, operatorId={}", teamId, team.getName(), userId);
    }

    // ==================== 成员管理 ====================

    /**
     * 获取团队成员列表
     * <p>
     * 调用 teamMapper.selectTeamMembers 查询所有团队成员，
     * 包含用户ID、用户名、头像、角色和加入时间，队长排在首位。
     * </p>
     *
     * @param teamId 团队 ID
     * @return 团队成员响应列表
     */
    @Override
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        List<Map<String, Object>> memberRows = teamMapper.selectTeamMembers(teamId);
        List<TeamMemberResponse> members = memberRows.stream().map(row -> {
            TeamMemberResponse response = new TeamMemberResponse();
            response.setUserId(MapUtils.getLong(row, "user_id"));
            response.setUsername(MapUtils.getStr(row, "username"));
            response.setAvatar(MapUtils.getStr(row, "avatar"));
            response.setRole(MapUtils.getStr(row, "role"));
            response.setJoinedAt(MapUtils.getDateTime(row, "joined_at"));
            return response;
        }).collect(Collectors.toList());

        log.info("查询团队成员: teamId={}, 成员数={}", teamId, members.size());
        return members;
    }

    /**
     * 移除团队成员（仅队长可操作）
     * <p>
     * 移除规则：
     * <ul>
     *     <li>操作者必须是队长</li>
     *     <li>不能移除队长自身</li>
     *     <li>被移除者必须是团队现有成员</li>
     *     <li>移除成功后向被移除者发送通知</li>
     * </ul>
     * </p>
     *
     * @param teamId   团队 ID
     * @param leaderId 操作者（队长）的用户 ID
     * @param memberId 被移除成员的用户 ID
     * @throws BusinessException 当操作者不是队长（ErrorCode.NOT_TEAM_LEADER）
     *                           或试图移除队长（ErrorCode.CANNOT_REMOVE_LEADER）
     *                           或目标不是团队成员时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long teamId, Long leaderId, Long memberId) {
        // 1. 校验操作者是队长
        checkIsLeader(teamId, leaderId);

        // 2. 不能移除队长本人
        if (leaderId.equals(memberId)) {
            log.warn("移除成员被拒绝 - 不能移除队长: teamId={}, leaderId={}", teamId, leaderId);
            throw new BusinessException(ErrorCode.CANNOT_REMOVE_LEADER);
        }

        // 3. 校验目标是团队成员
        TeamMember target = teamMemberMapper.selectByTeamAndUser(teamId, memberId);
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "该用户不是团队成员");
        }
        // 额外检查：目标不能是队长（防止数据异常）
        if ("leader".equals(target.getRole())) {
            throw new BusinessException(ErrorCode.CANNOT_REMOVE_LEADER);
        }

        // 4. 执行移除操作
        teamMemberMapper.deleteByTeamAndUser(teamId, memberId);

        // 5. 向被移除的成员发送通知
        Team team = this.getById(teamId);
        String teamName = team != null ? team.getName() : "未知团队";
        notificationService.sendNotification(
                memberId,
                "team",
                "您已被移出团队",
                "您已被移出团队「" + teamName + "」",
                "team",
                teamId
        );

        log.info("成员已被移除: teamId={}, memberId={}, operatorId={}", teamId, memberId, leaderId);
    }

    // ==================== 权限校验 ====================

    /**
     * 校验用户是否为指定团队的队长
     * <p>
     * 通过 teamMemberMapper.selectByTeamAndUser 查询成员记录，
     * 检查其角色是否为 "leader"。如果不是队长，抛出 BusinessException。
     * 此方法作为其他管理操作的前置权限检查。
     * </p>
     *
     * @param teamId 团队 ID
     * @param userId 要校验的用户 ID
     * @throws BusinessException 当用户不是队长时（ErrorCode.NOT_TEAM_LEADER）
     */
    @Override
    public void checkIsLeader(Long teamId, Long userId) {
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        if (member == null || !"leader".equals(member.getRole())) {
            log.warn("权限校验失败 - 非队长操作: teamId={}, userId={}, role={}",
                    teamId, userId, member != null ? member.getRole() : "null");
            throw new BusinessException(ErrorCode.NOT_TEAM_LEADER);
        }
    }

    /**
     * 校验用户是否为指定团队的成员
     * <p>
     * 通过 teamMemberMapper.existsByTeamAndUser 检查成员关联是否存在。
     * 如果查询结果为 0，说明用户不是该团队成员，抛出 BusinessException。
     * </p>
     *
     * @param teamId 团队 ID
     * @param userId 要校验的用户 ID
     * @throws BusinessException 当用户不是团队成员时（ErrorCode.NOT_TEAM_MEMBER）
     */
    @Override
    public void checkIsMember(Long teamId, Long userId) {
        if (teamMemberMapper.existsByTeamAndUser(teamId, userId) == 0) {
            log.warn("权限校验失败 - 非团队成员: teamId={}, userId={}", teamId, userId);
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
    }
}
