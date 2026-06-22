package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 * <p>
 * 定义系统中所有业务操作的错误码和对应的中文提示信息。
 * 错误码采用分段规则：2xx为成功，4xx为HTTP标准错误，5xx为服务端错误，
 * 20000+为业务自定义错误，其中前两位数字代表模块编号。
 * </p>
 *
 * <ul>
 *     <li>2xxxx：用户/认证模块错误</li>
 *     <li>3xxxx：团队模块错误</li>
 *     <li>4xxxx：项目/任务模块错误</li>
 *     <li>5xxxx：技能标签模块错误</li>
 * </ul>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未登录或Token已过期 */
    UNAUTHORIZED(401, "未登录或Token已过期"),

    /** 无权限访问 */
    FORBIDDEN(403, "无权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 资源冲突 */
    CONFLICT(409, "资源冲突"),

    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ==================== 用户/认证模块 (2xxxx) ====================

    /** 用户名已存在 */
    USERNAME_EXISTS(20001, "用户名已存在"),

    /** 邮箱已被注册 */
    EMAIL_EXISTS(20002, "邮箱已被注册"),

    /** 原密码错误 */
    OLD_PASSWORD_ERROR(20003, "原密码错误"),

    /** 用户已被禁用 */
    USER_DISABLED(20004, "用户已被禁用"),

    // ==================== 团队模块 (3xxxx) ====================

    /** 团队不存在 */
    TEAM_NOT_FOUND(30001, "团队不存在"),

    /** 团队已满员 */
    TEAM_FULL(30002, "团队已满员"),

    /** 非团队成员，无权操作 */
    NOT_TEAM_MEMBER(30003, "非团队成员，无权操作"),

    /** 非队长，无权操作 */
    NOT_TEAM_LEADER(30004, "非队长，无权操作"),

    /** 已经申请过该团队 */
    ALREADY_APPLIED(30005, "已经申请过该团队"),

    /** 已是该团队成员 */
    ALREADY_MEMBER(30006, "已是该团队成员"),

    /** 不能移除队长 */
    CANNOT_REMOVE_LEADER(30007, "不能移除队长"),

    /** 该申请已处理 */
    APPLICATION_ALREADY_PROCESSED(30008, "该申请已处理"),

    // ==================== 项目/任务模块 (4xxxx) ====================

    /** 项目不存在 */
    PROJECT_NOT_FOUND(40001, "项目不存在"),

    /** 阶段不存在 */
    STAGE_NOT_FOUND(40002, "阶段不存在"),

    /** 阶段下仍有未完成任务，无法删除 */
    STAGE_HAS_TASKS(40003, "阶段下仍有未完成任务，无法删除"),

    /** 任务列表不存在 */
    TASK_LIST_NOT_FOUND(40004, "任务列表不存在"),

    /** 默认列表不可删除 */
    DEFAULT_LIST_CANNOT_DELETE(40005, "默认列表不可删除"),

    /** 任务不存在 */
    TASK_NOT_FOUND(40006, "任务不存在"),

    // ==================== 技能标签模块 (5xxxx) ====================

    /** 技能标签不存在 */
    SKILL_NOT_FOUND(50001, "技能标签不存在"),

    /** 技能标签已存在 */
    SKILL_EXISTS(50002, "技能标签已存在");

    /** 错误码 */
    private final int code;

    /** 错误信息（中文描述） */
    private final String message;
}
