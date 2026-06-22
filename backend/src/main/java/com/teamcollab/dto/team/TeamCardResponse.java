package com.teamcollab.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团队卡片响应DTO
 * <p>
 * 团队列表页展示所需的团队摘要信息，包含名称、状态、成员数及所需技能。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队卡片响应")
public class TeamCardResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String name;

    /**
     * 团队描述
     */
    @Schema(description = "团队描述", example = "专注于企业级应用开发的精英团队")
    private String description;

    /**
     * 团队状态：recruiting/active/archived
     */
    @Schema(description = "团队状态", example = "recruiting")
    private String status;

    /**
     * 当前成员数量
     */
    @Schema(description = "当前成员数", example = "8")
    private Integer currentMembers;

    /**
     * 最大成员数量
     */
    @Schema(description = "最大成员数", example = "20")
    private Integer maxMembers;

    /**
     * 创建者用户名
     */
    @Schema(description = "创建者用户名", example = "john_doe")
    private String creatorName;

    /**
     * 团队所需技能列表
     */
    @Schema(description = "所需技能列表")
    private List<RequiredSkillVO> requiredSkills;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 团队所需技能VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "所需技能")
    public static class RequiredSkillVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 技能ID
         */
        @Schema(description = "技能ID", example = "1")
        private Long skillId;

        /**
         * 技能名称
         */
        @Schema(description = "技能名称", example = "Java")
        private String skillName;

        /**
         * 技能分类
         */
        @Schema(description = "技能分类", example = "后端开发")
        private String category;
    }
}
