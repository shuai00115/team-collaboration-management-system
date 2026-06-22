package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;

/**
 * 权限不足异常
 * <p>
 * 当用户已登录但角色或权限不足以执行当前操作时抛出此异常。
 * 该异常会被 {@link GlobalExceptionHandler} 处理并返回 HTTP 403 状态码。
 * </p>
 *
 * @author TeamCollab
 * @see GlobalExceptionHandler
 */
public class ForbiddenException extends BusinessException {

    /**
     * 构造权限不足异常
     *
     * @param message 错误提示消息，说明缺少何种权限
     */
    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN.getCode(), message);
    }
}
