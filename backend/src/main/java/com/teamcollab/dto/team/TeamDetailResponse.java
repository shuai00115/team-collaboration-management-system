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
 * 团队详情响应DTO
 * <p>
 * 返回团队的完整信息，包括基本信息、所需技能、成员列表及时间戳。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队详情响应")
public class TeamDetailResponse implements Serializable {

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
    @Schema(description = "团队状态", example = "active")
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
     * 创建者ID
     */
    @Schema(description = "创建者ID", example = "1")
    private Long creatorId;

    /**
     * 创建者用户名
     */
    @Schema(description = "创建者用户名", example = "john_doe")
    private String creatorName;

    /**
     * 所需技能列表
     */
    @Schema(description = "所需技能列表")
    private List<RequiredSkillVO> requiredSkills;

    /**
     * 团队成员列表
     */
    @Schema(description = "团队成员列表")
    private List<MemberVO> members;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 团队成员VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "团队成员")
    public static class MemberVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1")
        private Long userId;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "john_doe")
        private String username;

        /**
         * 头像URL
         */
        @Schema(description = "头像", example = "https://cdn.example.com/avatars/default.png")
        private String avatar;

        /**
         * 成员角色：creator/admin/member
         */
        @Schema(description = "成员角色", example = "creator")
        private String role;

        /**
         * 加入时间
         */
        @Schema(description = "加入时间")
        private LocalDateTime joinedAt;
    }

    /**
     * 所需技能VO（内嵌类）
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
