package com.teamcollab.common.util;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Map 类型安全取值工具类
 * <p>
 * 解决 MySQL Connector/J 9.x 对 BIGINT UNSIGNED 列返回 BigInteger 的问题，
 * 提供安全的类型转换方法，避免 ClassCastException。
 * </p>
 *
 * @author teamcollab
 */
public final class MapUtils {

    private MapUtils() {}

    /** 从 Map 中安全获取 Long 值 */
    public static Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigInteger) return ((BigInteger) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }

    /** 从 Map 中安全获取 Integer 值 */
    public static Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigInteger) return ((BigInteger) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }

    /** 从 Map 中安全获取 String 值 */
    public static String getStr(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /** 从 Map 中安全获取 LocalDateTime 值 */
    public static LocalDateTime getDateTime(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    /** 从 Map 中安全获取 LocalDate 值（兼容 MySQL DATE 列的多种 JDBC 返回类型） */
    public static LocalDate getLocalDate(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof Date) return ((Date) value).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return null;
    }
}
