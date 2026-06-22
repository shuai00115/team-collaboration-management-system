package com.teamcollab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamcollab.dto.notification.NotificationResponse;
import com.teamcollab.dto.notification.UnreadCountResponse;
import com.teamcollab.entity.Notification;
import com.teamcollab.entity.TeamMember;
import com.teamcollab.mapper.NotificationMapper;
import com.teamcollab.mapper.TeamMemberMapper;
import com.teamcollab.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * <p>
 * 提供系统通知消息的查询、已读标记以及消息发送功能。
 * 支持向单个用户、团队队长或全体团队成员发送通知。
 * 通知系统用于实时推送团队协作中的各类事件，如入队申请、任务分配、
 * 审核结果等。
 * </p>
 *
 * <p><b>通知类型：</b></p>
 * <ul>
 *     <li>system —— 系统通知</li>
 *     <li>team —— 团队相关（成员变动等）</li>
 *     <li>task —— 任务相关（分配、变更等）</li>
 *     <li>application —— 申请相关（审核结果等）</li>
 * </ul>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    /** 通知 Mapper，提供未读计数和批量标记等方法 */
    private final NotificationMapper notificationMapper;

    /** 团队成员 Mapper，用于查询队长和全体成员 */
    private final TeamMemberMapper teamMemberMapper;

    // ==================== 查询通知列表 ====================

    /**
     * 获取通知列表（分页，支持按已读状态和类型过滤）
     * <p>
     * 查询指定用户收到的通知消息，按创建时间倒序排列（最新在前）。
     * 支持按 isRead（0=未读，1=已读）和 type（通知类型）进行过滤。
     * </p>
     *
     * @param userId   用户 ID
     * @param pageNum  当前页码，从 1 开始
     * @param pageSize 每页记录数
     * @param isRead   按已读状态过滤（1=已读，0=未读），可为 null 表示不过滤
     * @param type     按通知类型过滤，可为 null 或空字符串表示不过滤
     * @return 分页的通知响应列表
     */
    @Override
    public Page<NotificationResponse> getNotifications(Long userId, int pageNum, int pageSize,
                                                        Integer isRead, String type) {
        // 构建动态查询条件
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        if (type != null && !type.isBlank()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);   // 最新通知在前

        // 执行分页查询
        Page<Notification> queryPage = new Page<>(pageNum, pageSize);
        Page<Notification> resultPage = this.page(queryPage, wrapper);

        // 转换为响应 DTO
        List<NotificationResponse> records = resultPage.getRecords().stream().map(notification -> {
            NotificationResponse response = new NotificationResponse();
            response.setNotificationId(notification.getNotificationId());
            response.setType(notification.getType());
            response.setTitle(notification.getTitle());
            response.setContent(notification.getContent());
            response.setRelatedType(notification.getRelatedType());
            response.setRelatedId(notification.getRelatedId());
            response.setProjectId(notification.getProjectId());
            response.setIsRead(notification.getIsRead());
            response.setCreatedAt(notification.getCreatedAt());
            return response;
        }).collect(Collectors.toList());

        // 构建分页响应
        Page<NotificationResponse> responsePage = new Page<>(pageNum, pageSize);
        responsePage.setTotal(resultPage.getTotal());
        responsePage.setRecords(records);

        log.info("查询通知列表: userId={}, isRead={}, type={}, 总数={}",
                userId, isRead, type, resultPage.getTotal());
        return responsePage;
    }

    // ==================== 未读数量统计 ====================

    /**
     * 获取未读通知数量
     * <p>
     * 调用 notificationMapper.countUnread 统计指定用户的未读通知总数。
     * </p>
     *
     * @param userId 用户 ID
     * @return 未读通知计数响应
     */
    @Override
    public UnreadCountResponse getUnreadCount(Long userId) {
        int count = notificationMapper.countUnread(userId);
        UnreadCountResponse response = new UnreadCountResponse();
        response.setUnreadCount(count);
        log.info("查询未读通知数: userId={}, unreadCount={}", userId, count);
        return response;
    }

    // ==================== 标记已读 ====================

    /**
     * 标记单条通知为已读
     * <p>
     * 将指定通知标记为已读状态（isRead = 1）。
     * userId 参数用于权限校验，确保只有通知的接收者才能标记已读。
     * </p>
     *
     * @param notificationId 通知 ID
     * @param userId         当前操作用户 ID（需为通知接收者）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId, Long userId) {
        int affected = notificationMapper.markAsRead(notificationId, userId);
        if (affected > 0) {
            log.info("通知已标记为已读: notificationId={}, userId={}", notificationId, userId);
        } else {
            log.warn("标记已读未生效（可能通知不存在或权限不足）: notificationId={}, userId={}",
                    notificationId, userId);
        }
    }

    /**
     * 批量标记通知为已读
     * <p>
     * 将指定用户的所有通知（或指定类型的通知）标记为已读状态。
     * type 为 null 时标记全部类型的通知。
     * </p>
     *
     * @param userId 用户 ID
     * @param type   通知类型过滤，可为 null 表示标记全部类型
     * @return 包含 updatedCount（已标记数量）的键值对
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> markAllAsRead(Long userId, String type) {
        int count = notificationMapper.markAllAsRead(userId, type);
        log.info("批量标记已读: userId={}, type={}, 标记数={}", userId, type, count);

        Map<String, Integer> result = new HashMap<>();
        result.put("updatedCount", count);
        return result;
    }

    // ==================== 发送通知 ====================

    /**
     * 创建并发送单条通知
     * <p>
     * 创建一条新通知记录并保存到数据库。通知状态默认为未读（isRead=0）。
     * 如果系统启用了 WebSocket 实时推送，可在此方法中扩展推送逻辑。
     * </p>
     *
     * @param userId      接收通知的用户 ID
     * @param type        通知类型（system/team/task/application）
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedType 关联业务类型（如 team/project/task），可为 null
     * @param relatedId   关联业务 ID，可为 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, String type, String title, String content,
                                  String relatedType, Long relatedId, Long projectId) {
        // 构建通知实体
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setProjectId(projectId);
        notification.setIsRead(0);                  // 默认为未读
        notification.setCreatedAt(LocalDateTime.now());

        // 保存到数据库
        notificationMapper.insert(notification);

        log.info("通知已发送: notificationId={}, userId={}, type={}, title={}",
                notification.getNotificationId(), userId, type, title);

        // TODO: 在此处扩展 WebSocket 实时推送逻辑
        //       通过 WebSocket 将通知实时推送给在线用户
    }

    /**
     * 向团队队长发送通知
     * <p>
     * 在 team_members 表中查找指定团队的队长（role = "leader"），
     * 向队长发送一条通知。常用于申请提交等需要队长知晓的场景。
     * </p>
     *
     * @param teamId      团队 ID（用于查找队长）
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedType 关联业务类型，可为 null
     * @param relatedId   关联业务 ID，可为 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToTeamLeader(Long teamId, String type, String title, String content,
                                  String relatedType, Long relatedId, Long projectId) {
        // 查询团队队长（role = "leader"）
        List<TeamMember> leaders = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getRole, "leader"));

        if (leaders.isEmpty()) {
            log.warn("未找到团队队长，通知无法发送: teamId={}", teamId);
            return;
        }

        // 向每位队长发送通知（理论上每个团队只有一位队长）
        for (TeamMember leader : leaders) {
            sendNotification(leader.getUserId(), type, title, content, relatedType, relatedId, projectId);
        }

        log.info("已向团队队长发送通知: teamId={}, type={}, 队长数={}",
                teamId, type, leaders.size());
    }

    /**
     * 向团队全体成员发送通知
     * <p>
     * 查询指定团队的所有成员，向每位成员发送一条通知。
     * 可通过 excludeUserId 排除某个特定用户（如操作发起者本人），
     * 避免向自己发送重复通知。
     * </p>
     *
     * @param teamId        团队 ID
     * @param type          通知类型
     * @param title         通知标题
     * @param content       通知内容
     * @param relatedType   关联业务类型，可为 null
     * @param relatedId     关联业务 ID，可为 null
     * @param excludeUserId 需要排除的用户 ID（如操作发起者），可为 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToTeamMembers(Long teamId, String type, String title, String content,
                                   String relatedType, Long relatedId, Long excludeUserId, Long projectId) {
        // 查询团队所有成员
        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId));

        if (members.isEmpty()) {
            log.warn("团队无成员，通知无法发送: teamId={}", teamId);
            return;
        }

        // 向每位成员发送通知（排除指定用户）
        int sentCount = 0;
        for (TeamMember member : members) {
            // 跳过需要排除的用户
            if (excludeUserId != null && excludeUserId.equals(member.getUserId())) {
                continue;
            }
            sendNotification(member.getUserId(), type, title, content, relatedType, relatedId, projectId);
            sentCount++;
        }

        log.info("已向团队成员发送通知: teamId={}, type={}, 发送数={}, 总成员={}",
                teamId, type, sentCount, members.size());
    }
}
