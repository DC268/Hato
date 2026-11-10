package com.hato.domain.geofence;

import com.hato.domain.model.GeoPoint;

/**
 * Clase base sellada para geocercas poligonales y circulares.
 */
public abstract sealed class Geofence permits PolygonGeofence, CircleGeofence {

    private final String id;
    private final String farmId;
    private final String name;
    private final double toleranceMeters;
    private final boolean active;
    private final String description;
    private final float areaHa;

    protected Geofence(String id, String farmId, String name, double toleranceMeters, boolean active, String description, float areaHa) {
        this.id = id;
        this.farmId = farmId;
        this.name = name;
        this.toleranceMeters = toleranceMeters;
        this.active = active;
        this.description = description;
        this.areaHa = areaHa;
    }

    public abstract boolean contains(GeoPoint p);
    public abstract double distanceToBoundaryMeters(GeoPoint p);

    /**
     * Determina si el punto está realmente fuera de la geocerca superando la tolerancia del GPS.
     */
    public boolean isOutside(GeoPoint p) {
        if (!active) return false;
        return !contains(p) && distanceToBoundaryMeters(p) > toleranceMeters;
    }

    public String id() { return id; }
    public String farmId() { return farmId; }
    public String name() { return name; }
    public double toleranceMeters() { return toleranceMeters; }
    public boolean active() { return active; }
    public String description() { return description; }
    public float areaHa() { return areaHa; }
}
