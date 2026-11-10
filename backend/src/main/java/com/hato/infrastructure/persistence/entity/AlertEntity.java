package com.hato.infrastructure.persistence.entity;

import com.hato.domain.model.Severity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
public class AlertEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "farm_id", nullable = false, length = 64)
    private String farmId;

    @Column(name = "animal_id", nullable = false, length = 64)
    private String animalId;

    @Column(name = "animal_name", length = 150)
    private String animalName;

    @Column(name = "zone_id", length = 64)
    private String zoneId;

    @Column(name = "zone_name", length = 150)
    private String zoneName;

    @Column(nullable = false, length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Severity severity = Severity.HIGH;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "meters_out")
    private Double metersOut;

    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column(name = "acknowledged_by", length = 64)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
