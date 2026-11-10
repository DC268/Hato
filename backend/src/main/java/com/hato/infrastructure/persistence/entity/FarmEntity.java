package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
public class FarmEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(name = "total_area_ha")
    private float totalAreaHa = 0.0f;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lng", nullable = false)
    private double centerLng;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
