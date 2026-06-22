package com.teamcollab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务调度配置类
 * 配置线程池任务调度器，为 @Scheduled 注解提供运行环境
 *
 * @author TeamCollab
 */
@Configuration
@EnableScheduling // 启用定时任务支持
public class ScheduledConfig {

    /**
     * 创建任务调度器 Bean
     * 使用线程池实现，核心线程数为 5，保证定时任务有足够的并发处理能力
     *
     * @return TaskScheduler 实例
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 线程池大小：最多5个定时任务并发执行
        scheduler.setThreadNamePrefix("scheduled-task-"); // 线程名前缀，便于日志追踪
        scheduler.setAwaitTerminationSeconds(60); // 关闭时等待任务完成的最大时间
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        return scheduler;
    }
}
