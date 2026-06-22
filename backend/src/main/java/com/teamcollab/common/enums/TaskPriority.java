package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务优先级枚举
 * <p>
 * 定义任务的紧急程度，用于任务排序和资源分配。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum TaskPriority {

    /** 高优先级 - 需要优先处理，通常为紧急任务 */
    high("高"),

    /** 中优先级 - 常规任务，按正常排期处理 */
    medium("中"),

    /** 低优先级 - 可延后处理的任务 */
    low("低");

    /** 优先级中文描述 */
    private final String description;
}
