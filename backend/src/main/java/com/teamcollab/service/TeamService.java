package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.team.*;
import com.teamcollab.entity.Team;

import java.util.List;
import java.util.Map;

/**
 * 团队服务接口
 * <p>
 * 提供团队的创建、查询、更新、解散以及成员管理等功能。
 * 部分操作需要队长权限。
 * </p>
 *
 * @author teamcollab
 */
public interface TeamService extends IService<Team> {

    /**
     * 获取招募中团队列表（公开接口）
     * <p>
     * 分页查询当前正在招募成员的团队卡片信息，供所有用户浏览。
     * </p>
     *
     * @param query 团队查询请求，包含分页参数、排序和过滤条件
     * @return 分页的团队卡片响应列表
     */
    Page<TeamCardResponse> getRecruitingTeams(TeamQueryRequest query);

    /**
     * 创建团队
     * <p>
     * 创建新团队，创建者自动成为该团队的队长。
     * </p>
     *
     * @param creatorId 创建者用户ID
     * @param request   创建团队请求，包含团队名称、描述、所需技能等信息
     * @return 包含新创建团队基本信息的键值对（如团队ID、名称等）
     */
    Map<String, Object> createTeam(Long creatorId, CreateTeamRequest request);

    /**
     * 获取团队详情（公开接口）
     * <p>
     * 查询指定团队的详细信息，包含成员列表和所需技能标签。
     * </p>
     *
     * @param teamId 团队ID
     * @return 团队详情响应，包含基本信息、成员列表和技能需求
     */
    TeamDetailResponse getTeamDetail(Long teamId);

    /**
     * 更新团队信息
     * <p>
     * 修改团队的基本信息，仅队长有权执行此操作。
     * </p>
     *
     * @param teamId 团队ID
     * @param userId 当前操作用户ID（需为队长）
     * @param request 更新团队请求，包含要修改的团队字段
     */
    void updateTeam(Long teamId, Long userId, UpdateTeamRequest request);

    /**
     * 解散团队
     * <p>
     * 解散指定团队，仅队长有权执行此操作。
     * 需要传入团队名称进行二次确认，防止误操作。
     * </p>
     *
     * @param teamId  团队ID
     * @param userId  当前操作用户ID（需为队长）
     * @param confirm 确认字符串，需与团队名称一致
     */
    void disbandTeam(Long teamId, Long userId, String confirm);

    /**
     * 获取团队成员列表（公开接口）
     * <p>
     * 查询指定团队的所有成员信息。
     * </p>
     *
     * @param teamId 团队ID
     * @return 团队成员响应列表
     */
    List<TeamMemberResponse> getTeamMembers(Long teamId);

    /**
     * 移除团队成员
     * <p>
     * 将指定成员从团队中移除，仅队长有权执行此操作。
     * </p>
     *
     * @param teamId   团队ID
     * @param leaderId 队长用户ID
     * @param memberId 要移除的成员用户ID
     */
    void removeMember(Long teamId, Long leaderId, Long memberId);

    /**
     * 校验用户是否为团队队长
     * <p>
     * 检查指定用户是否是该团队的队长，如果不是则抛出权限异常。
     * </p>
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @throws com.teamcollab.exception.BusinessException 当用户不是队长时抛出
     */
    void checkIsLeader(Long teamId, Long userId);

    /**
     * 校验用户是否为团队成员
     * <p>
     * 检查指定用户是否已加入该团队，如果不是则抛出权限异常。
     * </p>
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @throws com.teamcollab.exception.BusinessException 当用户不是成员时抛出
     */
    void checkIsMember(Long teamId, Long userId);
}
