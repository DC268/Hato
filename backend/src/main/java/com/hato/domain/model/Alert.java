package com.hato.domain.model;

import java.time.Instant;

public record Alert(
        String id,
        String farmId,
        String animalId,
        String animalName,
        String zoneId,
        String zoneName,
        String type,
        Severity severity,
        String message,
        Double metersOut,
        boolean acknowledged,
        String acknowledgedBy,
        Instant acknowledgedAt,
        Instant createdAt
) {}
