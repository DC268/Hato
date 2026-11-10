package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class ZoneEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "farm_id", nullable = false, length = 64)
    private String farmId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String type = "POLYGON"; // POLYGON o CIRCLE

    @Column(name = "points_json", columnDefinition = "TEXT")
    private String pointsJson;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lng")
    private Double centerLng;

    @Column(name = "radius_meters")
    private Double radiusMeters;

    @Column(name = "tolerance_meters", nullable = false)
    private float toleranceMeters = 10.0f;

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "area_ha")
    private float areaHa = 0.0f;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
