package com.teamcollab.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 令牌提供器
 * 负责 JWT 令牌的生成、解析和验证，用于无状态身份认证
 *
 * @author TeamCollab
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 签名密钥，从配置文件中读取（生产环境务必使用足够长的随机字符串）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 过期时间（秒），从配置文件中读取，默认7200秒（2小时）
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 从密钥字符串派生出的 HMAC 签名密钥对象
     */
    private SecretKey key;

    /**
     * 初始化方法
     * 在 Bean 属性注入完成后，从 Base64 编码的密钥字符串创建 HMAC 密钥对象
     */
    @PostConstruct
    public void init() {
        // 将密钥字符串转换为字节数组，再生成 HMAC-SHA 签名密钥
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 JWT 令牌
     * 将用户 ID、用户名、角色信息编码到令牌中，并设置过期时间
     *
     * @param userId   用户主键ID
     * @param username 用户名
     * @param role     用户角色（如 ADMIN、MEMBER 等）
     * @return 生成的 JWT 令牌字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000); // 过期时间 = 当前时间 + 配置的秒数

        return Jwts.builder()
                .subject(userId.toString()) // 主题：用户ID（作为令牌持有者标识）
                .claim("username", username) // 自定义声明：用户名
                .claim("role", role) // 自定义声明：用户角色
                .issuedAt(now) // 签发时间
                .expiration(expiryDate) // 过期时间
                .signWith(key) // 使用 HMAC 密钥签名
                .compact(); // 构建令牌字符串
    }

    /**
     * 从 JWT 令牌中提取用户 ID
     *
     * @param token JWT 令牌字符串
     * @return 用户主键ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject()); // subject 中存储了 userId
    }

    /**
     * 从 JWT 令牌中提取用户名
     *
     * @param token JWT 令牌字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class); // 从自定义声明中获取用户名
    }

    /**
     * 从 JWT 令牌中提取用户角色
     *
     * @param token JWT 令牌字符串
     * @return 角色名称
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class); // 从自定义声明中获取角色
    }

    /**
     * 验证 JWT 令牌是否有效
     * 尝试解析令牌，如果解析成功则返回 true，失败则返回 false
     *
     * @param token JWT 令牌字符串
     * @return true 表示令牌有效，false 表示无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 尝试解析，解析失败会抛出异常
            return true;
        } catch (Exception e) {
            // 令牌无效（签名错误、已过期、格式错误等）
            return false;
        }
    }

    /**
     * 私有方法：从 JWT 令牌中解析出声明体（Claims）
     * 使用签名密钥验证令牌完整性，并返回其中存储的数据
     *
     * @param token JWT 令牌字符串
     * @return 包含令牌数据的 Claims 对象
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // 设置签名验证密钥
                .build()
                .parseSignedClaims(token) // 解析已签名的令牌
                .getPayload(); // 获取负载（claims）
    }
}
