package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能实体类
 * 对应数据库表 skills，存储系统中可用的技能标签
 */
@Data
@TableName("skills")
@Schema(description = "技能信息")
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 技能ID，主键，自增
     */
    @TableId(value = "skill_id", type = IdType.AUTO)
    @Schema(description = "技能ID")
    private Long skillId;

    /**
     * 技能名称，如 Java、Python、项目管理等
     */
    @Schema(description = "技能名称")
    private String skillName;

    /**
     * 技能分类，如 编程语言、设计工具、管理能力等
     */
    @Schema(description = "技能分类")
    private String category;

    /**
     * 技能记录创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
