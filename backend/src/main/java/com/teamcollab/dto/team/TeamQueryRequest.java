package com.teamcollab.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 团队查询请求DTO
 * <p>
 * 用于分页查询和筛选团队列表，支持按状态、技能、关键词、成员数范围等条件过滤。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "团队查询请求")
public class TeamQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认第1页
     */
    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小，默认12条
     */
    @Schema(description = "每页大小", example = "12", defaultValue = "12")
    private Integer pageSize = 12;

    /**
     * 团队状态筛选，默认查询招募中
     */
    @Schema(description = "团队状态", example = "recruiting", defaultValue = "recruiting", allowableValues = {"recruiting", "active", "archived"})
    private String status = "recruiting";

    /**
     * 按所需技能ID筛选
     */
    @Schema(description = "技能ID筛选", example = "1")
    private Long skillId;

    /**
     * 关键词模糊搜索（团队名称或描述）
     */
    @Schema(description = "关键词搜索", example = "开发")
    private String keyword;

    /**
     * 最低成员数筛选
     */
    @Schema(description = "最低成员数", example = "5")
    private Integer minMembers;

    /**
     * 最高成员数筛选
     */
    @Schema(description = "最高成员数", example = "15")
    private Integer maxMembers;
}
