package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 * <p>
 * 定义系统中各类通知消息的类型，用于消息推送和用户提醒。
 * 每种类型对应不同的业务场景和通知内容模板。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum NotificationType {

    /** 入队申请已通过 - 用户申请加入团队被队长批准 */
    join_approved("入队申请已通过"),

    /** 入队申请已拒绝 - 用户申请加入团队被队长拒绝 */
    join_rejected("入队申请已拒绝"),

    /** 新的入队申请 - 有用户申请加入团队，通知队长审核 */
    new_join_request("新的入队申请"),

    /** 任务分配 - 用户被分配了新的任务 */
    task_assigned("任务分配"),

    /** 任务更新 - 用户负责的任务信息发生了变更 */
    task_updated("任务更新"),

    /** 截止日期提醒 - 任务即将到期的提醒 */
    due_reminder("截止日期提醒"),

    /** 阶段超期 - 项目阶段已超出计划截止日期 */
    stage_overdue("阶段超期"),

    /** 成员被移除 - 用户被从团队中移除 */
    member_removed("成员被移除");

    /** 通知类型中文描述 */
    private final String description;
}
