package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户技能关联实体类
 * 对应数据库表 user_skills，记录用户与技能的关联关系及其掌握程度
 * 复合主键：(user_id, skill_id)
 */
@Data
@TableName("user_skills")
@Schema(description = "用户技能关联")
public class UserSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，复合主键之一，关联 users 表
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 技能ID，复合主键之一，关联 skills 表
     */
    @TableField("skill_id")
    @Schema(description = "技能ID")
    private Long skillId;

    /**
     * 技能掌握程度：beginner-初级，intermediate-中级，advanced-高级
     */
    @Schema(description = "技能掌握程度", allowableValues = {"beginner", "intermediate", "advanced"})
    private String level;

    /**
     * 关联记录创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
