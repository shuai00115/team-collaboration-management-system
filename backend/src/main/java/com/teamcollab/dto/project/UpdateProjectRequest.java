package com.teamcollab.dto.project;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新项目信息请求DTO
 * <p>
 * 用于修改已有项目的名称和描述。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新项目请求")
public class UpdateProjectRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目名称，2-200个字符
     */
    @Size(min = 2, max = 200, message = "项目名称长度必须在2-200个字符之间", groups = UpdateGroup.class)
    @Schema(description = "项目名称", example = "企业办公管理系统 V2")
    private String name;

    /**
     * 项目描述，最长500字
     */
    @Size(max = 500, message = "项目描述不能超过500个字符", groups = UpdateGroup.class)
    @Schema(description = "项目描述", example = "升级后的综合办公管理平台")
    private String description;
}
