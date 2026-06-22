package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.task.*;
import com.teamcollab.entity.Task;

import java.util.Map;

/**
 * 任务服务接口
 * <p>
 * 提供任务的创建、查询、更新、删除、移动和排序功能。
 * 任务是看板中的基本卡片单元，任意团队成员均可创建和编辑任务。
 * </p>
 *
 * @author teamcollab
 */
public interface TaskService extends IService<Task> {

    /**
     * 获取任务列表中的任务（看板列任务，分页）
     * <p>
     * 分页查询指定任务列表下的所有任务，支持多条件过滤和排序。
     * 可按优先级、阶段、负责人、关键词等条件进行过滤。
     * </p>
     *
     * @param listId     任务列表ID
     * @param pageNum    当前页码，从1开始
     * @param pageSize   每页记录数
     * @param sortBy     排序方式（如 deadline/priority/createdAt），可为null表示默认排序
     * @param priority   按优先级过滤，可为null表示不过滤
     * @param stageId    按阶段ID过滤，可为null表示不过滤
     * @param assigneeId 按负责人ID过滤，可为null表示不过滤
     * @param keyword    关键词模糊搜索（匹配任务标题），可为null表示不过滤
     * @return 分页的任务简要响应列表
     */
    Page<TaskBriefResponse> getTasksByListId(Long listId, int pageNum, int pageSize, String sortBy,
        String priority, Long stageId, Long assigneeId, String keyword);

    /**
     * 创建任务
     * <p>
     * 在指定任务列表下创建新任务，任意团队成员均可创建。
     * </p>
     *
     * @param listId 任务列表ID
     * @param userId 创建者用户ID
     * @param request 创建任务请求，包含任务标题、描述、优先级、负责人、截止日期等
     * @return 包含新创建任务基本信息的键值对（如任务ID、标题等）
     */
    Map<String, Object> createTask(Long listId, Long userId, CreateTaskRequest request);

    /**
     * 获取任务完整详情
     * <p>
     * 查询指定任务的完整信息，包含描述、评论、附件等详细内容。
     * </p>
     *
     * @param taskId 任务ID
     * @return 任务详情响应
     */
    TaskDetailResponse getTaskDetail(Long taskId);

    /**
     * 更新任务字段
     * <p>
     * 修改任务的各种字段信息，任意团队成员均可更新。
     * </p>
     *
     * @param taskId  任务ID
     * @param userId  当前操作用户ID
     * @param request 更新任务请求，包含要修改的任务字段
     */
    void updateTask(Long taskId, Long userId, UpdateTaskRequest request);

    /**
     * 删除任务
     * <p>
     * 删除指定任务。
     * </p>
     *
     * @param taskId 任务ID
     * @param userId 当前操作用户ID
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 移动任务到其他列表
     * <p>
     * 将任务移动到另一个任务列表（看板列），可同时指定在目标列表中的位置。
     * </p>
     *
     * @param taskId  任务ID
     * @param userId  当前操作用户ID
     * @param request 移动任务请求，包含目标列表ID和可选的目标位置
     */
    void moveTask(Long taskId, Long userId, MoveTaskRequest request);

    /**
     * 重新排序任务（拖拽排序）
     * <p>
     * 在同一任务列表内调整任务的排列顺序（支持看板卡片拖拽排序）。
     * </p>
     *
     * @param userId  当前操作用户ID
     * @param request 任务排序请求，包含任务ID列表及其新位置
     */
    void reorderTasks(Long userId, TaskReorderRequest request);

    /**
     * 获取任务所属项目ID
     * <p>
     * 通过任务ID，沿着任务→任务列表→项目的关联链查询其所属的项目ID。
     * 常用于权限校验场景。
     * </p>
     *
     * @param taskId 任务ID
     * @return 任务所属的项目ID
     */
    Long getTaskProjectId(Long taskId);
}
