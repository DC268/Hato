package com.hato.api.dto;

import java.time.Instant;

public record AlertDto(
        String id,
        String farmId,
        String animalId,
        String animalName,
        String zoneId,
        String zoneName,
        String type,
        String severity,
        String message,
        Double metersOut,
        boolean acknowledged,
        String acknowledgedBy,
        Instant acknowledgedAt,
        Instant createdAt
) {}
