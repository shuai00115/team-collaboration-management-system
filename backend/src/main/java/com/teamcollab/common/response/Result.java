package com.teamcollab.common.response;

import com.teamcollab.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 所有 Controller 接口的返回值均使用此类进行包装，
 * 确保前端能够以统一的格式解析和处理服务端响应。
 * 支持泛型，可以携带任意类型的业务数据。
 * </p>
 *
 * <p><b>响应格式示例：</b></p>
 * <pre>{@code
 *   // 成功响应（无数据）
 *   { "code": 200, "msg": "操作成功", "data": null }
 *
 *   // 成功响应（带数据）
 *   { "code": 200, "msg": "查询成功", "data": { ... } }
 *
 *   // 失败响应
 *   { "code": 400, "msg": "请求参数错误", "data": null }
 * }</pre>
 *
 * @param <T> 响应中携带的数据类型
 * @author TeamCollab
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码，200 表示成功，其他表示各种错误 */
    @Schema(description = "状态码", example = "200")
    private int code;

    /** 响应消息，成功时为"操作成功"，失败时为具体错误描述 */
    @Schema(description = "响应消息", example = "操作成功")
    private String msg;

    /** 响应数据，可为任意类型，无数据时为 null */
    @Schema(description = "响应数据")
    private T data;

    // ==================== 成功响应快捷方法 ====================

    /**
     * 构建成功的响应（无数据）
     * <p>适用于增删改操作，无需返回具体数据时使用。</p>
     *
     * @param <T> 数据类型
     * @return code=200, msg="操作成功", data=null
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建成功的响应（携带数据）
     * <p>适用于查询操作，需要返回查询结果时使用。</p>
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return code=200, msg="操作成功", data=传入的数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构建成功的响应（自定义消息，无数据）
     * <p>适用于增删改等无需返回数据但需要自定义成功提示的场景。</p>
     *
     * @param msg  自定义成功消息
     * @param <T>  数据类型（通常为 Void）
     * @return code=200, msg=自定义消息, data=null
     */
    public static <T> Result<T> success(String msg) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), msg, null);
    }

    /**
     * 构建成功的响应（自定义消息及携带数据）
     * <p>适用于需要自定义成功提示消息的场景。</p>
     *
     * @param msg  自定义成功消息
     * @param data 响应数据
     * @param <T>  数据类型
     * @return code=200, msg=自定义消息, data=传入的数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), msg, data);
    }

    // ==================== 失败响应快捷方法 ====================

    /**
     * 构建失败的响应（通过错误码和消息）
     * <p>适用于需要自定义错误码和消息的场景，如第三方接口调用失败等。</p>
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  数据类型（通常为 Void）
     * @return 包含错误码和消息的响应
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 构建失败的响应（通过预定义错误码枚举）
     * <p>适用于使用预定义错误码的场景，消息会自动从枚举中获取。</p>
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型（通常为 Void）
     * @return 包含预定义错误码和默认消息的响应
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 构建失败的响应（通过预定义错误码枚举，并覆盖默认消息）
     * <p>适用于需要复用错误码但提供更具体的错误描述的场景。</p>
     *
     * @param errorCode 错误码枚举（code 值会被保留）
     * @param msg       自定义错误消息（覆盖枚举中的默认 message）
     * @param <T>       数据类型（通常为 Void）
     * @return 包含预定义错误码和自定义消息的响应
     */
    public static <T> Result<T> error(ErrorCode errorCode, String msg) {
        return new Result<>(errorCode.getCode(), msg, null);
    }
}
