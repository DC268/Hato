package com.hato.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IdleDetector {

    private static final Logger log = LoggerFactory.getLogger(IdleDetector.class);

    @Scheduled(fixedRate = 600000) // Cada 10 minutos
    public void detectProlongedInactivity() {
        log.debug("Ejecutando detección de inactividad de ganado...");
    }
}
