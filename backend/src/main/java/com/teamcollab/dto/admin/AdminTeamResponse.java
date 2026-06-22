package com.teamcollab.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员团队响应DTO
 * <p>
 * 管理员视角下的团队信息，包含项目数量和创建时间等管理字段。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员团队响应")
public class AdminTeamResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队ID
     */
    @Schema(description = "团队ID", example = "1")
    private Long teamId;

    /**
     * 团队名称
     */
    @Schema(description = "团队名称", example = "天启开发组")
    private String name;

    /**
     * 团队状态：recruiting/active/archived
     */
    @Schema(description = "团队状态", example = "active", allowableValues = {"recruiting", "active", "archived"})
    private String status;

    /**
     * 当前成员数量
     */
    @Schema(description = "当前成员数", example = "8")
    private Integer currentMembers;

    /**
     * 最大成员数量
     */
    @Schema(description = "最大成员数", example = "20")
    private Integer maxMembers;

    /**
     * 创建者用户名
     */
    @Schema(description = "创建者", example = "john_doe")
    private String creatorName;

    /**
     * 团队下的项目数量
     */
    @Schema(description = "项目数量", example = "3")
    private Integer projectCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
