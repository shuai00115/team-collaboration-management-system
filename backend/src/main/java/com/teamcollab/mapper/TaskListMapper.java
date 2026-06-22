package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.TaskList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 任务列表 Mapper 接口
 * <p>
 * 提供任务列表（看板列）相关的数据库操作，包括项目下任务列表查询（含任务数统计）、
 * 最大排序位置查询、列表位置更新等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface TaskListMapper extends BaseMapper<TaskList> {

    /**
     * 查询指定项目下的所有任务列表（含任务数量统计），按 position 排序
     *
     * @param projectId 项目 ID
     * @return 任务列表，每项含任务数量
     */
    List<Map<String, Object>> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询指定项目下任务列表的最大排序位置
     *
     * @param projectId 项目 ID
     * @return 最大 position 值，无记录时返回 -1
     */
    int maxPosition(@Param("projectId") Long projectId);

    /**
     * 更新指定任务列表的排序位置
     *
     * @param listId   任务列表 ID
     * @param position 新的排序位置
     * @return 受影响的行数
     */
    int updatePosition(@Param("listId") Long listId, @Param("position") Integer position);
}
