package com.hato.infrastructure.persistence.entity;

import com.hato.domain.model.TrackerStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "trackers")
@Getter
@Setter
@NoArgsConstructor
public class TrackerEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String code; // IMEI o MAC

    @Column(name = "battery_percent", nullable = false)
    private int batteryPercent = 100;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "report_interval_seconds", nullable = false)
    private int reportIntervalSeconds = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrackerStatus status = TrackerStatus.ONLINE;
}
