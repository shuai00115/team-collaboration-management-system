package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 阶段模板实体类
 * 对应数据库表 stage_templates，存储预设的阶段模板，用于快速创建项目阶段
 */
@Data
@TableName("stage_templates")
@Schema(description = "阶段模板信息")
public class StageTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID，主键，自增
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    @Schema(description = "模板ID")
    private Long templateId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 阶段数据JSON字符串，存储阶段配置的JSON格式数据
     */
    @Schema(description = "阶段JSON数据")
    private String stagesJson;

    /**
     * 模板创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
