package com.teamcollab.dto.tasklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 任务列表响应DTO
 * <p>
 * 返回任务列表（看板列）的信息，包括名称、排序位置及包含的任务数。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务列表响应")
public class TaskListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列表ID
     */
    @Schema(description = "列表ID", example = "1")
    private Long listId;

    /**
     * 列表名称
     */
    @Schema(description = "列表名称", example = "待办事项")
    private String name;

    /**
     * 排序位置
     */
    @Schema(description = "排序位置", example = "1")
    private Integer position;

    /**
     * 是否为默认列表
     */
    @Schema(description = "是否默认列表", example = "true")
    private Boolean isDefault;

    /**
     * 列表内任务数量
     */
    @Schema(description = "任务数量", example = "8")
    private Integer taskCount;
}
