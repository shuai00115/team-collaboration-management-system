package com.teamcollab.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务简要响应DTO
 * <p>
 * 任务列表/看板视图中展示的任务摘要信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务简要响应")
public class TaskBriefResponse implements Serializable {

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
     * 任务描述
     */
    @Schema(description = "任务描述", example = "使用Spring Security实现JWT认证登录接口")
    private String description;

    /**
     * 优先级：low/medium/high/urgent
     */
    @Schema(description = "优先级", example = "high")
    private String priority;

    /**
     * 所属列表ID
     */
    @Schema(description = "列表ID", example = "2")
    private Long listId;

    /**
     * 所属列表名称
     */
    @Schema(description = "列表名称", example = "进行中")
    private String listName;

    /**
     * 所属阶段ID
     */
    @Schema(description = "阶段ID", example = "1")
    private Long stageId;

    /**
     * 所属阶段名称
     */
    @Schema(description = "阶段名称", example = "需求分析阶段")
    private String stageName;

    /**
     * 指派给的用户ID
     */
    @Schema(description = "指派用户ID", example = "3")
    private Long assigneeId;

    /**
     * 指派给的用户名
     */
    @Schema(description = "指派用户名", example = "jane_smith")
    private String assigneeName;

    /**
     * 指派用户头像
     */
    @Schema(description = "指派用户头像", example = "https://cdn.example.com/avatars/default.png")
    private String assigneeAvatar;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID", example = "1")
    private Long creatorId;

    /**
     * 创建者用户名
     */
    @Schema(description = "创建者用户名", example = "john_doe")
    private String creatorName;

    /**
     * 截止日期
     */
    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    /**
     * 排序位置
     */
    @Schema(description = "排序位置", example = "3")
    private Integer position;

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
}
