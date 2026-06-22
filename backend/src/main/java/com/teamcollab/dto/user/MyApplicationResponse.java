package com.teamcollab.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的申请响应DTO
 * <p>
 * 返回当前用户发出的入队申请信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的申请响应")
public class MyApplicationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请记录ID
     */
    @Schema(description = "申请ID", example = "1")
    private Long requestId;

    /**
     * 目标团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 目标团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String teamName;

    /**
     * 申请留言
     */
    @Schema(description = "申请留言", example = "我有5年Java开发经验，希望加入贵团队")
    private String message;

    /**
     * 申请状态：pending/approved/rejected
     */
    @Schema(description = "申请状态", example = "pending")
    private String status;

    /**
     * 申请创建时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime createdAt;

    /**
     * 审核时间，未审核时为null
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
}
