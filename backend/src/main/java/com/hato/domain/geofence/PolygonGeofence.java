package com.hato.domain.geofence;

import com.hato.domain.model.GeoPoint;
import java.util.Collections;
import java.util.List;

public final class PolygonGeofence extends Geofence {

    private final List<GeoPoint> points;

    public PolygonGeofence(String id, String farmId, String name, double toleranceMeters, List<GeoPoint> points,
                           boolean active, String description, float areaHa) {
        super(id, farmId, name, toleranceMeters, active, description,
                areaHa > 0 ? areaHa : GeometryUtils.calculateAreaHa(points));
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Un polígono debe tener al menos 3 vértices");
        }
        this.points = List.copyOf(points);
    }

    @Override
    public boolean contains(GeoPoint p) {
        return GeometryUtils.containsPoint(points, p);
    }

    @Override
    public double distanceToBoundaryMeters(GeoPoint p) {
        return GeometryUtils.distanceToBoundaryMeters(points, p);
    }

    public List<GeoPoint> points() {
        return Collections.unmodifiableList(points);
    }
}
