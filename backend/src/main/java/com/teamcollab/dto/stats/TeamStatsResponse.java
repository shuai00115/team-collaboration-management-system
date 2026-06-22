package com.teamcollab.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 团队统计响应DTO
 * <p>
 * 返回团队的整体任务统计概览、各优先级任务分布及成员贡献统计。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "团队统计响应")
public class TeamStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String teamName;

    /**
     * 整体任务概览
     */
    @Schema(description = "任务概览")
    private Overview overview;

    /**
     * 按优先级分组的任务统计
     */
    @Schema(description = "优先级分布统计")
    private PriorityBreakdown priorityBreakdown;

    /**
     * 各成员任务统计
     */
    @Schema(description = "成员统计列表")
    private List<MemberStats> memberStats;

    /**
     * 任务概览（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "任务概览")
    public static class Overview implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 任务总数
         */
        @Schema(description = "任务总数", example = "48")
        private Integer totalTasks;

        /**
         * 已完成任务数
         */
        @Schema(description = "已完成任务数", example = "30")
        private Integer completedTasks;

        /**
         * 进行中任务数
         */
        @Schema(description = "进行中任务数", example = "12")
        private Integer inProgressTasks;

        /**
         * 待办任务数
         */
        @Schema(description = "待办任务数", example = "6")
        private Integer todoTasks;

        /**
         * 完成率，0.0 ~ 1.0
         */
        @Schema(description = "完成率", example = "0.625")
        private Double completionRate;
    }

    /**
     * 优先级分布统计（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "优先级分布统计")
    public static class PriorityBreakdown implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 高优先级统计
         */
        @Schema(description = "高优先级统计")
        private PriorityStats high;

        /**
         * 中优先级统计
         */
        @Schema(description = "中优先级统计")
        private PriorityStats medium;

        /**
         * 低优先级统计
         */
        @Schema(description = "低优先级统计")
        private PriorityStats low;
    }

    /**
     * 优先级统计数据（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "优先级统计")
    public static class PriorityStats implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 该优先级总任务数
         */
        @Schema(description = "总任务数", example = "20")
        private Integer total;

        /**
         * 该优先级已完成任务数
         */
        @Schema(description = "已完成数", example = "15")
        private Integer completed;
    }

    /**
     * 成员统计（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "成员统计")
    public static class MemberStats implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "3")
        private Long userId;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "jane_smith")
        private String username;

        /**
         * 头像URL
         */
        @Schema(description = "头像", example = "https://cdn.example.com/avatars/default.png")
        private String avatar;

        /**
         * 分配的任务总数
         */
        @Schema(description = "分配任务数", example = "12")
        private Integer assignedTasks;

        /**
         * 已完成任务数
         */
        @Schema(description = "已完成任务数", example = "8")
        private Integer completedTasks;

        /**
         * 超期任务数
         */
        @Schema(description = "超期任务数", example = "2")
        private Integer overdueTasks;

        /**
         * 完成率，0.0 ~ 1.0
         */
        @Schema(description = "完成率", example = "0.66")
        private Double completionRate;

        /**
         * 贡献度，0.0 ~ 1.0（该成员完成任务占团队总完成任务的比率）
         */
        @Schema(description = "贡献度", example = "0.27")
        private Double contribution;
    }
}
