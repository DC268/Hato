package com.hato.infrastructure.scheduling;

import com.hato.infrastructure.persistence.repository.SpringDataTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class SignalWatchdog {

    private static final Logger log = LoggerFactory.getLogger(SignalWatchdog.class);
    private final SpringDataTrackerRepository trackerRepository;

    public SignalWatchdog(SpringDataTrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    @Scheduled(fixedRate = 60000) // Cada minuto
    public void checkSilentTrackers() {
        Instant now = Instant.now();
        var trackers = trackerRepository.findAll();

        for (var tracker : trackers) {
            if (tracker.getLastSeenAt() != null) {
                Duration silent = Duration.between(tracker.getLastSeenAt(), now);
                long thresholdSeconds = tracker.getReportIntervalSeconds() * 3L;
                if (silent.toSeconds() > thresholdSeconds) {
                    log.debug("Tracker {} sin reporte por {}s (umbral: {}s)",
                            tracker.getCode(), silent.toSeconds(), thresholdSeconds);
                }
            }
        }
    }
}
