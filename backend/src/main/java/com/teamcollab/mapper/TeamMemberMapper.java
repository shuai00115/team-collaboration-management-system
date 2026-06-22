package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 团队成员 Mapper 接口
 * <p>
 * 提供团队成员关联关系的数据库操作，包括成员统计、成员查询、成员删除、
 * 成员存在性检查、分页成员列表查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    /**
     * 统计指定团队的成员数量
     *
     * @param teamId 团队 ID
     * @return 成员数量
     */
    int countByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据团队 ID 和用户 ID 查询成员记录
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     * @return 团队成员实体，未找到返回 null
     */
    TeamMember selectByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 删除指定团队中的指定成员
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     * @return 受影响的行数
     */
    int deleteByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 检查用户是否已是团队成员
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     * @return 记录数（0 表示不是成员）
     */
    int existsByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 分页查询团队成员列表（含用户头像、用户名等信息）
     *
     * @param page   分页对象
     * @param teamId 团队 ID
     * @return 分页成员列表
     */
    Page<Map<String, Object>> selectMembersByTeamId(Page<TeamMember> page, @Param("teamId") Long teamId);
}
