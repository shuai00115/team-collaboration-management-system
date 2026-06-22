package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 阶段实体类
 * 对应数据库表 stages，存储项目的阶段划分信息
 */
@Data
@TableName("stages")
@Schema(description = "项目阶段信息")
public class Stage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阶段ID，主键，自增
     */
    @TableId(value = "stage_id", type = IdType.AUTO)
    @Schema(description = "阶段ID")
    private Long stageId;

    /**
     * 所属项目ID，关联 projects 表
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 阶段名称
     */
    @Schema(description = "阶段名称")
    private String name;

    /**
     * 阶段描述信息
     */
    @Schema(description = "阶段描述")
    private String description;

    /**
     * 阶段开始日期
     */
    @Schema(description = "开始日期")
    private LocalDate startDate;

    /**
     * 阶段结束日期
     */
    @Schema(description = "结束日期")
    private LocalDate endDate;

    /**
     * 阶段排序索引，用于控制阶段的显示顺序
     */
    @Schema(description = "排序索引")
    private Integer orderIndex;

    /**
     * 阶段状态：not_started-未开始，in_progress-进行中，completed-已完成
     */
    @Schema(description = "阶段状态", allowableValues = {"not_started", "in_progress", "completed"})
    private String status;
}
