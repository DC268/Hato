package com.hato.infrastructure.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hato.domain.model.Alert;
import com.hato.domain.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class LivePositionPublisher {

    private static final Logger log = LoggerFactory.getLogger(LivePositionPublisher.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Sesiones activas agrupadas por farmId
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> farmSessions = new ConcurrentHashMap<>();

    public void registerSession(String farmId, WebSocketSession session) {
        farmSessions.computeIfAbsent(farmId, k -> new CopyOnWriteArrayList<>()).add(session);
        log.info("Sesión WebSocket registrada para farmId: {}. Total: {}", farmId, farmSessions.get(farmId).size());
    }

    public void removeSession(String farmId, WebSocketSession session) {
        var sessions = farmSessions.get(farmId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                farmSessions.remove(farmId);
            }
        }
        log.info("Sesión WebSocket removida para farmId: {}", farmId);
    }

    public void publishPosition(String farmId, String animalId, Position p, String status) {
        var sessions = farmSessions.get(farmId);
        if (sessions == null || sessions.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "type", "position",
                "animalId", animalId,
                "lat", p.point().lat(),
                "lng", p.point().lng(),
                "status", status != null ? status : "INSIDE",
                "battery", p.batteryPercent(),
                "recordedAt", p.recordedAt().toString()
        );

        broadcast(sessions, payload);
    }

    public void publishAlert(String farmId, Alert alert) {
        var sessions = farmSessions.get(farmId);
        if (sessions == null || sessions.isEmpty()) return;

        Map<String, Object> payload = Map.of(
                "type", "alert",
                "alertId", alert.id(),
                "event", alert.type(),
                "severity", alert.severity().name(),
                "animalName", alert.animalName() != null ? alert.animalName() : "Ganado",
                "zoneName", alert.zoneName() != null ? alert.zoneName() : "",
                "metersOut", alert.metersOut() != null ? alert.metersOut() : 0.0,
                "at", alert.createdAt().toString()
        );

        broadcast(sessions, payload);
    }

    private void broadcast(CopyOnWriteArrayList<WebSocketSession> sessions, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("Error enviando mensaje WebSocket a sesión {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error serializando mensaje WebSocket", e);
        }
    }
}
