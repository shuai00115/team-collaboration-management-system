package com.teamcollab.dto.stage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 阶段模板响应DTO
 * <p>
 * 返回阶段模板的详细信息，包含模板中的所有阶段项。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "阶段模板响应")
public class StageTemplateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "标准软件开发流程")
    private String templateName;

    /**
     * 阶段数量
     */
    @Schema(description = "阶段数量", example = "5")
    private Integer stageCount;

    /**
     * 模板阶段列表
     */
    @Schema(description = "阶段列表")
    private List<StageTemplateItem> stages;

    /**
     * 阶段模板项（内嵌类）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "阶段模板项")
    public static class StageTemplateItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 阶段名称
         */
        @Schema(description = "阶段名称", example = "需求分析")
        private String name;

        /**
         * 阶段描述
         */
        @Schema(description = "阶段描述", example = "收集和分析项目需求，编写需求规格说明书")
        private String description;

        /**
         * 排序索引
         */
        @Schema(description = "排序索引", example = "1")
        private Integer orderIndex;
    }
}
