package com.hato.domain

typealias GeoPoint = com.hato.domain.model.GeoPoint
typealias Position = com.hato.domain.model.Position
typealias Tracker = com.hato.domain.model.Tracker
typealias TrackerStatus = com.hato.domain.model.TrackerStatus
typealias HealthStatus = com.hato.domain.model.HealthStatus
typealias Animal = com.hato.domain.model.Animal
typealias Farm = com.hato.domain.model.Farm
typealias Geofence = com.hato.domain.model.Geofence
typealias PolygonGeofence = com.hato.domain.model.PolygonGeofence
typealias CircleGeofence = com.hato.domain.model.CircleGeofence
typealias AppAlert = com.hato.domain.model.AppAlert

fun calculateAreaHa(points: List<com.hato.domain.model.GeoPoint>): Float {
    return com.hato.domain.model.calculateAreaHa(points)
}
