package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队实体类
 * 对应数据库表 teams，存储团队的基本信息
 */
@Data
@TableName("teams")
@Schema(description = "团队信息")
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID，主键，自增
     */
    @TableId(value = "team_id", type = IdType.AUTO)
    @Schema(description = "团队ID")
    private Long teamId;

    /**
     * 团队名称
     */
    @Schema(description = "团队名称")
    private String name;

    /**
     * 团队描述信息
     */
    @Schema(description = "团队描述")
    private String description;

    /**
     * 团队最大成员数量限制
     */
    @Schema(description = "最大成员数")
    private Integer maxMembers;

    /**
     * 团队状态：recruiting-招募中，closed-已关闭招募
     */
    @Schema(description = "团队状态", allowableValues = {"recruiting", "closed"})
    private String status;

    /**
     * 团队创建者用户ID，关联 users 表
     */
    @Schema(description = "创建者用户ID")
    private Long creatorId;

    /**
     * 团队创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 团队信息更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
