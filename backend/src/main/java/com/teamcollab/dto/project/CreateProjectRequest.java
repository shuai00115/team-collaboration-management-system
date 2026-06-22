package com.teamcollab.dto.project;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建项目请求DTO
 * <p>
 * 用于在团队中创建新项目，包含项目名称和描述。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建项目请求")
public class CreateProjectRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目名称，2-200个字符
     */
    @NotBlank(message = "项目名称不能为空", groups = AddGroup.class)
    @Size(min = 2, max = 200, message = "项目名称长度必须在2-200个字符之间", groups = AddGroup.class)
    @Schema(description = "项目名称", example = "企业办公管理系统", required = true)
    private String name;

    /**
     * 项目描述，最长500字
     */
    @Size(max = 500, message = "项目描述不能超过500个字符", groups = AddGroup.class)
    @Schema(description = "项目描述", example = "面向中小企业的综合办公管理平台")
    private String description;
}
