package com.teamcollab.dto.auth;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户注册请求DTO
 * <p>
 * 用于处理新用户注册时提交的表单数据，包含用户名、密码、邮箱及可选技能列表。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名，4-20个字符，仅允许字母、数字和下划线
     */
    @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须为4-20位字母、数字或下划线", groups = AddGroup.class)
    @Schema(description = "用户名", example = "john_doe", required = true)
    private String username;

    /**
     * 密码，6-20个字符
     */
    @NotBlank(message = "密码不能为空", groups = AddGroup.class)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间", groups = AddGroup.class)
    @Schema(description = "密码", example = "password123", required = true)
    private String password;

    /**
     * 电子邮箱地址
     */
    @NotBlank(message = "邮箱不能为空", groups = AddGroup.class)
    @Email(message = "邮箱格式不正确", groups = AddGroup.class)
    @Schema(description = "电子邮箱", example = "john@example.com", required = true)
    private String email;

    /**
     * 用户技能ID列表（选填）
     */
    @Schema(description = "技能ID列表（选填）", example = "[1, 2, 3]")
    private List<Long> skillIds;
}
