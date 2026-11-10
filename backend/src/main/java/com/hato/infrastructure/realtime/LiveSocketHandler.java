package com.hato.infrastructure.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

@Component
public class LiveSocketHandler extends TextWebSocketHandler {

    private final LivePositionPublisher publisher;

    public LiveSocketHandler(LivePositionPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String farmId = extractFarmId(session.getUri());
        session.getAttributes().put("farmId", farmId);
        publisher.registerSession(farmId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String farmId = (String) session.getAttributes().getOrDefault("farmId", "default");
        publisher.removeSession(farmId, session);
    }

    private String extractFarmId(URI uri) {
        if (uri == null || uri.getQuery() == null) return "farm-01";
        for (String param : uri.getQuery().split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "farmId".equalsIgnoreCase(pair[0])) {
                return pair[1];
            }
        }
        return "farm-01";
    }
}
