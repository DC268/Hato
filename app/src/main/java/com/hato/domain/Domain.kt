package com.hato.domain

import kotlin.math.*

data class GeoPoint(val lat: Double, val lng: Double) {
    init {
        require(lat in -90.0..90.0 && lng in -180.0..180.0) {
            "Coordenadas fuera de rango: lat=$lat, lng=$lng"
        }
    }

    fun distanceMetersTo(other: GeoPoint): Double {
        val r = 6371000.0 // Radio de la Tierra en metros
        val dLat = Math.toRadians(other.lat - this.lat)
        val dLng = Math.toRadians(other.lng - this.lng)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(this.lat)) * cos(Math.toRadians(other.lat)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

enum class TrackerStatus { ONLINE, OFFLINE, LOW_BATTERY }
enum class HealthStatus { HEALTHY, ALERT, ATTENTION, RESTING }

data class Position(
    val trackerId: String,
    val point: GeoPoint,
    val recordedAt: String,
    val receivedAt: String,
    val speedKmh: Float,
    val batteryPercent: Int,
    val accuracyMeters: Float
)

data class Tracker(
    val id: String,
    val code: String,
    val batteryPercent: Int,
    val lastSeenAt: String,
    val reportIntervalSeconds: Int,
    val status: TrackerStatus
)

data class Animal(
    val id: String,
    val farmId: String,
    val name: String,
    val tagNumber: String,
    val displayNumber: String,
    val breed: String,
    val ageYears: Int,
    val weightKg: Float? = null,
    val photoUrl: String? = null,
    val notes: String? = null,
    val trackerId: String,
    val currentZoneId: String? = null,
    var healthStatus: HealthStatus
)

data class Farm(
    val id: String,
    val name: String,
    val location: String,
    val totalAreaHa: Float,
    val ownerName: String,
    val center: GeoPoint
)

abstract class Geofence(
    open val id: String,
    open val farmId: String,
    open val name: String,
    open val toleranceMeters: Float = 10f,
    open var active: Boolean = true,
    open var description: String = "",
    open var areaHa: Float = 0f,
    open var perimeterMeters: Float = 0f
) {
    abstract fun contains(point: GeoPoint): Boolean
    
    fun isOutside(point: GeoPoint): Boolean {
        if (!active) return false
        return !contains(point)
    }
}

class PolygonGeofence(
    id: String,
    farmId: String,
    name: String,
    toleranceMeters: Float,
    val points: List<GeoPoint>,
    active: Boolean = true,
    description: String = "",
    areaHa: Float = 0f,
    perimeterMeters: Float = 0f
) : Geofence(id, farmId, name, toleranceMeters, active, description, areaHa, perimeterMeters) {
    
    override fun contains(point: GeoPoint): Boolean {
        var intersectCount = 0
        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            if (rayIntersectsSegment(point, p1, p2)) {
                intersectCount++
            }
        }
        return (intersectCount % 2 == 1)
    }

    private fun rayIntersectsSegment(p: GeoPoint, a: GeoPoint, b: GeoPoint): Boolean {
        var aPoint = a
        var bPoint = b
        if (aPoint.lat > bPoint.lat) {
            aPoint = b
            bPoint = a
        }
        if (p.lat == aPoint.lat || p.lat == bPoint.lat) {
            val adjustedLat = p.lat + 0.00001
            return rayIntersectsSegment(GeoPoint(adjustedLat, p.lng), a, b)
        }
        if (p.lat < aPoint.lat || p.lat > bPoint.lat || p.lng > max(aPoint.lng, bPoint.lng)) {
            return false
        }
        if (p.lng < min(aPoint.lng, bPoint.lng)) {
            return true
        }
        val mRed = if (aPoint.lng != bPoint.lng) (bPoint.lat - aPoint.lat) / (bPoint.lng - aPoint.lng) else Double.MAX_VALUE
        val mBlue = if (aPoint.lng != p.lng) (p.lat - aPoint.lat) / (p.lng - aPoint.lng) else Double.MAX_VALUE
        return mBlue >= mRed
    }
}

// Clase para manejar Alertas del sistema
data class AppAlert(
    val title: String,
    val description: String,
    val time: String
)

// Función para calcular el área aproximada de un polígono en hectáreas
fun calculateAreaHa(points: List<GeoPoint>): Float {
    if (points.size < 3) return 0f
    var area = 0.0
    val earthRadius = 6378137.0
    for (i in points.indices) {
        val p1 = points[i]
        val p2 = points[(i + 1) % points.size]
        val lat1 = Math.toRadians(p1.lat)
        val lat2 = Math.toRadians(p2.lat)
        val lng1 = Math.toRadians(p1.lng)
        val lng2 = Math.toRadians(p2.lng)
        area += (lng2 - lng1) * (2.0 + sin(lat1) + sin(lat2))
    }
    area = abs(area * earthRadius * earthRadius / 2.0)
    return (area / 10000.0).toFloat() // Convertir m^2 a Hectáreas
}