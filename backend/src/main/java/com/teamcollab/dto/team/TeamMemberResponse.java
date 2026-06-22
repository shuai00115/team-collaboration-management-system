package com.teamcollab.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队成员响应DTO
 * <p>
 * 返回团队中单个成员的基本信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队成员响应")
public class TeamMemberResponse implements Serializable {

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
    @Schema(description = "成员角色", example = "member")
    private String role;

    /**
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
