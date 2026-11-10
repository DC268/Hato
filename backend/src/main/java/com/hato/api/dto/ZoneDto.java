package com.hato.api.dto;

import java.util.List;

public record ZoneDto(
        String id,
        String farmId,
        String name,
        String type,
        List<PointDto> points,
        Double centerLat,
        Double centerLng,
        Double radiusMeters,
        float toleranceMeters,
        boolean active,
        String description,
        float areaHa
) {
    public record PointDto(double lat, double lng) {}
}
