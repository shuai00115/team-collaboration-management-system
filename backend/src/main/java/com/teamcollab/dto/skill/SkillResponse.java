package com.teamcollab.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能响应DTO
 * <p>
 * 返回系统技能库中的技能信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技能响应")
public class SkillResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID
     */
    @Schema(description = "技能ID", example = "1")
    private Long skillId;

    /**
     * 技能名称
     */
    @Schema(description = "技能名称", example = "Java")
    private String skillName;

    /**
     * 技能分类
     */
    @Schema(description = "技能分类", example = "后端开发")
    private String category;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
