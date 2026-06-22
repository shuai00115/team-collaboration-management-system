package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.TeamRequiredSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 团队所需技能 Mapper 接口
 * <p>
 * 提供团队所需技能关联关系的数据库操作，包括查询团队所需技能详情、批量删除团队所需技能等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface TeamRequiredSkillMapper extends BaseMapper<TeamRequiredSkill> {

    /**
     * 查询指定团队的所有所需技能（含技能详情）
     *
     * @param teamId 团队 ID
     * @return 技能列表，每项包含技能名称、分类等详细信息
     */
    List<Map<String, Object>> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 删除指定团队的所有所需技能关联
     *
     * @param teamId 团队 ID
     * @return 受影响的行数
     */
    int deleteByTeamId(@Param("teamId") Long teamId);
}
