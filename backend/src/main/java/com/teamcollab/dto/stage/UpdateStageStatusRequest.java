package com.teamcollab.dto.stage;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新阶段状态请求DTO
 * <p>
 * 用于切换阶段的执行状态。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新阶段状态请求")
public class UpdateStageStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阶段状态：not_started（未开始）/ in_progress（进行中）/ completed（已完成）
     */
    @NotBlank(message = "阶段状态不能为空", groups = UpdateGroup.class)
    @Schema(description = "阶段状态", example = "completed", required = true, allowableValues = {"not_started", "in_progress", "completed"})
    private String status;
}
