package com.teamcollab.dto.stage;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 创建阶段请求DTO
 * <p>
 * 用于在项目中创建新的阶段，包含名称、描述及起止日期。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "创建阶段请求")
public class CreateStageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阶段名称，2-100个字符
     */
    @NotBlank(message = "阶段名称不能为空", groups = AddGroup.class)
    @Size(min = 2, max = 100, message = "阶段名称长度必须在2-100个字符之间", groups = AddGroup.class)
    @Schema(description = "阶段名称", example = "需求分析阶段", required = true)
    private String name;

    /**
     * 阶段描述，最长500字
     */
    @Size(max = 500, message = "阶段描述不能超过500个字符", groups = AddGroup.class)
    @Schema(description = "阶段描述", example = "完成需求调研与分析，输出需求文档")
    private String description;

    /**
     * 阶段开始日期
     */
    @Schema(description = "开始日期", example = "2026-01-01")
    private LocalDate startDate;

    /**
     * 阶段结束日期
     */
    @Schema(description = "结束日期", example = "2026-02-01")
    private LocalDate endDate;
}
