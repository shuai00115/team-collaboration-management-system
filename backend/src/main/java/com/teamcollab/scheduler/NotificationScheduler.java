package com.teamcollab.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teamcollab.entity.Notification;
import com.teamcollab.entity.Project;
import com.teamcollab.entity.Stage;
import com.teamcollab.entity.Task;
import com.teamcollab.mapper.NotificationMapper;
import com.teamcollab.mapper.ProjectMapper;
import com.teamcollab.mapper.StageMapper;
import com.teamcollab.mapper.TaskMapper;
import com.teamcollab.mapper.TeamMemberMapper;
import com.teamcollab.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知定时任务调度器
 * <p>
 * 负责周期性执行与通知相关的后台任务，包括：
 * <ul>
 *   <li>任务截止日期提醒 —— 每小时执行</li>
 *   <li>阶段超期检测 —— 每天凌晨2点</li>
 *   <li>旧通知清理 —— 每天凌晨3点</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TaskMapper taskMapper;
    private final StageMapper stageMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final ProjectMapper projectMapper;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    /**
     * 任务截止日期提醒
     * <p>
     * 每小时执行一次（整点触发），扫描所有截止日期在 24 小时内且未完成的任务，
     * 向任务的负责人发送提醒通知。同一任务同一天只提醒一次。
     * </p>
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkTaskDueReminders() {
        log.info("开始执行任务截止日期提醒...");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusHours(24);

            // 查询所有任务
            List<Task> dueTasks = taskMapper.selectList(null);
            int count = 0;

            for (Task task : dueTasks) {
                // 筛选条件：有截止日期、在24小时内、有负责人
                if (task.getDueDate() != null
                        && task.getDueDate().isAfter(now)
                        && task.getDueDate().isBefore(tomorrow)
                        && task.getAssigneeId() != null) {

                    // 检查今天是否已经发送过提醒
                    int existingCount = notificationMapper.countByUserIdAndType(
                            task.getAssigneeId(), "due_reminder");

                    if (existingCount == 0) {
                        // 发送截止日期提醒通知
                        notificationService.sendNotification(
                                task.getAssigneeId(),
                                "due_reminder",
                                "任务即将到期",
                                "任务 \"" + task.getTitle() + "\" 的截止日期即将到达，请及时处理。",
                                "task",
                                task.getTaskId()
                        );
                        count++;
                    }
                }
            }

            log.info("任务截止日期提醒完成，共发送 {} 条通知", count);
        } catch (Exception e) {
            log.error("任务截止日期提醒执行失败", e);
        }
    }

    /**
     * 阶段超期检测
     * <p>
     * 每天凌晨 2 点执行，扫描所有结束日期已过期但状态未标记为 "completed" 的阶段，
     * 向对应团队的负责人发送超期提醒通知。
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void checkStageOverdue() {
        log.info("开始执行阶段超期检测...");
        try {
            List<Stage> allStages = stageMapper.selectList(null);
            LocalDate today = LocalDate.now();
            int count = 0;

            for (Stage stage : allStages) {
                // 筛选条件：有结束日期、已过期、未完成
                if (stage.getEndDate() != null
                        && stage.getEndDate().isBefore(today)
                        && !"completed".equals(stage.getStatus())) {

                    // 获取阶段所属团队ID
                    Long teamId = getTeamIdByStage(stage);
                    if (teamId != null) {
                        // 向团队负责人发送超期通知
                        notificationService.sendToTeamLeader(
                                teamId,
                                "stage_overdue",
                                "阶段已超期",
                                "阶段 \"" + stage.getName() + "\" 已超过截止日期，请及时处理。",
                                "stage",
                                stage.getStageId()
                        );
                        count++;
                    } else {
                        log.warn("无法获取阶段对应团队: stageId={}, projectId={}",
                                stage.getStageId(), stage.getProjectId());
                    }
                }
            }

            log.info("阶段超期检测完成，共发送 {} 条通知", count);
        } catch (Exception e) {
            log.error("阶段超期检测执行失败", e);
        }
    }

    /**
     * 旧通知清理
     * <p>
     * 每天凌晨 3 点执行，删除 90 天前已读的通知，释放数据库存储空间。
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldNotifications() {
        log.info("开始清理旧通知...");
        try {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

            // 查询 90 天前已读的通知记录
            List<Notification> notifications = notificationMapper.selectList(
                    new LambdaQueryWrapper<Notification>()
                            .eq(Notification::getIsRead, 1)
                            .lt(Notification::getCreatedAt, ninetyDaysAgo)
            );

            if (notifications != null && !notifications.isEmpty()) {
                // 批量删除
                List<Long> ids = notifications.stream()
                        .map(Notification::getNotificationId)
                        .toList();
                notificationMapper.deleteByIds(ids);
                log.info("旧通知清理完成，共删除 {} 条", ids.size());
            } else {
                log.info("无旧通知需要清理");
            }
        } catch (Exception e) {
            log.error("旧通知清理执行失败", e);
        }
    }

    /**
     * 根据阶段信息查询所属团队ID
     * <p>
     * 通过阶段 → 项目 → 团队的关联链获取。
     * </p>
     *
     * @param stage 阶段实体
     * @return 团队ID，若查询失败则返回 null
     */
    private Long getTeamIdByStage(Stage stage) {
        if (stage == null || stage.getProjectId() == null) {
            return null;
        }
        Project project = projectMapper.selectById(stage.getProjectId());
        return project != null ? project.getTeamId() : null;
    }
}
