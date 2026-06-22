package com.teamcollab.dto.admin;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户状态请求DTO
 * <p>
 * 管理员用于启用或禁用指定用户账号。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新用户状态请求")
public class UpdateUserStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号状态：active（启用）/ disabled（禁用）
     */
    @NotBlank(message = "账号状态不能为空", groups = UpdateGroup.class)
    @Pattern(regexp = "active|disabled", message = "账号状态必须为active或disabled", groups = UpdateGroup.class)
    @Schema(description = "账号状态", example = "disabled", required = true, allowableValues = {"active", "disabled"})
    private String status;
}
