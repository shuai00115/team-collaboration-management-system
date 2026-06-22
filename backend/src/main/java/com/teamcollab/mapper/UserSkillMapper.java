package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.UserSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户技能关联 Mapper 接口
 * <p>
 * 提供用户与技能关联关系的数据库操作，包括查询用户技能列表、删除关联、检查关联是否存在等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface UserSkillMapper extends BaseMapper<UserSkill> {

    /**
     * 查询指定用户的所有技能（含技能详情）
     *
     * @param userId 用户 ID
     * @return 用户技能列表，每项包含技能名称、分类等信息
     */
    List<Map<String, Object>> selectUserSkills(@Param("userId") Long userId);

    /**
     * 删除用户与指定技能的关联
     *
     * @param userId  用户 ID
     * @param skillId 技能 ID
     * @return 受影响的行数
     */
    int deleteByUserAndSkill(@Param("userId") Long userId, @Param("skillId") Long skillId);

    /**
     * 检查用户与技能的关联是否存在
     *
     * @param userId  用户 ID
     * @param skillId 技能 ID
     * @return 关联记录数（0 表示不存在）
     */
    int countByUserAndSkill(@Param("userId") Long userId, @Param("skillId") Long skillId);
}
