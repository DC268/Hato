package com.hato.domain.geofence;

import com.hato.domain.model.GeoPoint;

public final class CircleGeofence extends Geofence {

    private final GeoPoint center;
    private final double radiusMeters;

    public CircleGeofence(String id, String farmId, String name, double toleranceMeters, GeoPoint center, double radiusMeters,
                          boolean active, String description) {
        super(id, farmId, name, toleranceMeters, active, description, (float) (Math.PI * radiusMeters * radiusMeters / 10000.0));
        this.center = center;
        this.radiusMeters = radiusMeters;
    }

    @Override
    public boolean contains(GeoPoint p) {
        return center.distanceMetersTo(p) <= radiusMeters;
    }

    @Override
    public double distanceToBoundaryMeters(GeoPoint p) {
        return Math.abs(center.distanceMetersTo(p) - radiusMeters);
    }

    public GeoPoint center() { return center; }
    public double radiusMeters() { return radiusMeters; }
}
