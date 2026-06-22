package com.teamcollab.dto.task;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新任务请求DTO
 * <p>
 * 用于修改已有任务的各项属性，所有字段均为可选。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新任务请求")
public class UpdateTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题，2-200个字符
     */
    @Size(min = 2, max = 200, message = "任务标题长度必须在2-200个字符之间", groups = UpdateGroup.class)
    @Schema(description = "任务标题", example = "实现JWT登录认证接口")
    private String title;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述", example = "更新描述内容")
    private String description;

    /**
     * 优先级：low/medium/high/urgent
     */
    @Schema(description = "优先级", example = "urgent", allowableValues = {"low", "medium", "high", "urgent"})
    private String priority;

    /**
     * 截止日期
     */
    @Schema(description = "截止日期", example = "2026-03-15 18:00:00")
    private String dueDate;

    /**
     * 指派给的用户ID
     */
    @Schema(description = "指派用户ID", example = "5")
    private Long assigneeId;

    /**
     * 所属阶段ID
     */
    @Schema(description = "阶段ID", example = "2")
    private Long stageId;
}
