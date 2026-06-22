package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;

/**
 * 资源不存在异常
 * <p>
 * 当请求的目标资源（如用户、团队、项目、任务等）在数据库中不存在时抛出此异常。
 * 该异常会被 {@link GlobalExceptionHandler} 处理并返回 HTTP 404 状态码。
 * </p>
 *
 * @author TeamCollab
 * @see GlobalExceptionHandler
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * 构造资源不存在异常
     *
     * @param message 错误提示消息，通常会说明哪个资源不存在
     */
    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND.getCode(), message);
    }
}
