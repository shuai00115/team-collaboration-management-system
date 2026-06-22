package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.exception.BusinessException;
import com.teamcollab.dto.skill.SkillResponse;
import com.teamcollab.entity.Skill;
import com.teamcollab.mapper.SkillMapper;
import com.teamcollab.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能服务实现类
 * <p>
 * 提供系统技能标签库的查询功能，包括分页搜索、分类列表获取和按ID查找。
 * 技能标签为用户个人资料和团队招募需求的基础数据。
 * </p>
 *
 * <p><b>功能说明：</b></p>
 * <ul>
 *     <li>getSkillList —— 分页查询技能列表，支持按分类和关键词筛选</li>
 *     <li>getCategories —— 获取所有不重复的技能分类名称</li>
 *     <li>getSkillByIdOrThrow —— 按 ID 查找技能，不存在时抛出异常</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl extends ServiceImpl<SkillMapper, Skill> implements SkillService {

    /** 技能 Mapper，提供分页查询、分类查询等数据库操作 */
    private final SkillMapper skillMapper;

    /**
     * 分页获取技能列表（支持分类筛选和关键词搜索）
     * <p>
     * 调用 SkillMapper.selectSkillPage 执行数据库分页查询，
     * 将查询到的 Skill 实体列表转换为 SkillResponse DTO 列表返回。
     * 支持按 category（技能分类）和 keyword（技能名称关键词）进行动态过滤。
     * </p>
     *
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param category 技能分类筛选，可为 null 表示不限制分类
     * @param keyword  关键词模糊搜索（匹配技能名称），可为 null 表示不搜索
     * @return 分页的技能响应列表
     */
    @Override
    public Page<SkillResponse> getSkillList(int pageNum, int pageSize, String category, String keyword) {
        // 执行分页查询
        Page<Skill> queryPage = new Page<>(pageNum, pageSize);
        Page<Skill> resultPage = skillMapper.selectSkillPage(queryPage, category, keyword);

        // 将实体转换为响应 DTO
        List<SkillResponse> records = resultPage.getRecords().stream().map(skill -> {
            SkillResponse response = new SkillResponse();
            response.setSkillId(skill.getSkillId());
            response.setSkillName(skill.getSkillName());
            response.setCategory(skill.getCategory());
            response.setCreatedAt(skill.getCreatedAt());
            return response;
        }).collect(Collectors.toList());

        // 构建分页响应
        Page<SkillResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询技能列表: page={}/{}, category={}, keyword={}, 总数={}",
                pageNum, pageSize, category, keyword, resultPage.getTotal());
        return responsePage;
    }

    /**
     * 获取所有技能分类名称列表
     * <p>
     * 调用 SkillMapper.selectCategories 查询 skills 表中所有不重复的分类名称。
     * 用于前端技能筛选器的下拉选项。
     * </p>
     *
     * @return 去重的分类名称字符串列表
     */
    @Override
    public List<String> getCategories() {
        List<String> categories = skillMapper.selectCategories();
        log.info("查询技能分类: 共 {} 个分类", categories.size());
        return categories;
    }

    /**
     * 根据技能ID获取技能实体（不存在时抛出异常）
     * <p>
     * 使用 MyBatis-Plus 基类的 getById 方法查询技能。
     * 如果查询结果为空，则抛出 BusinessException 并携带 SKILL_NOT_FOUND 错误码。
     * 此方法常用于其他服务中校验技能是否存在的场景。
     * </p>
     *
     * @param skillId 技能 ID
     * @return Skill 实体对象
     * @throws BusinessException 当技能不存在时（ErrorCode.SKILL_NOT_FOUND）
     */
    @Override
    public Skill getSkillByIdOrThrow(Long skillId) {
        Skill skill = this.getById(skillId);
        if (skill == null) {
            log.warn("技能不存在: skillId={}", skillId);
            throw new BusinessException(ErrorCode.SKILL_NOT_FOUND);
        }
        return skill;
    }
}
