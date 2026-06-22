package com.teamcollab.common.util;

import com.teamcollab.common.enums.ErrorCode;
import com.teamcollab.common.exception.BusinessException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * SQL 安全工具类
 * <p>
 * 使用白名单机制防止 SQL 注入攻击，特别是在动态排序（ORDER BY）场景下。
 * 由于 MyBatis 的 #{ } 占位符无法用于 ORDER BY 子句，
 * 前端传入的排序字段必须经过白名单校验后才能拼接进 SQL 语句。
 * </p>
 *
 * <p><b>设计原则：</b></p>
 * <ul>
 *     <li>仅允许白名单内的字段用于排序，拒绝所有非白名单字段</li>
 *     <li>白名单字段名与数据库列名保持一致</li>
 *     <li>校验不通过时直接抛出业务异常，阻断非法请求</li>
 * </ul>
 *
 * @author TeamCollab
 */
public final class SqlUtils {

    /** 任务模块允许的排序字段白名单 */
    private static final Set<String> ALLOWED_SORT_FIELDS_FOR_TASKS;

    /** 团队模块允许的排序字段白名单 */
    private static final Set<String> ALLOWED_SORT_FIELDS_FOR_TEAMS;

    static {
        // 初始化任务排序字段白名单
        Set<String> taskFields = new LinkedHashSet<>();
        taskFields.add("position");
        taskFields.add("priority");
        taskFields.add("due_date");
        taskFields.add("created_at");
        ALLOWED_SORT_FIELDS_FOR_TASKS = Collections.unmodifiableSet(taskFields);

        // 初始化团队排序字段白名单
        Set<String> teamFields = new LinkedHashSet<>();
        teamFields.add("created_at");
        teamFields.add("updated_at");
        teamFields.add("name");
        ALLOWED_SORT_FIELDS_FOR_TEAMS = Collections.unmodifiableSet(teamFields);
    }

    /**
     * 私有构造函数，防止外部实例化工具类
     */
    private SqlUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 校验排序字段是否在允许的白名单内
     * <p>
     * 如果传入的排序字段不在白名单中，则抛出参数错误的业务异常，
     * 防止恶意构造的排序字段注入 SQL。
     * </p>
     *
     * @param sortBy         前端传入的排序字段名
     * @param allowedFields  该业务模块允许的排序字段白名单
     * @throws BusinessException 如果排序字段不在白名单内
     */
    public static void validateSortField(String sortBy, Set<String> allowedFields) {
        if (sortBy == null || sortBy.isEmpty()) {
            return; // 未指定排序字段，允许通过
        }
        if (allowedFields == null || !allowedFields.contains(sortBy)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                    "不允许的排序字段: " + sortBy + "，允许的字段: " + allowedFields);
        }
    }

    /**
     * 获取任务模块的允许排序字段集合
     * <p>
     * 包含字段：position（排序位置）, priority（优先级）, due_date（截止日期）, created_at（创建时间）。
     * </p>
     *
     * @return 不可修改的任务排序字段白名单
     */
    public static Set<String> getTaskSortFields() {
        return ALLOWED_SORT_FIELDS_FOR_TASKS;
    }

    /**
     * 获取团队模块的允许排序字段集合
     * <p>
     * 包含字段：created_at（创建时间）, updated_at（更新时间）, name（团队名称）。
     * </p>
     *
     * @return 不可修改的团队排序字段白名单
     */
    public static Set<String> getTeamSortFields() {
        return ALLOWED_SORT_FIELDS_FOR_TEAMS;
    }
}
