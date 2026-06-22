package com.teamcollab.dto.stage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 阶段响应DTO
 * <p>
 * 返回阶段的详细信息，包括所属项目、状态、任务统计及是否超期。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "阶段响应")
public class StageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阶段ID
     */
    @Schema(description = "阶段ID", example = "1")
    private Long stageId;

    /**
     * 所属项目ID
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 阶段名称
     */
    @Schema(description = "阶段名称", example = "需求分析阶段")
    private String name;

    /**
     * 阶段描述
     */
    @Schema(description = "阶段描述", example = "完成需求调研与分析，输出需求文档")
    private String description;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private LocalDate endDate;

    /**
     * 排序索引
     */
    @Schema(description = "排序索引", example = "1")
    private Integer orderIndex;

    /**
     * 阶段状态：not_started/in_progress/completed
     */
    @Schema(description = "阶段状态", example = "in_progress")
    private String status;

    /**
     * 阶段内任务统计
     */
    @Schema(description = "任务统计")
    private TaskStatsVO taskStats;

    /**
     * 是否已超期（当前日期超过结束日期且未完成）
     */
    @Schema(description = "是否超期", example = "false")
    private Boolean isOverdue;

    /**
     * 任务统计VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "任务统计")
    public static class TaskStatsVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务总数
         */
        @Schema(description = "任务总数", example = "12")
        private Integer total;

        /**
         * 已完成任务数
         */
        @Schema(description = "已完成数", example = "9")
        private Integer completed;

        /**
         * 完成率，0.0 ~ 1.0
         */
        @Schema(description = "完成率", example = "0.75")
        private Double completionRate;
    }
}
