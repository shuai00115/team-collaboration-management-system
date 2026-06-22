package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 技能 Mapper 接口
 * <p>
 * 提供技能相关的数据库操作，包括技能分页搜索、技能分类列表查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    /**
     * 分页查询技能列表（支持分类筛选和关键字搜索）
     *
     * @param page     分页对象
     * @param category 技能分类
     * @param keyword  搜索关键字（匹配技能名称）
     * @return 分页技能列表
     */
    Page<Skill> selectSkillPage(Page<Skill> page, @Param("category") String category, @Param("keyword") String keyword);

    /**
     * 查询所有技能分类（去重）
     *
     * @return 分类名称列表
     */
    List<String> selectCategories();
}
