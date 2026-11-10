package com.hato.application.service;

import com.hato.domain.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertDeduplicator {

    // Guarda el último estado o evento por clave compuesta (animalId + ":" + tipo)
    private final Map<String, Instant> lastAlertTimes = new ConcurrentHashMap<>();

    public boolean shouldDispatch(AlertEvent event) {
        String key = event.animalId() + ":" + event.getClass().getSimpleName();
        Instant lastTime = lastAlertTimes.get(key);
        Instant now = Instant.now();

        if (lastTime != null && now.toEpochMilli() - lastTime.toEpochMilli() < 120_000) { // 2 minutos de enfriamiento
            return false;
        }

        lastAlertTimes.put(key, now);
        return true;
    }
}
