package com.teamcollab.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 未读通知数量响应DTO
 * <p>
 * 返回当前用户的未读通知总数。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "未读通知数量响应")
public class UnreadCountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 未读通知数量
     */
    @Schema(description = "未读通知数量", example = "5")
    private Integer unreadCount;
}
