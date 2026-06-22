package com.teamcollab.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应DTO
 * <p>
 * 返回用户的完整信息，包括基本信息、技能列表及注册时间。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserInfoResponse implements Serializable {

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
     * 邮箱
     */
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像", example = "https://cdn.example.com/avatars/default.png")
    private String avatar;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介", example = "热爱编程的全栈工程师")
    private String bio;

    /**
     * 用户角色
     */
    @Schema(description = "角色", example = "member")
    private String role;

    /**
     * 用户技能列表
     */
    @Schema(description = "技能列表")
    private List<UserSkillVO> skills;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    /**
     * 用户技能VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户技能")
    public static class UserSkillVO implements Serializable {

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

        /**
         * 技能等级：beginner/intermediate/advanced
         */
        @Schema(description = "技能等级", example = "advanced")
        private String level;
    }
}
