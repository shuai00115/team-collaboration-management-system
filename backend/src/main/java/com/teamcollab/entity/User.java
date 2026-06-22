package com.teamcollab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 users，存储系统用户的基本信息
 */
@Data
@TableName("users")
@Schema(description = "用户信息")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID，主键，自增
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名，用于登录和显示
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码哈希值，存储加密后的密码
     */
    @TableField("password_hash")
    @Schema(description = "密码哈希值")
    private String passwordHash;

    /**
     * 用户邮箱地址
     */
    @Schema(description = "邮箱地址")
    private String email;

    /**
     * 用户头像URL地址
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 用户个人简介
     */
    @Schema(description = "个人简介")
    private String bio;

    /**
     * 用户角色：admin-管理员，member-普通成员
     */
    @Schema(description = "用户角色", allowableValues = {"admin", "member"})
    private String role;

    /**
     * 用户状态：active-激活，disabled-禁用
     */
    @Schema(description = "用户状态", allowableValues = {"active", "disabled"})
    private String status;

    /**
     * 用户创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 用户信息更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
