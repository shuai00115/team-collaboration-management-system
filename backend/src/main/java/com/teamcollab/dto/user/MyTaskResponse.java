package com.teamcollab.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的任务响应DTO
 * <p>
 * 返回当前用户被分配的任务的简要信息，包含任务所属项目、团队等上下文。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的任务响应")
public class MyTaskResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    /**
     * 任务标题
     */
    @Schema(description = "任务标题", example = "实现用户登录接口")
    private String title;

    /**
     * 优先级：low/medium/high/urgent
     */
    @Schema(description = "优先级", example = "high")
    private String priority;

    /**
     * 任务列表名称
     */
    @Schema(description = "任务列表名称", example = "进行中")
    private String listName;

    /**
     * 阶段名称
     */
    @Schema(description = "阶段名称", example = "第一阶段")
    private String stageName;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "企业办公系统")
    private String projectName;

    /**
     * 团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String teamName;

    /**
     * 团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 截止日期
     */
    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
