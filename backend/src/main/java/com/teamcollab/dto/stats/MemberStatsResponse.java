package com.teamcollab.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成员统计响应DTO
 * <p>
 * 返回单个成员在指定团队中的详细任务统计数据，按优先级分类展示。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成员统计响应")
public class MemberStatsResponse implements Serializable {

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
     * 进行中任务数
     */
    @Schema(description = "进行中任务数", example = "3")
    private Integer inProgressTasks;

    /**
     * 待办任务数
     */
    @Schema(description = "待办任务数", example = "1")
    private Integer todoTasks;

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
     * 贡献度，0.0 ~ 1.0
     */
    @Schema(description = "贡献度", example = "0.27")
    private Double contribution;

    /**
     * 按优先级分组的任务统计
     */
    @Schema(description = "按优先级统计")
    private PriorityTasks tasksByPriority;

    /**
     * 按优先级分组的任务统计（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "按优先级统计")
    public static class PriorityTasks implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 高优先级统计
         */
        @Schema(description = "高优先级")
        private PriorityCount high;

        /**
         * 中优先级统计
         */
        @Schema(description = "中优先级")
        private PriorityCount medium;

        /**
         * 低优先级统计
         */
        @Schema(description = "低优先级")
        private PriorityCount low;
    }

    /**
     * 优先级任务计数（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "优先级任务计数")
    public static class PriorityCount implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 分配数
         */
        @Schema(description = "分配数", example = "5")
        private Integer assigned;

        /**
         * 完成数
         */
        @Schema(description = "完成数", example = "3")
        private Integer completed;
    }
}
