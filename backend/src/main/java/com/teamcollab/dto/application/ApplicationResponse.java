package com.teamcollab.dto.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入队申请响应DTO
 * <p>
 * 返回入队申请的详细信息，包括申请人信息、技能及申请状态。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "入队申请响应")
public class ApplicationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请记录ID
     */
    @Schema(description = "申请ID", example = "1")
    private Long requestId;

    /**
     * 申请人用户ID
     */
    @Schema(description = "申请人ID", example = "5")
    private Long userId;

    /**
     * 申请人用户名
     */
    @Schema(description = "申请人用户名", example = "jane_smith")
    private String username;

    /**
     * 申请人头像
     */
    @Schema(description = "申请人头像", example = "https://cdn.example.com/avatars/default.png")
    private String avatar;

    /**
     * 申请人技能列表
     */
    @Schema(description = "申请人技能列表")
    private List<UserSkillVO> userSkills;

    /**
     * 申请留言
     */
    @Schema(description = "申请留言", example = "希望加入贵团队一起学习进步")
    private String message;

    /**
     * 申请状态：pending/approved/rejected
     */
    @Schema(description = "申请状态", example = "pending")
    private String status;

    /**
     * 申请创建时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime createdAt;

    /**
     * 申请审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    /**
     * 申请人技能VO（内嵌类）
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
         * 技能等级：beginner/intermediate/advanced
         */
        @Schema(description = "技能等级", example = "advanced")
        private String level;
    }
}
