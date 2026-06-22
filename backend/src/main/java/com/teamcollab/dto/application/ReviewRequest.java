package com.teamcollab.dto.application;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核入队申请请求DTO
 * <p>
 * 用于团队管理员审核入队申请，决定批准或拒绝。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "审核入队申请请求")
public class ReviewRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审核动作：approve（批准）/ reject（拒绝）
     */
    @NotBlank(message = "审核动作不能为空", groups = UpdateGroup.class)
    @Pattern(regexp = "approve|reject", message = "审核动作必须为approve或reject", groups = UpdateGroup.class)
    @Schema(description = "审核动作", example = "approve", required = true, allowableValues = {"approve", "reject"})
    private String action;
}
