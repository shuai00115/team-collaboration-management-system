package com.teamcollab.dto.task;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 移动任务请求DTO
 * <p>
 * 用于将任务移动到指定任务列表的指定位置（拖拽操作）。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "移动任务请求")
public class MoveTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标列表ID
     */
    @NotNull(message = "目标列表ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "目标列表ID", example = "3", required = true)
    private Long targetListId;

    /**
     * 目标排序位置
     */
    @Schema(description = "目标排序位置", example = "1")
    private Integer targetPosition;
}
