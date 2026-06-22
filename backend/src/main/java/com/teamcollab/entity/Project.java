package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 对应数据库表 projects，存储团队下的项目信息
 */
@Data
@TableName("projects")
@Schema(description = "项目信息")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID，主键，自增
     */
    @TableId(value = "project_id", type = IdType.AUTO)
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 所属团队ID，关联 teams 表
     */
    @Schema(description = "所属团队ID")
    private Long teamId;

    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String name;

    /**
     * 项目描述信息
     */
    @Schema(description = "项目描述")
    private String description;

    /**
     * 项目创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 项目信息更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
