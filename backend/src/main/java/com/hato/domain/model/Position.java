package com.hato.domain.model;

import java.time.Instant;

/**
 * Lectura inmutable de posición recibida desde un collar GPS.
 */
public record Position(
        String trackerId,
        GeoPoint point,
        Instant recordedAt,
        double speedKmh,
        int batteryPercent,
        double accuracyMeters
) {}
