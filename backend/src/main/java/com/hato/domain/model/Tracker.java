package com.hato.domain.model;

import java.time.Instant;

public record Tracker(
        String id,
        String code,
        int batteryPercent,
        Instant lastSeenAt,
        int reportIntervalSeconds,
        TrackerStatus status
) {}
