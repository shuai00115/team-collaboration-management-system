package com.teamcollab.dto.team;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新团队信息请求DTO
 * <p>
 * 用于团队管理员修改团队名称、描述、成员上限、状态及所需技能。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新团队请求")
public class UpdateTeamRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队名称，2-100个字符
     */
    @Size(min = 2, max = 100, message = "团队名称长度必须在2-100个字符之间", groups = UpdateGroup.class)
    @Schema(description = "团队名称", example = "天启开发组 Pro")
    private String name;

    /**
     * 团队描述，最长500字
     */
    @Size(max = 500, message = "团队描述不能超过500个字符", groups = UpdateGroup.class)
    @Schema(description = "团队描述", example = "升级后的精英开发团队")
    private String description;

    /**
     * 最大成员数，范围2-100
     */
    @Min(value = 2, message = "最大成员数不能少于2", groups = UpdateGroup.class)
    @Max(value = 100, message = "最大成员数不能超过100", groups = UpdateGroup.class)
    @Schema(description = "最大成员数", example = "30")
    private Integer maxMembers;

    /**
     * 团队状态：recruiting/active/archived
     */
    @Schema(description = "团队状态", example = "recruiting", allowableValues = {"recruiting", "active", "archived"})
    private String status;

    /**
     * 团队所需技能ID列表
     */
    @Schema(description = "所需技能ID列表", example = "[1, 2, 3, 4]")
    private List<Long> requiredSkillIds;
}
