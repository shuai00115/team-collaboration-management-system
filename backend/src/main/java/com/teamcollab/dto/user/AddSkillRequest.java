package com.teamcollab.dto.user;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加用户技能请求DTO
 * <p>
 * 用于为当前用户添加一项技能及其熟练等级。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "添加用户技能请求")
public class AddSkillRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID
     */
    @NotNull(message = "技能ID不能为空", groups = AddGroup.class)
    @Schema(description = "技能ID", example = "1", required = true)
    private Long skillId;

    /**
     * 技能等级：beginner（初级）/ intermediate（中级）/ advanced（高级）
     */
    @NotBlank(message = "技能等级不能为空", groups = AddGroup.class)
    @Pattern(regexp = "beginner|intermediate|advanced", message = "技能等级必须为beginner、intermediate或advanced", groups = AddGroup.class)
    @Schema(description = "技能等级", example = "advanced", required = true, allowableValues = {"beginner", "intermediate", "advanced"})
    private String level;
}
