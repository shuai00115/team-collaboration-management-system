package com.teamcollab.dto.admin;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建技能请求DTO
 * <p>
 * 管理员用于向系统技能库中添加新的技能选项。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建技能请求")
public class CreateSkillRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能名称，最长50个字符
     */
    @NotBlank(message = "技能名称不能为空", groups = AddGroup.class)
    @Size(max = 50, message = "技能名称不能超过50个字符", groups = AddGroup.class)
    @Schema(description = "技能名称", example = "Kubernetes", required = true)
    private String skillName;

    /**
     * 技能分类，最长50个字符
     */
    @NotBlank(message = "技能分类不能为空", groups = AddGroup.class)
    @Size(max = 50, message = "技能分类不能超过50个字符", groups = AddGroup.class)
    @Schema(description = "技能分类", example = "运维与DevOps", required = true)
    private String category;
}
