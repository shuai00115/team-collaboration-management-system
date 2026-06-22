package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.notification.NotificationResponse;
import com.teamcollab.dto.notification.ReadAllRequest;
import com.teamcollab.dto.notification.UnreadCountResponse;
import com.teamcollab.security.CurrentUser;
import com.teamcollab.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "通知管理", description = "通知消息查询与已读标记接口")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<PageResult<NotificationResponse>> getNotifications(
            @CurrentUser Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "15") int pageSize,
            @Parameter(description = "已读状态：0=未读 1=已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "通知类型") @RequestParam(required = false) String type) {
        Page<NotificationResponse> page = notificationService.getNotifications(userId, pageNum, pageSize, isRead, type);
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "获取未读通知数")
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(@CurrentUser Long userId) {
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记单条通知已读")
    @PutMapping("/{notificationId}/read")
    public Result<Void> markAsRead(
            @CurrentUser Long userId,
            @Parameter(description = "通知ID") @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId, userId);
        return Result.success("已标记为已读");
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Map<String, Integer>> markAllAsRead(
            @CurrentUser Long userId,
            @RequestBody(required = false) ReadAllRequest request) {
        String type = request != null ? request.getType() : null;
        return Result.success("已全部标记为已读", notificationService.markAllAsRead(userId, type));
    }
}
