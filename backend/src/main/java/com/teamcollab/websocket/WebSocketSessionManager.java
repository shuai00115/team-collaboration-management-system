package com.teamcollab.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 * <p>
 * 维护 userId → WebSocketSession 的映射，用于实时消息推送。
 * 使用 ConcurrentHashMap 保证线程安全。
 * </p>
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** userId → WebSocketSession 映射表 */
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 添加 WebSocket 会话
     *
     * @param userId  用户ID
     * @param session WebSocket 会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        sessionMap.put(userId, session);
        log.info("WebSocket 连接建立: userId={}, sessionId={}", userId, session.getId());
    }

    /**
     * 移除 WebSocket 会话
     *
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        sessionMap.remove(userId);
        log.info("WebSocket 连接断开: userId={}", userId);
    }

    /**
     * 获取用户的 WebSocket 会话
     *
     * @param userId 用户ID
     * @return WebSocket 会话，若用户不在线则返回 null
     */
    public WebSocketSession getSession(Long userId) {
        return sessionMap.get(userId);
    }

    /**
     * 判断用户是否在线
     *
     * @param userId 用户ID
     * @return true 表示在线，false 表示离线
     */
    public boolean isOnline(Long userId) {
        return sessionMap.containsKey(userId);
    }

    /**
     * 获取当前在线用户数
     *
     * @return 在线人数
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }
}
