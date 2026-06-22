package com.teamcollab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 阶段状态枚举
 * <p>
 * 定义项目中各阶段的执行状态，用于跟踪项目进度。
 * </p>
 *
 * @author TeamCollab
 */
@Getter
@AllArgsConstructor
public enum StageStatus {

    /** 未开始 - 阶段尚未启动 */
    not_started("未开始"),

    /** 进行中 - 阶段正在执行 */
    in_progress("进行中"),

    /** 已完成 - 阶段已全部完成 */
    completed("已完成");

    /** 状态中文描述 */
    private final String description;
}
