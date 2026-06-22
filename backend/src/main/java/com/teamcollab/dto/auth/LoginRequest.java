package com.teamcollab.dto.auth;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求DTO
 * <p>
 * 用于处理用户登录时提交的用户名和密码。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
    @Schema(description = "用户名", example = "john_doe", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空", groups = AddGroup.class)
    @Schema(description = "密码", example = "password123", required = true)
    private String password;
}
