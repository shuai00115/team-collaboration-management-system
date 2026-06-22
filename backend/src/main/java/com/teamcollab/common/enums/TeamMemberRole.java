package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团队成员角色枚举
 * <p>
 * 定义团队内部成员的角色，用于控制成员在团队内的操作权限。
 * 队长拥有团队的全部管理权限，成员拥有基本的协作权限。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum TeamMemberRole {

    /** 队长 - 拥有团队的全部管理权限，包括审批申请、移除成员等 */
    leader("队长"),

    /** 成员 - 拥有团队的基本协作权限 */
    member("成员");

    /** 角色中文描述 */
    private final String description;
}
