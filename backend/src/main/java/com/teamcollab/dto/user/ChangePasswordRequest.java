package com.teamcollab.dto.user;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求DTO
 * <p>
 * 用于用户修改登录密码，需提供原密码和新密码。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空", groups = UpdateGroup.class)
    @Schema(description = "原密码", example = "oldPassword123", required = true)
    private String oldPassword;

    /**
     * 新密码，6-20个字符
     */
    @NotBlank(message = "新密码不能为空", groups = UpdateGroup.class)
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间", groups = UpdateGroup.class)
    @Schema(description = "新密码", example = "newPassword456", required = true)
    private String newPassword;
}
