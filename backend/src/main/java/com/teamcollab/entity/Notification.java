package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库表 notifications，存储系统通知消息
 */
@Data
@TableName("notifications")
@Schema(description = "通知信息")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID，主键，自增
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long notificationId;

    /**
     * 接收通知的用户ID，关联 users 表
     */
    @Schema(description = "接收用户ID")
    private Long userId;

    /**
     * 通知类型，如 申请通知、审批通知、任务分配通知等
     */
    @Schema(description = "通知类型")
    private String type;

    /**
     * 通知标题
     */
    @Schema(description = "通知标题")
    private String title;

    /**
     * 通知内容详情
     */
    @Schema(description = "通知内容")
    private String content;

    /**
     * 关联实体类型，指定通知关联的业务对象类型（如 team、project、task 等）
     */
    @Schema(description = "关联实体类型")
    private String relatedType;

    /**
     * 关联实体ID，指向具体业务对象的ID
     */
    @Schema(description = "关联实体ID")
    private Long relatedId;

    /**
     * 是否已读：0-未读，1-已读
     */
    @Schema(description = "是否已读")
    private Integer isRead;

    /**
     * 通知创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
