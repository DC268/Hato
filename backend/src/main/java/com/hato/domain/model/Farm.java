package com.hato.domain.model;

public record Farm(
        String id,
        String name,
        String location,
        float totalAreaHa,
        String ownerName,
        GeoPoint center
) {}
