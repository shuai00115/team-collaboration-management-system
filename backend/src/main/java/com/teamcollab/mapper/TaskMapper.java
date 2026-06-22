package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 任务 Mapper 接口
 * <p>
 * 提供任务相关的数据库操作，包括任务列表分页查询、任务详情查询、用户任务查询、
 * 任务排序管理、任务统计报表（项目/团队/成员维度）等功能。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 分页查询指定任务列表下的任务（支持多条件筛选和排序）
     *
     * @param page       分页对象
     * @param listId     任务列表 ID
     * @param sortBy     排序字段（需白名单校验）
     * @param priority   优先级筛选
     * @param stageId    阶段筛选
     * @param assigneeId 负责人筛选
     * @param keyword    关键字搜索（匹配标题）
     * @return 分页任务列表
     */
    Page<Map<String, Object>> selectByListId(Page<Task> page, @Param("listId") Long listId,
        @Param("sortBy") String sortBy, @Param("priority") String priority,
        @Param("stageId") Long stageId, @Param("assigneeId") Long assigneeId,
        @Param("keyword") String keyword);

    /**
     * 查询任务详细信息（含关联的项目、团队、阶段、负责人、创建者等完整信息）
     *
     * @param taskId 任务 ID
     * @return 任务详情 Map
     */
    Map<String, Object> selectTaskDetail(@Param("taskId") Long taskId);

    /**
     * 分页查询用户相关的任务（作为负责人或创建者，支持多条件筛选）
     *
     * @param page     分页对象
     * @param userId   用户 ID
     * @param priority 优先级筛选
     * @param listId   任务列表筛选
     * @param teamId   团队筛选
     * @return 分页任务列表
     */
    Page<Map<String, Object>> selectUserTasks(Page<Task> page, @Param("userId") Long userId,
        @Param("priority") String priority, @Param("listId") Long listId, @Param("teamId") Long teamId);

    /**
     * 查询指定任务列表下任务的最大排序位置
     *
     * @param listId 任务列表 ID
     * @return 最大 position 值，无记录时返回 -1
     */
    int maxPosition(@Param("listId") Long listId);

    /**
     * 更新任务的排序位置和所属列表（用于拖拽移动任务）
     *
     * @param taskId   任务 ID
     * @param position 新的排序位置
     * @param listId   目标列表 ID
     * @return 受影响的行数
     */
    int updatePosition(@Param("taskId") Long taskId, @Param("position") Integer position, @Param("listId") Long listId);

    /**
     * 查询项目的任务统计信息（总数和已完成数）
     *
     * @param projectId 项目 ID
     * @return 统计信息 Map（total, completed）
     */
    Map<String, Object> selectTaskStats(@Param("projectId") Long projectId);

    /**
     * 查询团队维度的任务统计信息
     *
     * @param teamId    团队 ID
     * @param projectId 项目 ID（可选，为 null 时统计团队下所有项目）
     * @return 统计信息 Map
     */
    Map<String, Object> selectTeamTaskStats(@Param("teamId") Long teamId, @Param("projectId") Long projectId);

    /**
     * 查询团队各成员的任务统计信息（分配数、完成数、逾期数）
     *
     * @param teamId    团队 ID
     * @param projectId 项目 ID（可选）
     * @return 成员统计列表
     */
    List<Map<String, Object>> selectMemberStats(@Param("teamId") Long teamId, @Param("projectId") Long projectId);

    /**
     * 查询单个成员的详细任务统计信息
     *
     * @param teamId 团队 ID
     * @param userId 用户 ID
     * @return 成员详细统计 Map
     */
    Map<String, Object> selectMemberDetailStats(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
