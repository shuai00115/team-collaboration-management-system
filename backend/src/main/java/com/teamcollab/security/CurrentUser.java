package com.teamcollab.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前用户注解
 * 用于在控制器方法参数中注入当前登录用户的 ID
 *
 * 使用示例：
 * <pre>
 * @GetMapping("/profile")
 * public Result getProfile(@CurrentUser Long userId) {
 *     // userId 即为当前登录用户的ID
 * }
 * </pre>
 *
 * @author TeamCollab
 */
@Target(ElementType.PARAMETER) // 注解作用于方法参数上
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时保留，以便反射读取
public @interface CurrentUser {

    /**
     * 可选值字段（保留扩展能力）
     *
     * @return 默认为空字符串
     */
    String value() default "";
}
