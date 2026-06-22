package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务列表实体类
 * 对应数据库表 task_lists，存储项目下的任务分组列表（如待办、进行中、已完成）
 */
@Data
@TableName("task_lists")
@Schema(description = "任务列表信息")
public class TaskList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务列表ID，主键，自增
     */
    @TableId(value = "list_id", type = IdType.AUTO)
    @Schema(description = "任务列表ID")
    private Long listId;

    /**
     * 所属项目ID，关联 projects 表
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 任务列表名称，如 待办、进行中、已完成
     */
    @Schema(description = "任务列表名称")
    private String name;

    /**
     * 列表排序位置，控制列表的显示顺序
     */
    @Schema(description = "排序位置")
    private Integer position;

    /**
     * 是否为默认列表：0-否，1-是
     */
    @Schema(description = "是否默认列表")
    private Integer isDefault;

    /**
     * 任务列表创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
