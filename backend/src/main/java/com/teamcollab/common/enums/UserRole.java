package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * <p>
 * 定义系统中的用户角色类型，用于权限控制和访问管理。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    /** 管理员 - 拥有系统的全部操作权限 */
    admin("管理员"),

    /** 普通成员 - 拥有基本的团队协作权限 */
    member("普通成员");

    /** 角色中文描述 */
    private final String description;
}
