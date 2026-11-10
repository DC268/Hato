package com.hato.data.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

data class GoogleLoginRequest(
    val idToken: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val user: UserDto
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null
)

data class PositionRequest(
    val trackerCode: String,
    val lat: Double,
    val lng: Double,
    val recordedAt: String,
    val speedKmh: Double? = null,
    val battery: Int? = null,
    val accuracy: Double? = null
)

data class PositionEventDto(
    val type: String,
    val animalId: String,
    val lat: Double,
    val lng: Double,
    val status: String,
    val battery: Int,
    val recordedAt: String
)

data class AlertEventDto(
    val type: String,
    val alertId: String,
    val event: String,
    val severity: String,
    val animalName: String,
    val zoneName: String? = null,
    val metersOut: Double? = null,
    val at: String
)

data class FarmDto(
    val id: String,
    val name: String,
    val location: String,
    val totalAreaHa: Float,
    val ownerName: String,
    val centerLat: Double,
    val centerLng: Double
)

data class AnimalDto(
    val id: String,
    val farmId: String,
    val name: String,
    val tagNumber: String,
    val displayNumber: String,
    val breed: String,
    val ageYears: Int,
    val weightKg: Float? = null,
    val trackerId: String,
    val healthStatus: String,
    val currentZoneId: String? = null
)

data class ZoneDto(
    val id: String,
    val farmId: String,
    val name: String,
    val type: String,
    val points: List<PointDto>? = null,
    val centerLat: Double? = null,
    val centerLng: Double? = null,
    val radiusMeters: Double? = null,
    val toleranceMeters: Float = 10f,
    val active: Boolean = true,
    val description: String = "",
    val areaHa: Float = 0f
)

data class PointDto(
    val lat: Double,
    val lng: Double
)

data class AlertDto(
    val id: String,
    val animalId: String,
    val animalName: String? = null,
    val type: String,
    val severity: String,
    val message: String,
    val at: String,
    val acknowledged: Boolean = false
)

data class DeviceTokenRequest(
    val token: String,
    val platform: String = "ANDROID"
)
