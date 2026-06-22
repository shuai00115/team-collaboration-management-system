package com.teamcollab.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知响应DTO
 * <p>
 * 返回系统通知的详细信息，包括通知类型、内容、关联业务及阅读状态。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知响应")
public class NotificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @Schema(description = "通知ID", example = "1")
    private Long notificationId;

    /**
     * 通知类型：system/team/task/application
     */
    @Schema(description = "通知类型", example = "team", allowableValues = {"system", "team", "task", "application"})
    private String type;

    /**
     * 通知标题
     */
    @Schema(description = "通知标题", example = "入队申请已通过")
    private String title;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容", example = "您申请加入「天启开发组」的请求已通过审核")
    private String content;

    /**
     * 关联业务类型
     */
    @Schema(description = "关联业务类型", example = "team")
    private String relatedType;

    /**
     * 关联业务ID
     */
    @Schema(description = "关联业务ID", example = "1")
    private Long relatedId;

    /**
     * 关联项目ID，方便前端点击通知后直接跳转到对应项目
     */
    @Schema(description = "关联项目ID", example = "1")
    private Long projectId;

    /**
     * 是否已读：0-未读，1-已读
     */
    @Schema(description = "是否已读", example = "0")
    private Integer isRead;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
