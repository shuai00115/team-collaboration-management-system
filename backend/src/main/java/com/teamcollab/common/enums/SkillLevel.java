package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 技能等级枚举
 * <p>
 * 定义用户掌握技能的熟练程度，用于团队成员技能管理。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum SkillLevel {

    /** 初级 - 对技能有基本了解，能够完成简单任务 */
    beginner("初级"),

    /** 中级 - 能够独立完成常规任务，有一定经验 */
    intermediate("中级"),

    /** 高级 - 精通该技能，能够解决复杂问题并指导他人 */
    advanced("高级");

    /** 等级中文描述 */
    private final String description;
}
