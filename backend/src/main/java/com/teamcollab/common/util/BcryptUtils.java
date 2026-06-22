package com.teamcollab.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Bcrypt 密码加密工具类
 * <p>
 * 基于 Spring Security 提供的 {@link BCryptPasswordEncoder} 实现密码的加密和校验。
 * 采用 BCrypt 算法，自动生成随机盐值并嵌入到加密结果中，保证相同密码每次加密结果不同。
 * BCrypt 的强度因子（cost factor）默认为 10，在安全和性能之间取得了平衡。
 * </p>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *     <li>所有静态方法均为线程安全，可在多线程环境下使用</li>
 *     <li>加密后的密文长度固定为 60 个字符</li>
 *     <li>每次调用 {@link #encode(String)} 对相同密码生成的密文均不同</li>
 *     <li>本工具类不可实例化，构造函数为私有</li>
 * </ul>
 *
 * @author TeamCollab
 */
public final class BcryptUtils {

    /** BCrypt 加密器实例，强度因子使用默认值 10 */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 私有构造函数，防止外部实例化工具类
     */
    private BcryptUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 对原始密码进行 BCrypt 加密
     * <p>
     * 使用自动生成的随机盐值对密码进行不可逆加密，
     * 加密结果中已包含盐值，后续可直接使用 {@link #matches(String, String)} 校验。
     * </p>
     *
     * @param rawPassword 原始明文密码，不可为空
     * @return BCrypt 加密后的密文，长度为 60 个字符
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验原始密码与加密后的密文是否匹配
     * <p>
     * 从密文中自动提取盐值，对原始密码进行相同算法加密后与密文比较。
     * </p>
     *
     * @param rawPassword     原始明文密码（用户输入的密码）
     * @param encodedPassword BCrypt 加密后的密文（数据库中存储的密码）
     * @return true - 密码匹配；false - 密码不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
