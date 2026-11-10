package com.hato.infrastructure.scheduling;

import com.hato.infrastructure.persistence.repository.SpringDataTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatteryMonitor {

    private static final Logger log = LoggerFactory.getLogger(BatteryMonitor.class);
    private final SpringDataTrackerRepository trackerRepository;

    public BatteryMonitor(SpringDataTrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public void monitorBatteries() {
        var trackers = trackerRepository.findAll();
        for (var tracker : trackers) {
            if (tracker.getBatteryPercent() <= 20) {
                log.warn("Alerta de Batería: Collar {} con {}%", tracker.getCode(), tracker.getBatteryPercent());
            }
        }
    }
}
