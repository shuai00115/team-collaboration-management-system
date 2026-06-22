package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知 Mapper 接口
 * <p>
 * 提供通知相关的数据库操作，包括未读通知统计、单条/批量标记已读、
 * 特定类型通知计数等功能。通知系统用于实时推送团队协作中的各类事件。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 统计用户的未读通知数量
     *
     * @param userId 用户 ID
     * @return 未读通知数
     */
    int countUnread(@Param("userId") Long userId);

    /**
     * 将指定通知标记为已读
     *
     * @param notificationId 通知 ID
     * @param userId         用户 ID（用于权限校验）
     * @return 受影响的行数
     */
    int markAsRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    /**
     * 批量将用户指定类型的通知标记为已读（type 为 null 时标记全部类型）
     *
     * @param userId 用户 ID
     * @param type   通知类型（可选）
     * @return 受影响的行数
     */
    int markAllAsRead(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 统计用户指定类型的通知数量（用于去重检查）
     *
     * @param userId 用户 ID
     * @param type   通知类型
     * @return 通知数量
     */
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
