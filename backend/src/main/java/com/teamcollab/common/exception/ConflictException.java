package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;

/**
 * 资源冲突异常
 * <p>
 * 当操作与现有数据存在冲突时抛出此异常，例如：
 * 用户名或邮箱重复、团队成员已满、申请已处理等场景。
 * 该异常会被 {@link GlobalExceptionHandler} 处理并返回 HTTP 409 状态码。
 * </p>
 *
 * @author TeamCollab
 * @see GlobalExceptionHandler
 */
public class ConflictException extends BusinessException {

    /**
     * 构造资源冲突异常
     *
     * @param message 错误提示消息，说明具体的冲突原因
     */
    public ConflictException(String message) {
        super(ErrorCode.CONFLICT.getCode(), message);
    }
}
