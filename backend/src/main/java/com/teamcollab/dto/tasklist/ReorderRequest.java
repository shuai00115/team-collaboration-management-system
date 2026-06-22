package com.teamcollab.dto.tasklist;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 任务列表排序请求DTO
 * <p>
 * 用于拖拽调整任务列表（看板列）的排列顺序。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "任务列表排序请求")
public class ReorderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排序列表，至少包含一个排序项
     */
    @NotNull(message = "排序列表不能为空", groups = UpdateGroup.class)
    @Size(min = 1, message = "至少需要一个排序项", groups = UpdateGroup.class)
    @Schema(description = "排序列表", required = true)
    private List<OrderItem> orders;

    /**
     * 排序项（内嵌类）
     */
    @Data
    @Schema(description = "排序项")
    public static class OrderItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务列表ID
         */
        @NotNull(message = "列表ID不能为空", groups = UpdateGroup.class)
        @Schema(description = "列表ID", example = "1", required = true)
        private Long listId;

        /**
         * 目标排序位置
         */
        @NotNull(message = "排序位置不能为空", groups = UpdateGroup.class)
        @Schema(description = "排序位置", example = "2", required = true)
        private Integer position;
    }
}
