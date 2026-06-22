package com.teamcollab.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情响应DTO
 * <p>
 * 返回项目的完整信息，包括任务统计详情和阶段列表。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目详情响应")
public class ProjectDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 所属团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 所属团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String teamName;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "企业办公管理系统")
    private String name;

    /**
     * 项目描述
     */
    @Schema(description = "项目描述", example = "面向中小企业的综合办公管理平台")
    private String description;

    /**
     * 详细任务统计
     */
    @Schema(description = "任务统计详情")
    private TaskStatsFullVO taskStats;

    /**
     * 阶段列表
     */
    @Schema(description = "阶段列表")
    private List<StageBriefVO> stages;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 详细任务统计VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "详细任务统计")
    public static class TaskStatsFullVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务总数
         */
        @Schema(description = "任务总数", example = "24")
        private Integer total;

        /**
         * 已完成任务数
         */
        @Schema(description = "已完成数", example = "18")
        private Integer completed;

        /**
         * 进行中任务数
         */
        @Schema(description = "进行中数", example = "4")
        private Integer inProgress;

        /**
         * 待办任务数
         */
        @Schema(description = "待办数", example = "2")
        private Integer todo;

        /**
         * 完成率，0.0 ~ 1.0
         */
        @Schema(description = "完成率", example = "0.75")
        private Double completionRate;
    }

    /**
     * 阶段简要VO（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "阶段简要信息")
    public static class StageBriefVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 阶段ID
         */
        @Schema(description = "阶段ID", example = "1")
        private Long stageId;

        /**
         * 阶段名称
         */
        @Schema(description = "阶段名称", example = "需求分析阶段")
        private String name;

        /**
         * 阶段状态
         */
        @Schema(description = "阶段状态", example = "in_progress")
        private String status;

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
         * 阶段完成率，0.0 ~ 1.0
         */
        @Schema(description = "阶段完成率", example = "0.8")
        private Double completionRate;
    }
}
