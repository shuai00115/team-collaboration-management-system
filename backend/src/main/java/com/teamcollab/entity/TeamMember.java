package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队成员关联实体类
 * 对应数据库表 team_members，记录团队成员关系及角色
 * 复合主键：(team_id, user_id)
 */
@Data
@TableName("team_members")
@Schema(description = "团队成员关联")
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID，复合主键之一，关联 teams 表
     */
    @TableField("team_id")
    @Schema(description = "团队ID")
    private Long teamId;

    /**
     * 用户ID，复合主键之一，关联 users 表
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 成员在团队中的角色：leader-队长，member-普通成员
     */
    @Schema(description = "团队成员角色", allowableValues = {"leader", "member"})
    private String role;

    /**
     * 成员加入团队的时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
