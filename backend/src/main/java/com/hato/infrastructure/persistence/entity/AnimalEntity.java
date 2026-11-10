package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "animals")
@Getter
@Setter
@NoArgsConstructor
public class AnimalEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "farm_id", nullable = false, length = 64)
    private String farmId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "tag_number", nullable = false, length = 50)
    private String tagNumber;

    @Column(name = "display_number", nullable = false, length = 20)
    private String displayNumber;

    @Column(nullable = false, length = 100)
    private String breed;

    @Column(name = "age_years", nullable = false)
    private int ageYears = 1;

    @Column(name = "weight_kg")
    private Float weightKg;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "tracker_id", length = 64)
    private String trackerId;

    @Column(name = "current_zone_id", length = 64)
    private String currentZoneId;

    @Column(name = "health_status", nullable = false, length = 30)
    private String healthStatus = "HEALTHY";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
