package com.teamcollab.common.constant;

/**
 * 正则表达式常量类
 * <p>
 * 集中管理系统中所有参数格式校验的正则表达式，
 * 用于 DTO 字段的 {@code @Pattern} 注解和业务逻辑中的格式校验。
 * </p>
 *
 * @author TeamCollab
 */
public final class RegexConstants {

    /**
     * 用户名格式校验正则
     * <p>
     * 规则：由 4-20 个字符组成，仅允许英文字母（大小写）、数字和下划线。
     * </p>
     * <p>示例：user_001, admin123, test_user</p>
     */
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{4,20}$";

    /**
     * 密码格式校验正则
     * <p>
     * 规则：长度为 6-20 个字符，允许任意字符（除换行符外）。
     * </p>
     */
    public static final String PASSWORD_PATTERN = "^.{6,20}$";

    /**
     * 邮箱格式校验正则
     * <p>
     * 规则：标准邮箱格式，包含用户名、@符号和域名，
     * 域名后缀至少为 2 个英文字母。
     * </p>
     * <p>示例：user@example.com, test@mail.company.cn</p>
     */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * 手机号格式校验正则（中国手机号）
     * <p>
     * 规则：以 1 开头，第二位为 3-9，后面跟随 9 位数字，总共 11 位。
     * </p>
     * <p>示例：13800138000, 15912345678</p>
     */
    public static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    /**
     * 私有构造函数，防止外部实例化常量类
     */
    private RegexConstants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }
}
