package com.campuslearning.server.websocket;

import com.campuslearning.server.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端通知 WebSocket 处理器。
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = session.getUri() == null ? "" : session.getUri().getQuery();
        if (query == null || !query.startsWith("token=")) {
            closeQuietly(session);
            return;
        }

        try {
            Claims claims = jwtUtil.parse(query.substring(6));
            sessionMap.put(Long.valueOf(claims.getSubject()), session);
        } catch (Exception ex) {
            closeQuietly(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    public void sendToUser(Long userId, String json) {
        WebSocketSession session = sessionMap.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(json));
        } catch (IOException ignored) {
        }
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}
