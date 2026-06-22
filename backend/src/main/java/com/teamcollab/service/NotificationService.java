package com.teamcollab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teamcollab.dto.notification.NotificationResponse;
import com.teamcollab.dto.notification.UnreadCountResponse;
import com.teamcollab.entity.Notification;

import java.util.Map;

/**
 * 通知服务接口
 * <p>
 * 提供系统通知的查询、已读标记以及消息发送功能。
 * 支持向单个用户、团队队长或全体团队成员发送通知。
 * </p>
 *
 * @author teamcollab
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 获取通知列表（分页）
     * <p>
     * 查询指定用户收到的通知消息，支持按已读状态和通知类型过滤。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  当前页码，从1开始
     * @param pageSize 每页记录数
     * @param isRead   按已读状态过滤（1已读/0未读），可为null表示不过滤
     * @param type     按通知类型过滤，可为null表示不过滤
     * @return 分页的通知响应列表
     */
    Page<NotificationResponse> getNotifications(Long userId, int pageNum, int pageSize, Integer isRead, String type);

    /**
     * 获取未读通知数量
     * <p>
     * 查询指定用户的未读通知总数。
     * </p>
     *
     * @param userId 用户ID
     * @return 未读通知计数响应，包含未读总数和可能的分类计数
     */
    UnreadCountResponse getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     * <p>
     * 将指定通知标记为已读状态，仅通知的接收者有权操作。
     * </p>
     *
     * @param notificationId 通知ID
     * @param userId         当前操作用户ID（需为通知接收者）
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记通知为已读
     * <p>
     * 将指定用户的所有通知（或指定类型的通知）标记为已读状态。
     * </p>
     *
     * @param userId 用户ID
     * @param type   通知类型过滤，可为null表示标记全部类型
     * @return 包含已标记数量的键值对（如 {"count": 10}）
     */
    Map<String, Integer> markAllAsRead(Long userId, String type);

    /**
     * 创建并发送通知
     * <p>
     * 创建一条新通知并发送给指定用户。
     * </p>
     *
     * @param userId      接收通知的用户ID
     * @param type        通知类型（如 system/team/task/application）
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedType 关联业务类型（如 team/project/task），可为null
     * @param relatedId   关联业务ID，可为null
     */
    void sendNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId);

    /**
     * 向团队队长发送通知
     * <p>
     * 创建通知并发送给指定团队的队长。
     * 常用于申请提交等需要队长知晓的场景。
     * </p>
     *
     * @param teamId      团队ID（用于查找队长）
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedType 关联业务类型，可为null
     * @param relatedId   关联业务ID，可为null
     */
    void sendToTeamLeader(Long teamId, String type, String title, String content, String relatedType, Long relatedId);

    /**
     * 向团队全体成员发送通知
     * <p>
     * 创建通知并发送给指定团队的所有成员，可排除某个特定用户。
     * 常用于团队公告、任务分配等需要全员知晓的场景。
     * </p>
     *
     * @param teamId        团队ID
     * @param type          通知类型
     * @param title         通知标题
     * @param content       通知内容
     * @param relatedType   关联业务类型，可为null
     * @param relatedId     关联业务ID，可为null
     * @param excludeUserId 需要排除的用户ID（如操作发起者本人），可为null表示不排除
     */
    void sendToTeamMembers(Long teamId, String type, String title, String content, String relatedType, Long relatedId, Long excludeUserId);
}
