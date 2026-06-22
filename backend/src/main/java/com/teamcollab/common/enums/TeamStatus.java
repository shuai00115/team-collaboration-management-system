package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团队状态枚举
 * <p>
 * 定义团队的招募状态，用于控制是否接受新成员的加入申请。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum TeamStatus {

    /** 招募中 - 团队正在接受新成员的入队申请 */
    recruiting("招募中"),

    /** 已关闭 - 团队已停止招募，不再接受新成员申请 */
    closed("已关闭");

    /** 状态中文描述 */
    private final String description;
}
