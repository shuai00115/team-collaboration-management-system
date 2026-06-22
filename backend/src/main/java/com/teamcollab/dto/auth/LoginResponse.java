package com.teamcollab.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录响应DTO
 * <p>
 * 登录成功后返回的令牌信息及用户基本信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌（JWT Token）
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    /**
     * 令牌类型，默认为 Bearer
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "过期时间（秒）", example = "7200")
    private Long expiresIn;

    /**
     * 当前登录用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 用户简要信息（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户简要信息")
    public static class UserInfo implements Serializable {

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
    }
}
