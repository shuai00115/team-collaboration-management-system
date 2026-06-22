package com.teamcollab.dto.tasklist;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新任务列表请求DTO
 * <p>
 * 用于修改已有任务列表的名称。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新任务列表请求")
public class UpdateTaskListRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务列表名称，2-100个字符
     */
    @NotBlank(message = "任务列表名称不能为空", groups = UpdateGroup.class)
    @Size(min = 2, max = 100, message = "任务列表名称长度必须在2-100个字符之间", groups = UpdateGroup.class)
    @Schema(description = "任务列表名称", example = "进行中", required = true)
    private String name;
}
