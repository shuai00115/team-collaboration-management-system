package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常基类
 * <p>
 * 所有业务异常的父类，包含错误码和错误信息。
 * 当业务逻辑出现不符合预期的情况时，应抛出此异常或其子类。
 * 由 {@link GlobalExceptionHandler} 统一捕获并转换为前端可识别的响应格式。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   // 使用预定义错误码
 *   throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 *
 *   // 使用错误码并附加自定义消息
 *   throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户ID：" + userId);
 *
 *   // 使用自定义错误码和消息
 *   throw new BusinessException(50001, "自定义错误信息");
 * }</pre>
 *
 * @author TeamCollab
 * @see GlobalExceptionHandler
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final int code;

    /** 错误信息 */
    private final String message;

    /**
     * 通过错误码枚举构造业务异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 通过错误码枚举构造业务异常，并覆盖默认的提示消息
     *
     * @param errorCode 错误码枚举
     * @param message   自定义的错误提示消息，覆盖枚举中默认的 message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }

    /**
     * 通过自定义的错误码和消息构造业务异常
     *
     * @param code    自定义错误码
     * @param message 错误提示消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
