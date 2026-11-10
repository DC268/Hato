package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "positions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tracker_id", "recorded_at"})
})
@Getter
@Setter
@NoArgsConstructor
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracker_id", nullable = false, length = 64)
    private String trackerId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "speed_kmh")
    private double speedKmh = 0.0;

    @Column
    private int battery = 100;

    @Column(name = "accuracy_meters")
    private double accuracyMeters = 5.0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
