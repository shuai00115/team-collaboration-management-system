package com.teamcollab.dto.task;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 任务排序请求DTO
 * <p>
 * 用于在指定任务列表内批量调整任务的排列顺序。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "任务排序请求")
public class TaskReorderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属列表ID
     */
    @NotNull(message = "列表ID不能为空", groups = UpdateGroup.class)
    @Schema(description = "列表ID", example = "2", required = true)
    private Long listId;

    /**
     * 任务排序列表，至少包含一个排序项
     */
    @NotNull(message = "排序列表不能为空", groups = UpdateGroup.class)
    @Size(min = 1, message = "至少需要一个排序项", groups = UpdateGroup.class)
    @Schema(description = "排序列表", required = true)
    private List<TaskOrderItem> orders;

    /**
     * 任务排序项（内嵌类）
     */
    @Data
    @Schema(description = "任务排序项")
    public static class TaskOrderItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务ID
         */
        @NotNull(message = "任务ID不能为空", groups = UpdateGroup.class)
        @Schema(description = "任务ID", example = "1", required = true)
        private Long taskId;

        /**
         * 目标排序位置
         */
        @NotNull(message = "排序位置不能为空", groups = UpdateGroup.class)
        @Schema(description = "排序位置", example = "1", required = true)
        private Integer position;
    }
}
