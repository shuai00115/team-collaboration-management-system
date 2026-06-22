package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.skill.SkillResponse;
import com.teamcollab.entity.Skill;

import java.util.List;

/**
 * 技能服务接口
 * <p>
 * 提供技能标签的查询与管理功能，包括分页列表、分类查询以及按ID精确查找。
 * </p>
 *
 * @author teamcollab
 */
public interface SkillService extends IService<Skill> {

    /**
     * 获取技能列表（分页）
     * <p>
     * 分页查询技能标签，支持按技能分类和关键词进行过滤。
     * </p>
     *
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param category 技能分类过滤，可为null表示不过滤
     * @param keyword  关键词模糊搜索（匹配技能名称），可为null表示不过滤
     * @return 分页的技能响应列表
     */
    Page<SkillResponse> getSkillList(int pageNum, int pageSize, String category, String keyword);

    /**
     * 获取所有技能分类
     * <p>
     * 查询系统中所有不重复的技能分类名称。
     * </p>
     *
     * @return 技能分类名称列表
     */
    List<String> getCategories();

    /**
     * 根据ID获取技能（不存在则抛出异常）
     * <p>
     * 通过技能ID精确查询技能信息，如果技能不存在则抛出业务异常。
     * </p>
     *
     * @param skillId 技能ID
     * @return 技能实体对象
     * @throws com.teamcollab.exception.BusinessException 当技能不存在时抛出
     */
    Skill getSkillByIdOrThrow(Long skillId);
}
