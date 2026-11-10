package com.hato.api.dto;

public record AnimalDto(
        String id,
        String farmId,
        String name,
        String tagNumber,
        String displayNumber,
        String breed,
        int ageYears,
        Float weightKg,
        String photoUrl,
        String notes,
        String trackerId,
        String currentZoneId,
        String healthStatus
) {}
