package com.teamcollab.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员用户响应DTO
 * <p>
 * 管理员视角下的用户信息，包含用户状态等管理所需字段。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员用户响应")
public class AdminUserResponse implements Serializable {

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
     * 用户角色：admin/member
     */
    @Schema(description = "角色", example = "member")
    private String role;

    /**
     * 账号状态：active/disabled
     */
    @Schema(description = "账号状态", example = "active", allowableValues = {"active", "disabled"})
    private String status;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
}
