package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 加入申请实体类
 * 对应数据库表 join_requests，存储用户申请加入团队的请求记录
 */
@Data
@TableName("join_requests")
@Schema(description = "加入团队申请")
public class JoinRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID，主键，自增
     */
    @TableId(value = "request_id", type = IdType.AUTO)
    @Schema(description = "申请ID")
    private Long requestId;

    /**
     * 目标团队ID，关联 teams 表
     */
    @Schema(description = "目标团队ID")
    private Long teamId;

    /**
     * 申请用户ID，关联 users 表
     */
    @Schema(description = "申请用户ID")
    private Long userId;

    /**
     * 申请附带的留言信息
     */
    @Schema(description = "申请留言")
    private String message;

    /**
     * 申请状态：pending-待审核，approved-已通过，rejected-已拒绝
     */
    @Schema(description = "申请状态", allowableValues = {"pending", "approved", "rejected"})
    private String status;

    /**
     * 申请创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 申请审核时间，审核通过或拒绝时记录
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
}
