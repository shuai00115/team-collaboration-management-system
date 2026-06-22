package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.task.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "任务管理", description = "看板任务 CRUD、移动与拖拽重排接口")
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "获取任务列表下的任务")
    @GetMapping("/api/v1/task-lists/{listId}/tasks")
    public Result<PageResult<TaskBriefResponse>> getTasks(
            @Parameter(description = "列表ID") @PathVariable Long listId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "排序方式") @RequestParam(required = false) String sortBy,
            @Parameter(description = "优先级筛选") @RequestParam(required = false) String priority,
            @Parameter(description = "阶段筛选") @RequestParam(required = false) Long stageId,
            @Parameter(description = "执行人筛选") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "标题模糊搜索") @RequestParam(required = false) String keyword) {
        Page<TaskBriefResponse> page = taskService.getTasksByListId(listId, pageNum, pageSize, sortBy, priority, stageId, assigneeId, keyword);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "创建任务")
    @PostMapping("/api/v1/task-lists/{listId}/tasks")
    public Result<Map<String, Object>> createTask(
            @CurrentUser Long userId,
            @Parameter(description = "列表ID") @PathVariable Long listId,
            @Valid @RequestBody CreateTaskRequest request) {
        return Result.success("任务创建成功", taskService.createTask(listId, userId, request));
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/api/v1/tasks/{taskId}")
    public Result<TaskDetailResponse> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return Result.success(taskService.getTaskDetail(taskId));
    }

    @Operation(summary = "编辑任务")
    @PutMapping("/api/v1/tasks/{taskId}")
    public Result<Void> updateTask(
            @CurrentUser Long userId,
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        taskService.updateTask(taskId, userId, request);
        return Result.success("任务更新成功");
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/api/v1/tasks/{taskId}")
    public Result<Void> deleteTask(
            @CurrentUser Long userId,
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        taskService.deleteTask(taskId, userId);
        return Result.success("任务已删除");
    }

    @Operation(summary = "移动任务到其他列表")
    @PutMapping("/api/v1/tasks/{taskId}/move")
    public Result<Void> moveTask(
            @CurrentUser Long userId,
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskRequest request) {
        taskService.moveTask(taskId, userId, request);
        return Result.success("任务已移动");
    }

    @Operation(summary = "看板内任务拖拽重排")
    @PutMapping("/api/v1/tasks/reorder")
    public Result<Void> reorderTasks(
            @CurrentUser Long userId,
            @Valid @RequestBody TaskReorderRequest request) {
        taskService.reorderTasks(userId, request);
        return Result.success("任务顺序已更新");
    }
}
