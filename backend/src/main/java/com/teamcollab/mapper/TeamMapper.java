package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 团队 Mapper 接口
 * <p>
 * 提供团队相关的数据库操作，包括团队招募搜索、团队详情查询、成员列表查询、
 * 行级锁查询、管理员视图查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    /**
     * 分页查询正在招募的团队列表（支持多条件筛选）
     *
     * @param page       分页对象
     * @param status     团队状态
     * @param skillId    所需技能 ID（筛选需要该技能的团队）
     * @param keyword    搜索关键字（匹配团队名称）
     * @param minMembers 最小成员数
     * @param maxMembers 最大成员数
     * @return 分页团队列表（含成员数量、所需技能等汇总信息）
     */
    Page<Map<String, Object>> selectRecruitingTeams(Page<Team> page, @Param("status") String status,
        @Param("skillId") Long skillId, @Param("keyword") String keyword,
        @Param("minMembers") Integer minMembers, @Param("maxMembers") Integer maxMembers);

    /**
     * 查询团队详细信息（含创建者名称和当前成员数量）
     *
     * @param teamId 团队 ID
     * @return 团队详情 Map
     */
    Map<String, Object> selectTeamDetail(@Param("teamId") Long teamId);

    /**
     * 查询团队成员列表（含用户头像、角色等信息，leader 排在首位）
     *
     * @param teamId 团队 ID
     * @return 成员列表
     */
    List<Map<String, Object>> selectTeamMembers(@Param("teamId") Long teamId);

    /**
     * 行级锁查询团队（用于并发控制，如更新团队信息前先锁定）
     *
     * @param teamId 团队 ID
     * @return 团队实体
     */
    Team selectForUpdate(@Param("teamId") Long teamId);

    /**
     * 管理员分页查询团队列表（支持关键字搜索和状态筛选）
     *
     * @param page    分页对象
     * @param keyword 搜索关键字
     * @param status  团队状态
     * @return 分页团队列表
     */
    Page<Map<String, Object>> selectAdminTeamPage(Page<Team> page, @Param("keyword") String keyword, @Param("status") String status);

    /**
     * 查询用户加入的所有团队
     *
     * @param userId 用户 ID
     * @return 团队列表
     */
    List<Map<String, Object>> selectUserTeams(@Param("userId") Long userId);

    /**
     * 统计团队成员数量
     *
     * @param teamId 团队 ID
     * @return 成员数量
     */
    int countMembers(@Param("teamId") Long teamId);
}
