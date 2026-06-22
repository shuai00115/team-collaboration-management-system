package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 团队需求技能关联实体类
 * 对应数据库表 team_required_skills，记录团队招募所需的技能要求
 * 复合主键：(team_id, skill_id)
 */
@Data
@TableName("team_required_skills")
@Schema(description = "团队需求技能关联")
public class TeamRequiredSkill implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID，复合主键之一，关联 teams 表
     */
    @TableField("team_id")
    @Schema(description = "团队ID")
    private Long teamId;

    /**
     * 技能ID，复合主键之一，关联 skills 表
     */
    @TableField("skill_id")
    @Schema(description = "技能ID")
    private Long skillId;
}
