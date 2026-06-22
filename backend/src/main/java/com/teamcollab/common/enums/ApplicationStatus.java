package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请状态枚举
 * <p>
 * 定义用户申请加入团队时的审核状态。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatus {

    /** 待审核 - 申请已提交，等待队长审核 */
    pending("待审核"),

    /** 已通过 - 申请已被队长批准，用户已成为团队成员 */
    approved("已通过"),

    /** 已拒绝 - 申请已被队长拒绝 */
    rejected("已拒绝");

    /** 状态中文描述 */
    private final String description;
}
