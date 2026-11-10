package com.hato.infrastructure.persistence.entity;

import com.hato.domain.model.TrackerStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "trackers")
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

    public TrackerEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public int getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }

    public void setReportIntervalSeconds(int reportIntervalSeconds) {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }

    public TrackerStatus getStatus() {
        return status;
    }

    public void setStatus(TrackerStatus status) {
        this.status = status;
    }
}
