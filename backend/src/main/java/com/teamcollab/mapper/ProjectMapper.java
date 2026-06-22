package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 项目 Mapper 接口
 * <p>
 * 提供项目相关的数据库操作，包括团队项目列表查询、项目详情查询、项目阶段列表查询等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 分页查询指定团队的项目列表（含任务统计信息）
     *
     * @param page   分页对象
     * @param teamId 团队 ID
     * @return 分页项目列表，每项含任务总数和已完成数
     */
    Page<Map<String, Object>> selectByTeamId(Page<Project> page, @Param("teamId") Long teamId);

    /**
     * 查询项目详细信息（含团队名称和任务统计）
     *
     * @param projectId 项目 ID
     * @return 项目详情 Map
     */
    Map<String, Object> selectProjectDetail(@Param("projectId") Long projectId);

    /**
     * 查询项目的所有阶段（含任务完成率统计）
     *
     * @param projectId 项目 ID
     * @return 阶段列表，每项含任务统计信息
     */
    List<Map<String, Object>> selectStagesByProjectId(@Param("projectId") Long projectId);
}
