package com.teamcollab.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 安全配置类
 * 配置认证与授权规则、JWT 过滤器集成、异常处理等安全策略
 *
 * @author TeamCollab
 */
@Configuration
@EnableWebSecurity // 启用 Spring Security
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 安全过滤器链配置
     * 核心安全策略：禁用 CSRF、无状态会话、基于路径的访问控制、JWT 过滤器集成、统一异常响应
     *
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF 保护（前后端分离 + JWT 无状态，不需要 CSRF）
                .csrf(csrf -> csrf.disable())

                // 2. 设置会话管理为无状态（JWT 认证，不使用服务器端 Session）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 配置路径访问权限
                .authorizeHttpRequests(auth -> auth
                        // 放行认证相关接口（登录、注册等）
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 放行技能相关接口（公共资源）
                        .requestMatchers("/api/v1/skills/**").permitAll()
                        // 放行 API 文档相关路径
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/doc.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        // 放行 WebSocket 连接
                        .requestMatchers("/ws/**").permitAll()
                        // 放行健康检查
                        .requestMatchers("/api/v1/health").permitAll()
                        // 团队列表允许公开访问（GET请求）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/teams").permitAll()
                        // 团队详情允许公开访问（GET请求）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/teams/{teamId}").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 4. 将 JWT 认证过滤器添加到 UsernamePasswordAuthenticationFilter 之前
                // 这样请求会先经过 JWT 过滤器提取用户信息，再走 Spring Security 的权限校验
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. 配置异常处理：返回统一的 JSON 格式错误信息
                .exceptionHandling(exceptions -> exceptions
                        // 未认证（未登录）时返回 401 + JSON
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            Map<String, Object> result = new HashMap<>();
                            result.put("code", 401);
                            result.put("message", "未登录或登录已过期，请重新登录");
                            result.put("data", null);
                            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                        })
                        // 权限不足时返回 403 + JSON
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            Map<String, Object> result = new HashMap<>();
                            result.put("code", 403);
                            result.put("message", "权限不足，无法访问该资源");
                            result.put("data", null);
                            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
                        })
                );

        return http.build();
    }

    /**
     * 认证管理器 Bean
     * 用于处理登录认证请求（如 /auth/login）
     *
     * @param authenticationConfiguration Spring Security 认证配置
     * @return AuthenticationManager 实例
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 密码编码器 Bean
     * 使用 BCrypt 加密算法，自动加盐哈希，安全性高
     * 用于用户注册时密码加密、登录时密码比对
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
