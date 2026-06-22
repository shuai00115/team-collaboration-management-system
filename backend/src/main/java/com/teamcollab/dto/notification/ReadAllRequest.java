package com.teamcollab.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 全部已读请求DTO
 * <p>
 * 将所有或指定类型的通知标记为已读。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "全部已读请求")
public class ReadAllRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知类型筛选（选填），不传则标记所有类型为已读
     */
    @Schema(description = "通知类型（选填）", example = "team", allowableValues = {"system", "team", "task", "application"})
    private String type;
}
