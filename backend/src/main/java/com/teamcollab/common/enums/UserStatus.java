package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * <p>
 * 定义用户账户的可用状态，用于控制用户是否可以登录和使用系统。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /** 激活状态 - 用户可正常登录和使用系统 */
    active("激活"),

    /** 禁用状态 - 用户被管理员禁用，无法登录系统 */
    disabled("禁用");

    /** 状态中文描述 */
    private final String description;
}
