package com.teamcollab.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 在每个请求到达控制器之前拦截，从 Authorization 头中提取并验证 JWT 令牌，
 * 将验证通过的用户信息设置到 Spring Security 上下文中
 * 继承 OncePerRequestFilter 确保每个请求只过滤一次
 *
 * @author TeamCollab
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * HTTP 请求头中的 Authorization 前缀
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 过滤器核心逻辑
     * 1. 从请求头中获取 Authorization 的值
     * 2. 校验是否以 "Bearer " 开头
     * 3. 提取并验证 JWT 令牌
     * 4. 将用户信息封装为 Authentication 对象，存入 SecurityContext
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中获取 JWT 令牌
        String token = extractTokenFromRequest(request);

        // 2. 如果令牌存在且有效，进行认证处理
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                // 从令牌中提取用户信息
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // 构建权限列表，角色需要加上 "ROLE_" 前缀以符合 Spring Security 规范
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                // 创建认证令牌对象
                // principal 设置为 userId 字符串，credentials 置空（JWT 验证已通过），authorities 为角色权限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(), // principal：用户ID
                                null, // credentials：JWT 无需密码
                                Collections.singletonList(authority) // authorities：权限列表
                        );

                // 将认证信息存入 SecurityContext，标记当前请求已认证
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功 - 用户ID: {}, 用户名: {}, 角色: {}", userId, username, role);
            } catch (Exception e) {
                log.error("JWT认证处理失败: {}", e.getMessage());
                // 认证失败时清空上下文，确保安全
                SecurityContextHolder.clearContext();
            }
        } else if (StringUtils.hasText(token)) {
            log.warn("JWT令牌验证失败或已过期");
        }

        // 3. 继续执行后续过滤器链（无论是否携带令牌都放行，由 Spring Security 授权规则决定访问权限）
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求中提取 JWT 令牌
     * 检查 Authorization 请求头，提取 "Bearer " 之后的令牌字符串
     *
     * @param request HTTP 请求
     * @return JWT 令牌字符串，如果不存在则返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // 检查请求头是否存在且以 "Bearer " 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()); // 去除前缀，返回纯令牌
        }
        return null;
    }
}
