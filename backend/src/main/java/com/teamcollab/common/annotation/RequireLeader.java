package com.teamcollab.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要队长权限的注解
 * <p>
 * 标注在 Controller 方法上，表示该方法需要当前用户是目标团队的队长才能执行。
 * 通过 AOP 切面或拦截器解析该注解，自动校验用户是否拥有队长身份，
 * 校验失败则抛出 {@link com.teamcollab.common.exception.ForbiddenException}。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   // 默认从请求参数中获取 teamId
 *   @RequireLeader
 *   @PostMapping("/team/{teamId}/approve")
 *   public Result<Void> approve(@PathVariable Long teamId, ...) { }
 *
 *   // 指定 teamId 参数名
 *   @RequireLeader("teamId")
 *   @PostMapping("/approve")
 *   public Result<Void> approve(@RequestParam Long teamId, ...) { }
 * }</pre>
 *
 * @author TeamCollab
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLeader {

    /**
     * 团队ID参数名称
     * <p>
     * 指定请求中表示团队ID的参数名，AOP 切面将根据此名称从
     * 请求参数或路径变量中获取团队ID，用于校验用户是否为该团队的队长。
     * </p>
     *
     * @return 参数名称，默认为 "teamId"
     */
    String value() default "teamId";
}
