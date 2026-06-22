package com.teamcollab.controller;

import com.teamcollab.common.response.Result;
import com.teamcollab.dto.tasklist.*;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.TaskListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "任务列表管理", description = "看板列 CRUD 与拖拽重排接口")
@RestController
@RequiredArgsConstructor
public class TaskListController {

    private final TaskListService taskListService;

    @Operation(summary = "获取项目下所有任务列表（看板列）")
    @GetMapping("/api/v1/projects/{projectId}/lists")
    public Result<List<TaskListResponse>> getProjectLists(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        return Result.success(taskListService.getProjectLists(projectId));
    }

    @Operation(summary = "创建自定义任务列表")
    @PostMapping("/api/v1/projects/{projectId}/lists")
    public Result<Map<String, Object>> createTaskList(
            @CurrentUser Long userId,
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskListRequest request) {
        return Result.success("任务列表创建成功", taskListService.createTaskList(projectId, userId, request));
    }

    @Operation(summary = "编辑任务列表名称")
    @PutMapping("/api/v1/task-lists/{listId}")
    public Result<Void> updateTaskList(
            @CurrentUser Long userId,
            @Parameter(description = "列表ID") @PathVariable Long listId,
            @Valid @RequestBody UpdateTaskListRequest request) {
        taskListService.updateTaskList(listId, userId, request);
        return Result.success("列表信息更新成功");
    }

    @Operation(summary = "删除任务列表")
    @DeleteMapping("/api/v1/task-lists/{listId}")
    public Result<Void> deleteTaskList(
            @CurrentUser Long userId,
            @Parameter(description = "列表ID") @PathVariable Long listId) {
        taskListService.deleteTaskList(listId, userId);
        return Result.success("列表已删除");
    }

    @Operation(summary = "拖拽重排任务列表")
    @PutMapping("/api/v1/task-lists/reorder")
    public Result<Void> reorderLists(
            @CurrentUser Long userId,
            @Valid @RequestBody ReorderRequest request) {
        taskListService.reorderLists(userId, request);
        return Result.success("列表顺序已更新");
    }
}
