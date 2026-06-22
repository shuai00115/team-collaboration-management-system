package com.teamcollab.dto.task;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建任务请求DTO
 * <p>
 * 用于在任务列表中创建新任务，包含标题、描述、优先级、截止日期、指派人和所属阶段。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建任务请求")
public class CreateTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题，2-200个字符
     */
    @NotBlank(message = "任务标题不能为空", groups = AddGroup.class)
    @Size(min = 2, max = 200, message = "任务标题长度必须在2-200个字符之间", groups = AddGroup.class)
    @Schema(description = "任务标题", example = "实现用户登录接口", required = true)
    private String title;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述", example = "使用Spring Security实现JWT认证登录接口")
    private String description;

    /**
     * 优先级：low/medium/high/urgent
     */
    @Schema(description = "优先级", example = "high", allowableValues = {"low", "medium", "high", "urgent"})
    private String priority;

    /**
     * 截止日期，格式：yyyy-MM-dd HH:mm:ss
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "截止日期格式必须为yyyy-MM-dd HH:mm:ss", groups = AddGroup.class)
    @Schema(description = "截止日期", example = "2026-03-01 18:00:00")
    private String dueDate;

    /**
     * 指派给的用户ID
     */
    @Schema(description = "指派用户ID", example = "3")
    private Long assigneeId;

    /**
     * 所属阶段ID
     */
    @Schema(description = "阶段ID", example = "1")
    private Long stageId;
}
