package com.teamcollab.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 技能查询请求DTO
 * <p>
 * 用于分页查询和筛选系统技能库中的技能。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "技能查询请求")
public class SkillQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认第1页
     */
    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小，默认50条
     */
    @Schema(description = "每页大小", example = "50", defaultValue = "50")
    private Integer pageSize = 50;

    /**
     * 技能分类筛选
     */
    @Schema(description = "技能分类", example = "后端开发")
    private String category;

    /**
     * 关键词模糊搜索
     */
    @Schema(description = "关键词搜索", example = "Java")
    private String keyword;
}
