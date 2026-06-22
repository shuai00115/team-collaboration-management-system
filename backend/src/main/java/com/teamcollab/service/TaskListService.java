package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.tasklist.*;
import com.teamcollab.entity.TaskList;

import java.util.List;
import java.util.Map;

/**
 * 任务列表服务接口
 * <p>
 * 提供看板视图中任务列表（列）的查询、创建、更新、删除和排序功能。
 * 任务列表是看板中的一列，包含多个任务卡片。
 * 任务列表操作需要队长权限。
 * </p>
 *
 * @author teamcollab
 */
public interface TaskListService extends IService<TaskList> {

    /**
     * 获取项目的所有任务列表（看板列）
     * <p>
     * 查询指定项目下的所有任务列表，每个列表附带任务数量统计。
     * </p>
     *
     * @param projectId 项目ID
     * @return 任务列表响应列表，包含各列的任务计数
     */
    List<TaskListResponse> getProjectLists(Long projectId);

    /**
     * 创建自定义任务列表
     * <p>
     * 在指定项目下创建新的任务列表（看板列），仅队长有权执行。
     * </p>
     *
     * @param projectId 项目ID
     * @param userId    当前操作用户ID（需为队长）
     * @param request   创建任务列表请求，包含列表名称等
     * @return 包含新创建任务列表基本信息的键值对（如列表ID、名称等）
     */
    Map<String, Object> createTaskList(Long projectId, Long userId, CreateTaskListRequest request);

    /**
     * 更新任务列表名称
     * <p>
     * 修改任务列表的名称，仅队长有权执行。
     * </p>
     *
     * @param listId  任务列表ID
     * @param userId  当前操作用户ID（需为队长）
     * @param request 更新任务列表请求，包含新的列表名称
     */
    void updateTaskList(Long listId, Long userId, UpdateTaskListRequest request);

    /**
     * 删除任务列表
     * <p>
     * 删除指定任务列表，仅队长有权执行。
     * 系统默认任务列表（待办/进行中/已完成）不可删除。
     * 删除操作会级联删除该列表下的所有任务。
     * </p>
     *
     * @param listId 任务列表ID
     * @param userId 当前操作用户ID（需为队长）
     */
    void deleteTaskList(Long listId, Long userId);

    /**
     * 重新排序任务列表
     * <p>
     * 批量更新任务列表的排列顺序（支持看板列拖拽排序），仅队长有权执行。
     * </p>
     *
     * @param userId  当前操作用户ID（需为队长）
     * @param request 排序请求，包含各列表ID及其新位置序号
     */
    void reorderLists(Long userId, ReorderRequest request);

    /**
     * 获取任务列表所属项目ID
     * <p>
     * 通过任务列表ID查询其所属的项目ID，常用于权限校验场景。
     * </p>
     *
     * @param listId 任务列表ID
     * @return 任务列表所属的项目ID
     */
    Long getTaskListProjectId(Long listId);
}
