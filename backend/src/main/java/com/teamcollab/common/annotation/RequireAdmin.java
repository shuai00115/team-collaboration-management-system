package com.teamcollab.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要管理员权限的注解
 * <p>
 * 标注在 Controller 方法或类上，表示该方法（或类中的所有方法）需要
 * 当前用户拥有系统管理员（admin）角色才能执行。
 * 通过 AOP 切面或拦截器解析该注解，自动校验当前登录用户的角色，
 * 校验失败则抛出 {@link com.teamcollab.common.exception.ForbiddenException}。
 * </p>
 *
 * <p><b>标注在方法上：</b>仅对当前方法生效</p>
 * <p><b>标注在类上：</b>对类中所有方法生效</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   // 标注在单个方法上
 *   @RequireAdmin
 *   @GetMapping("/admin/users")
 *   public Result<PageResult<UserVO>> listUsers(...) { }
 *
 *   // 标注在整个类上
 *   @RequireAdmin
 *   @RestController
 *   @RequestMapping("/admin")
 *   public class AdminController { }
 * }</pre>
 *
 * @author TeamCollab
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
    // 无需参数，仅作为标记注解使用
}
