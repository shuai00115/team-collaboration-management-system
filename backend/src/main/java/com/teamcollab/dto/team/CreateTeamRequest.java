package com.teamcollab.dto.team;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建团队请求DTO
 * <p>
 * 用于用户创建新团队时提交的基本信息和所需技能。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建团队请求")
public class CreateTeamRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队名称，2-100个字符
     */
    @NotBlank(message = "团队名称不能为空", groups = AddGroup.class)
    @Size(min = 2, max = 100, message = "团队名称长度必须在2-100个字符之间", groups = AddGroup.class)
    @Schema(description = "团队名称", example = "天启开发组", required = true)
    private String name;

    /**
     * 团队描述，最长500字
     */
    @Size(max = 500, message = "团队描述不能超过500个字符", groups = AddGroup.class)
    @Schema(description = "团队描述", example = "专注于企业级应用开发的精英团队")
    private String description;

    /**
     * 最大成员数，默认10人，范围2-100
     */
    @Min(value = 2, message = "最大成员数不能少于2", groups = AddGroup.class)
    @Max(value = 100, message = "最大成员数不能超过100", groups = AddGroup.class)
    @Schema(description = "最大成员数", example = "20", defaultValue = "10")
    private Integer maxMembers = 10;

    /**
     * 团队所需的技能ID列表
     */
    @Schema(description = "所需技能ID列表", example = "[1, 2, 3]")
    private List<Long> requiredSkillIds;
}
