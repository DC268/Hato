package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "zones")
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

    public ZoneEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPointsJson() {
        return pointsJson;
    }

    public void setPointsJson(String pointsJson) {
        this.pointsJson = pointsJson;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(Double centerLng) {
        this.centerLng = centerLng;
    }

    public Double getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Double radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public float getToleranceMeters() {
        return toleranceMeters;
    }

    public void setToleranceMeters(float toleranceMeters) {
        this.toleranceMeters = toleranceMeters;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(float areaHa) {
        this.areaHa = areaHa;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
