package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务实体类
 * 对应数据库表 tasks，存储项目中的具体任务信息
 */
@Data
@TableName("tasks")
@Schema(description = "任务信息")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID，主键，自增
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 所属任务列表ID，关联 task_lists 表
     */
    @Schema(description = "所属任务列表ID")
    private Long listId;

    /**
     * 所属阶段ID，关联 stages 表
     */
    @Schema(description = "所属阶段ID")
    private Long stageId;

    /**
     * 任务标题
     */
    @Schema(description = "任务标题")
    private String title;

    /**
     * 任务详细描述
     */
    @Schema(description = "任务描述")
    private String description;

    /**
     * 任务优先级：high-高，medium-中，low-低
     */
    @Schema(description = "任务优先级", allowableValues = {"high", "medium", "low"})
    private String priority;

    /**
     * 任务截止日期时间
     */
    @Schema(description = "截止日期")
    private LocalDateTime dueDate;

    /**
     * 任务负责人用户ID，关联 users 表
     */
    @Schema(description = "负责人用户ID")
    private Long assigneeId;

    /**
     * 任务创建者用户ID，关联 users 表
     */
    @Schema(description = "创建者用户ID")
    private Long creatorId;

    /**
     * 任务排序位置，控制任务在列表中的显示顺序
     */
    @Schema(description = "排序位置")
    private Integer position;

    /**
     * 任务创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 任务信息更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
