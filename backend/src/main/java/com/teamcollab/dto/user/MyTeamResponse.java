package com.teamcollab.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的团队响应DTO
 * <p>
 * 返回当前用户所加入团队的基本信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的团队响应")
public class MyTeamResponse implements Serializable {

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
    private String teamName;

    /**
     * 团队描述
     */
    @Schema(description = "团队描述", example = "专注于企业级应用开发的团队")
    private String description;

    /**
     * 我的角色：creator/admin/member
     */
    @Schema(description = "我在团队中的角色", example = "member")
    private String myRole;

    /**
     * 团队状态：recruiting/active/archived
     */
    @Schema(description = "团队状态", example = "active")
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
     * 加入时间
     */
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
