package com.teamcollab.config;

import com.teamcollab.security.CurrentUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 配置类
 * 配置跨域请求、自定义参数解析器等
 *
 * @author TeamCollab
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentUserResolver currentUserResolver;

    /**
     * 配置跨域请求映射
     * 允许所有来源、方法、请求头，支持携带凭证，预检请求缓存1小时
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("*") // 允许所有来源（与allowCredentials兼容）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带凭证（Cookie）
                .maxAge(3600); // 预检请求缓存时间（秒）
    }

    /**
     * 注册自定义参数解析器
     * 将 @CurrentUser 注解的参数解析器添加到 Spring MVC 的参数解析器链中
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}
