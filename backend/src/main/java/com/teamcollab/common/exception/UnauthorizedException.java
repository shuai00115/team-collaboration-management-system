package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;

/**
 * 未授权异常
 * <p>
 * 当用户未登录或Token已过期时抛出此异常。
 * 通常由认证过滤器或拦截器在检测到无效/过期的Token时抛出。
 * 该异常会被 {@link GlobalExceptionHandler} 处理并返回 HTTP 401 状态码。
 * </p>
 *
 * @author TeamCollab
 * @see GlobalExceptionHandler
 */
public class UnauthorizedException extends BusinessException {

    /**
     * 构造未授权异常
     *
     * @param message 错误提示消息，说明未授权的具体原因
     */
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED.getCode(), message);
    }
}
