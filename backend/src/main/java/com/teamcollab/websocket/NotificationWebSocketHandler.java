package com.teamcollab.websocket;

import com.teamcollab.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;

/**
 * 通知 WebSocket 处理器
 * <p>
 * 负责 WebSocket 连接的建立、关闭、心跳维持以及消息推送。
 * 连接时通过 URI 查询参数中的 JWT token 进行身份验证。
 * </p>
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /** 自注入，用于支持 @Async 异步方法调用；@Lazy 打破 Spring 循环依赖 */
    @Autowired
    @org.springframework.context.annotation.Lazy
    private NotificationWebSocketHandler self;

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * WebSocket 连接建立后的回调
     * <p>
     * 从 URI 查询参数中提取 "token"，使用 JwtTokenProvider 验证 JWT，
     * 验证通过后将 userId 与会话关联存入 SessionManager；
     * 验证失败则关闭连接并返回 NOT_ACCEPTABLE 状态码。
     * </p>
     *
     * @param session 当前 WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) {
                log.warn("WebSocket URI 为空，关闭连接");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 从查询参数中提取 token
            String query = uri.getQuery();
            String token = extractParam(query, "token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 连接缺少 token 参数，关闭连接");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 验证 JWT 并获取 userId
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket token 验证失败，关闭连接");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("无法从 token 中提取 userId，关闭连接");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 将会话加入管理器，并将userId存入session attributes
            session.getAttributes().put("userId", userId);
            sessionManager.addSession(userId, session);
            log.info("WebSocket 连接验证通过: userId={}, sessionId={}", userId, session.getId());

        } catch (IOException e) {
            log.error("WebSocket 连接建立过程中发生 IO 异常: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("WebSocket 连接建立过程中发生异常: {}", e.getMessage(), e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                log.error("关闭异常连接时发生错误", ex);
            }
        }
    }

    /**
     * WebSocket 连接关闭后的回调
     * <p>
     * 从 SessionManager 中移除对应的会话。
     * </p>
     *
     * @param session     当前 WebSocket 会话
     * @param closeStatus 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        // 遍历 sessionMap 查找并移除该 session
        // 注意：此处需要从 sessionMap 中根据 session 反向查找 userId
        // 实际项目中可维护一个 sessionId → userId 的反向映射，这里采用遍历方式
        Long userId = findUserIdBySession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        log.info("WebSocket 连接关闭: sessionId={}, closeStatus={}", session.getId(), closeStatus);
    }

    /**
     * 处理收到的文本消息
     * <p>
     * 支持心跳检测：收到 "ping" 时回复 "pong"，
     * 收到 "pong" 时仅记录日志。
     * </p>
     *
     * @param session 当前 WebSocket 会话
     * @param message 收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload == null) {
            return;
        }

        switch (payload.trim()) {
            case "ping":
                // 回复心跳
                try {
                    session.sendMessage(new TextMessage("pong"));
                } catch (IOException e) {
                    log.error("发送心跳响应失败: sessionId={}", session.getId(), e);
                }
                break;

            case "pong":
                log.debug("收到心跳回复: sessionId={}", session.getId());
                break;

            default:
                log.debug("收到未知消息: sessionId={}, payload={}", session.getId(), payload);
                break;
        }
    }

    /**
     * 处理传输层异常
     * <p>
     * 记录错误日志并从 SessionManager 中移除异常会话。
     * </p>
     *
     * @param session   当前 WebSocket 会话
     * @param exception 异常对象
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输异常: sessionId={}, error={}", session.getId(), exception.getMessage(), exception);
        Long userId = findUserIdBySession(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.error("关闭异常会话失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 向指定用户发送通知消息（异步执行）
     *
     * @param userId  目标用户ID
     * @param message 要发送的 JSON 消息字符串
     */
    @Async
    public void sendNotification(Long userId, String message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session == null) {
            log.debug("用户不在线，跳过推送: userId={}", userId);
            return;
        }
        if (!session.isOpen()) {
            log.warn("WebSocket 会话已关闭，移除: userId={}", userId);
            sessionManager.removeSession(userId);
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
            log.debug("消息已推送给用户: userId={}", userId);
        } catch (IOException e) {
            log.error("推送消息失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 向指定用户广播通知（异步执行）
     * <p>
     * 构建标准 JSON 格式的通知消息并通过 WebSocket 推送。
     * </p>
     *
     * @param userId      目标用户ID
     * @param type        通知类型
     * @param title       通知标题
     * @param unreadCount 未读消息数量
     */
    @Async
    public void broadcastNotification(Long userId, String type, String title, Integer unreadCount) {
        // 构建 JSON 格式通知消息
        String json = buildNotificationJson(type, title, unreadCount);
        // 通过 self 代理调用，确保 @Async 生效
        self.sendNotification(userId, json);
    }

    /**
     * 构建通知 JSON 字符串
     *
     * @param type        通知类型
     * @param title       通知标题
     * @param unreadCount 未读消息数量
     * @return JSON 字符串
     */
    private String buildNotificationJson(String type, String title, Integer unreadCount) {
        return String.format(
                "{\"type\":\"notification\",\"notificationType\":\"%s\",\"title\":\"%s\",\"unreadCount\":%d}",
                escapeJson(type),
                escapeJson(title),
                unreadCount
        );
    }

    /**
     * 简单的 JSON 字符串转义
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 从查询字符串中提取指定参数的值
     *
     * @param query    查询字符串，如 "token=xxx&other=yyy"
     * @param paramKey 要提取的参数名
     * @return 参数值，若不存在则返回 null
     */
    private String extractParam(String query, String paramKey) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && paramKey.equals(kv[0].trim())) {
                return kv[1].trim();
            }
        }
        return null;
    }

    /**
     * 根据 WebSocketSession 查找对应的 userId
     * <p>
     * 通过遍历 SessionManager 中的映射表进行反向查找。
     * 若高并发场景可维护独立的 sessionId → userId 映射以提升性能。
     * </p>
     *
     * @param session WebSocket 会话
     * @return 对应的 userId，若未找到则返回 null
     */
    private Long findUserIdBySession(WebSocketSession session) {
        // 通过 SessionManager 的 sessionMap 反向查找
        // 此处通过反射或包内访问方式，简单起见直接判断 isOnline
        // 更好的做法：在 afterConnectionClosed 时通过 session 的 attributes 获取 userId
        return getUserIdFromSessionAttributes(session);
    }

    /**
     * 从 Session attributes 中获取 userId
     * <p>
     * 在 afterConnectionEstablished 时将 userId 存入 session attributes，
     * 以便在连接关闭时直接获取，避免遍历查找。
     * </p>
     *
     * @param session WebSocket 会话
     * @return userId，若不存在则返回 null
     */
    private Long getUserIdFromSessionAttributes(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}
