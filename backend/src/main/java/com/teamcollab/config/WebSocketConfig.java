package com.teamcollab.config;

import com.teamcollab.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 * 注册通知推送 WebSocket 处理器，用于实时消息推送
 *
 * @author TeamCollab
 */
@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    /**
     * 注册 WebSocket 处理器
     * 将通知处理器映射到 /ws/notification 路径，允许跨域访问
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notification") // 注册处理器和路径
                .setAllowedOrigins("*"); // 允许所有来源的跨域 WebSocket 连接
    }
}
