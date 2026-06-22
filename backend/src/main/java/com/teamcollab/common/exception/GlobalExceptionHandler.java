package com.teamcollab.common.exception;

import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用 {@code @RestControllerAdvice} 拦截所有 Controller 层抛出的异常，
 * 将异常转换为统一的 {@link Result} 响应格式返回给前端。
 * 支持业务异常、参数校验异常、数据库异常、权限异常等多种异常类型的处理。
 * </p>
 *
 * <p><b>处理策略：</b></p>
 * <ul>
 *     <li>业务异常 {@link BusinessException}：直接返回异常中携带的错误码和消息</li>
 *     <li>参数校验异常：收集所有字段的校验失败信息，统一返回</li>
 *     <li>数据库异常：解析异常信息，返回用户友好的提示</li>
 *     <li>未知异常：记录日志并返回通用错误，避免暴露服务端细节</li>
 * </ul>
 *
 * @author TeamCollab
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 处理自定义业务异常
     * <p>
     * 捕获 {@link BusinessException} 及其所有子类异常，
     * 将异常中的错误码和消息提取并封装到响应中。
     * </p>
     *
     * @param e 业务异常
     * @return 包含错误码和消息的统一响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理方法参数校验失败异常（用于 @RequestBody 校验）
     * <p>
     * 当 Controller 方法参数使用 {@code @Valid} 或 {@code @Validated} 注解，
     * 且请求体中的字段校验失败时，Spring 会抛出此异常。
     * 遍历所有字段错误，拼接为可读的错误信息返回。
     * </p>
     *
     * @param e 方法参数校验异常
     * @return HTTP 400 响应，包含所有字段的校验失败信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), errorMsg);
    }

    /**
     * 处理绑定异常（用于 @ModelAttribute 或表单参数校验）
     * <p>
     * 当使用 {@code @Validated} 注解校验 Query 参数或表单参数失败时抛出此异常。
     * 处理方式与 {@link MethodArgumentNotValidException} 相同。
     * </p>
     *
     * @param e 绑定异常
     * @return HTTP 400 响应，包含所有字段的校验失败信息
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", errorMsg);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), errorMsg);
    }

    // ==================== HTTP 请求异常 ====================

    /**
     * 处理 HTTP 请求方法不支持异常
     * <p>
     * 当客户端使用错误的 HTTP 方法（如用 GET 访问 POST 接口）时，
     * Spring 会抛出此异常。返回 405 状态码并提示支持的请求方法。
     * </p>
     *
     * @param e 请求方法不支持异常
     * @return HTTP 405 响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        String msg = "不支持的请求方式: " + e.getMethod() + "，支持的请求方式: " + e.getSupportedHttpMethods();
        log.warn("请求方法不支持: {}", msg);
        return Result.error(405, msg);
    }

    // ==================== 数据库异常 ====================

    /**
     * 处理数据库唯一约束冲突异常
     * <p>
     * 当执行插入或更新操作违反数据库唯一约束时，MyBatis/Spring 会抛出此异常。
     * 通过解析异常消息中的关键信息，自动识别冲突类型并返回友好的中文提示。
     * </p>
     *
     * <p><b>识别的冲突类型：</b></p>
     * <ul>
     *     <li>用户名重复（包含 "username" 关键字）</li>
     *     <li>邮箱重复（包含 "email" 关键字）</li>
     *     <li>技能标签重复（包含 "skill" 关键字）</li>
     *     <li>其他冲突（兜底处理）</li>
     * </ul>
     *
     * @param e 唯一键冲突异常
     * @return HTTP 409 响应，包含具体的冲突提示信息
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据库唯一约束冲突: {}", e.getMessage());
        String message = e.getMessage();
        ErrorCode errorCode;

        // 根据异常消息中的关键字段名称，匹配对应的错误码
        if (message != null && message.contains("username")) {
            errorCode = ErrorCode.USERNAME_EXISTS;
        } else if (message != null && message.contains("email")) {
            errorCode = ErrorCode.EMAIL_EXISTS;
        } else if (message != null && message.contains("skill")) {
            errorCode = ErrorCode.SKILL_EXISTS;
        } else {
            // 无法识别具体冲突类型，返回通用冲突错误
            errorCode = ErrorCode.CONFLICT;
        }

        return Result.error(errorCode);
    }

    /**
     * 处理数据访问异常（通用数据库异常）
     * <p>
     * 当发生非唯一键冲突的其他数据库异常时（如连接超时、SQL语法错误等），
     * 返回通用的数据库异常提示，避免暴露内部SQL细节。
     * </p>
     *
     * @param e 数据访问异常
     * @return HTTP 500 响应
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleDataAccessException(DataAccessException e) {
        log.error("数据库访问异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "数据库异常，请稍后重试");
    }

    // ==================== 权限异常 ====================

    /**
     * 处理 Spring Security 访问拒绝异常
     * <p>
     * 当已认证的用户尝试访问其无权限的资源时抛出此异常。
     * 返回 HTTP 403 响应。
     * </p>
     *
     * @param e 访问拒绝异常
     * @return HTTP 403 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN);
    }

    // ==================== 兜底异常 ====================

    /**
     * 处理所有未被其他处理器捕获的异常（兜底处理）
     * <p>
     * 作为异常处理的最后一道防线，捕获所有未预见的运行时异常。
     * 记录完整的异常堆栈信息以便排查问题，向前端返回通用错误提示，
     * 不暴露服务端的内部错误细节。
     * </p>
     *
     * @param e 未预见的异常
     * @return HTTP 500 响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统未知异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), "服务器内部错误，请联系管理员");
    }
}
