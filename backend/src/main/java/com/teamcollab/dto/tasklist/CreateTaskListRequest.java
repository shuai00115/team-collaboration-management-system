package com.teamcollab.dto.tasklist;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建任务列表请求DTO
 * <p>
 * 用于在阶段下创建新的任务列表（看板列）。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建任务列表请求")
public class CreateTaskListRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务列表名称，2-100个字符
     */
    @NotBlank(message = "任务列表名称不能为空", groups = AddGroup.class)
    @Size(min = 2, max = 100, message = "任务列表名称长度必须在2-100个字符之间", groups = AddGroup.class)
    @Schema(description = "任务列表名称", example = "待办事项", required = true)
    private String name;
}
