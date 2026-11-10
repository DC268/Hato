package com.hato.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "animals")
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

    public AnimalEntity() {
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

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(String displayNumber) {
        this.displayNumber = displayNumber;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(int ageYears) {
        this.ageYears = ageYears;
    }

    public Float getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Float weightKg) {
        this.weightKg = weightKg;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getCurrentZoneId() {
        return currentZoneId;
    }

    public void setCurrentZoneId(String currentZoneId) {
        this.currentZoneId = currentZoneId;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
