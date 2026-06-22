package com.teamcollab.dto.stage;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用阶段模板请求DTO
 * <p>
 * 用于将指定的阶段模板应用到项目中，批量创建阶段。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "应用阶段模板请求")
public class ApplyTemplateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阶段模板ID
     */
    @NotNull(message = "模板ID不能为空", groups = AddGroup.class)
    @Schema(description = "模板ID", example = "1", required = true)
    private Long templateId;
}
