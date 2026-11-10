package com.hato.domain.model;

/**
 * Coordenada geográfica inmutable.
 * Distancia en metros calculada mediante la fórmula de Haversine.
 */
public record GeoPoint(double lat, double lng) {

    public GeoPoint {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Coordenadas fuera de rango: lat=" + lat + ", lng=" + lng);
        }
    }

    public double distanceMetersTo(GeoPoint other) {
        double r = 6_371_000.0; // Radio de la Tierra en metros
        double dLat = Math.toRadians(other.lat - this.lat);
        double dLng = Math.toRadians(other.lng - this.lng);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat))
                * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }
}
