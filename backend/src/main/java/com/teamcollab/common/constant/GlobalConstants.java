package com.teamcollab.common.constant;

/**
 * 全局常量定义类
 * <p>
 * 集中管理系统级别的常量，包括 HTTP 头信息、分页默认参数、
 * Token 配置等，避免在代码中硬编码魔法值。
 * </p>
 *
 * @author TeamCollab
 */
public final class GlobalConstants {

    /**
     * Token 传递的 HTTP 请求头名称
     * <p>前端在请求头中使用此名称传递 JWT Token。</p>
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     * <p>JWT Token 在请求头中的前缀，标准格式为 "Bearer {token}"。</p>
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 默认页码
     * <p>当前端未指定页码时，默认查询第 1 页。</p>
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页记录数
     * <p>当前端未指定每页显示记录数时，默认显示 10 条。</p>
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页记录数
     * <p>限制单次查询返回的最大记录数，防止恶意请求导致数据库压力过大。</p>
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Token 过期时间（秒）
     * <p>默认 604800 秒即 7 天。登录成功后颁发的 JWT Token 在此时长后失效。</p>
     */
    public static final long TOKEN_EXPIRATION = 604800;

    /**
     * 私有构造函数，防止外部实例化常量类
     */
    private GlobalConstants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }
}
