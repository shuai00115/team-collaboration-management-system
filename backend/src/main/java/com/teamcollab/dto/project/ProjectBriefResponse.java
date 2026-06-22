package com.teamcollab.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目简要响应DTO
 * <p>
 * 项目列表展示所需的项目摘要信息，包含任务统计概况。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目简要响应")
public class ProjectBriefResponse implements Serializable {

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
     * 任务统计信息
     */
    @Schema(description = "任务统计")
    private TaskStatsVO taskStats;

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
        @Schema(description = "任务总数", example = "24")
        private Integer total;

        /**
         * 已完成任务数
         */
        @Schema(description = "已完成数", example = "18")
        private Integer completed;

        /**
         * 完成率，0.0 ~ 1.0
         */
        @Schema(description = "完成率", example = "0.75")
        private Double completionRate;
    }
}
