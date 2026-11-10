package com.hato.domain.geofence;

import com.hato.domain.model.GeoPoint;
import java.util.List;

public final class GeometryUtils {

    private GeometryUtils() {}

    /**
     * Algoritmo de Ray Casting para comprobar si un punto está dentro de un polígono.
     */
    public static boolean containsPoint(List<GeoPoint> points, GeoPoint p) {
        if (points == null || points.size() < 3) return false;
        int intersectCount = 0;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % n);
            if (rayIntersectsSegment(p, p1, p2)) {
                intersectCount++;
            }
        }
        return (intersectCount % 2 == 1);
    }

    private static boolean rayIntersectsSegment(GeoPoint p, GeoPoint a, GeoPoint b) {
        GeoPoint aPoint = a;
        GeoPoint bPoint = b;
        if (aPoint.lat() > bPoint.lat()) {
            aPoint = b;
            bPoint = a;
        }
        if (p.lat() == aPoint.lat() || p.lat() == bPoint.lat()) {
            p = new GeoPoint(p.lat() + 0.00001, p.lng());
        }
        if (p.lat() < aPoint.lat() || p.lat() > bPoint.lat() || p.lng() > Math.max(aPoint.lng(), bPoint.lng())) {
            return false;
        }
        if (p.lng() < Math.min(aPoint.lng(), bPoint.lng())) {
            return true;
        }
        double mRed = (aPoint.lng() != bPoint.lng()) ? (bPoint.lat() - aPoint.lat()) / (bPoint.lng() - aPoint.lng()) : Double.MAX_VALUE;
        double mBlue = (aPoint.lng() != p.lng()) ? (p.lat() - aPoint.lat()) / (p.lng() - aPoint.lng()) : Double.MAX_VALUE;
        return mBlue >= mRed;
    }

    /**
     * Calcula la distancia mínima desde un punto al borde del polígono en metros.
     */
    public static double distanceToBoundaryMeters(List<GeoPoint> points, GeoPoint p) {
        if (points == null || points.isEmpty()) return Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % n);
            double dist = distancePointToSegmentMeters(p, p1, p2);
            if (dist < minDistance) {
                minDistance = dist;
            }
        }
        return minDistance;
    }

    private static double distancePointToSegmentMeters(GeoPoint p, GeoPoint a, GeoPoint b) {
        double d1 = p.distanceMetersTo(a);
        double d2 = p.distanceMetersTo(b);
        return Math.min(d1, d2);
    }

    /**
     * Calcula el área aproximada en hectáreas.
     */
    public static float calculateAreaHa(List<GeoPoint> points) {
        if (points == null || points.size() < 3) return 0f;
        double area = 0.0;
        double earthRadius = 6378137.0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % n);
            double lat1 = Math.toRadians(p1.lat());
            double lat2 = Math.toRadians(p2.lat());
            double lng1 = Math.toRadians(p1.lng());
            double lng2 = Math.toRadians(p2.lng());
            area += (lng2 - lng1) * (2.0 + Math.sin(lat1) + Math.sin(lat2));
        }
        area = Math.abs(area * earthRadius * earthRadius / 2.0);
        return (float) (area / 10000.0);
    }
}
