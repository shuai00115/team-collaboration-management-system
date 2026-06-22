package com.teamcollab.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 当前用户参数解析器
 * 自动解析控制器方法中标注了 @CurrentUser 的参数，
 * 从 Spring Security 上下文中获取当前登录用户的 ID 并注入
 *
 * 工作原理：
 * 1. JwtAuthenticationFilter 已将 userId 存入 SecurityContext 的 principal 中
 * 2. 本解析器从 SecurityContext 取出 principal，转为 Long 类型注入
 *
 * @author TeamCollab
 */
@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否需要此解析器处理
     * 只有当参数上标注了 @CurrentUser 注解时才返回 true
     *
     * @param parameter 方法参数元数据
     * @return true 表示由本解析器处理
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查参数上是否有 @CurrentUser 注解
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    /**
     * 解析参数值
     * 从 SecurityContext 中获取已认证的用户信息，提取用户ID
     *
     * @param parameter     方法参数元数据
     * @param mavContainer  ModelAndView 容器（本解析器不使用）
     * @param webRequest    原生 Web 请求
     * @param binderFactory 数据绑定工厂（本解析器不使用）
     * @return 当前登录用户的 Long 类型的 ID
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        // 从 Spring Security 上下文中获取当前的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 未登录或认证信息不存在时返回 null
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        // principal 在 JwtAuthenticationFilter 中被设置为 userId.toString()
        // 将其转换回 Long 类型返回
        String principal = authentication.getPrincipal().toString();
        try {
            return Long.parseLong(principal);
        } catch (NumberFormatException e) {
            // 如果 principal 不是数字格式（理论上不会发生），返回 null
            return null;
        }
    }
}
