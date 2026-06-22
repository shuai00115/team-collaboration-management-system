package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.Stage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 阶段（Stage）Mapper 接口
 * <p>
 * 提供项目阶段相关的数据库操作，包括阶段列表查询（含任务统计）、
 * 阶段下任务数统计、阶段最大排序索引查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface StageMapper extends BaseMapper<Stage> {

    /**
     * 查询指定项目的所有阶段（含任务数量统计）
     *
     * @param projectId 项目 ID
     * @return 阶段列表，每项含任务总数
     */
    List<Map<String, Object>> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计指定阶段下的任务数量
     *
     * @param stageId 阶段 ID
     * @return 任务数量
     */
    int countTasksByStageId(@Param("stageId") Long stageId);

    /**
     * 查询指定项目下阶段的最大排序索引
     *
     * @param projectId 项目 ID
     * @return 最大排序索引，无记录时返回 -1
     */
    int maxOrderIndex(@Param("projectId") Long projectId);
}
