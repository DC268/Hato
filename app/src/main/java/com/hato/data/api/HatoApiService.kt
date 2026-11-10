package com.hato.data.api

import com.hato.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface HatoApiService {

    // --- Autenticación ---
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    // --- Fincas ---
    @GET("api/farms")
    suspend fun getFarms(): Response<List<FarmDto>>

    @GET("api/farms/{id}/animals")
    suspend fun getFarmAnimals(@Path("id") farmId: String): Response<List<AnimalDto>>

    // --- Animales ---
    @POST("api/animals")
    suspend fun createAnimal(@Body animal: AnimalDto): Response<AnimalDto>

    @PUT("api/animals/{id}")
    suspend fun updateAnimal(@Path("id") id: String, @Body animal: AnimalDto): Response<AnimalDto>

    // --- Zonas / Geocercas ---
    @GET("api/zones")
    suspend fun getZones(@Query("farmId") farmId: String? = null): Response<List<ZoneDto>>

    @POST("api/zones")
    suspend fun createZone(@Body zone: ZoneDto): Response<ZoneDto>

    @DELETE("api/zones/{id}")
    suspend fun deleteZone(@Path("id") id: String): Response<Unit>

    // --- Alertas ---
    @GET("api/alerts")
    suspend fun getAlerts(@Query("status") status: String? = null): Response<List<AlertDto>>

    @POST("api/alerts/{id}/ack")
    suspend fun acknowledgeAlert(@Path("id") id: String): Response<Unit>

    // --- Ingesta de Posiciones ---
    @POST("api/ingest/positions")
    suspend fun ingestPosition(
        @Header("X-Ingest-Key") ingestKey: String,
        @Body position: PositionRequest
    ): Response<Unit>

    // --- Tokens de Dispositivo ---
    @POST("api/devices/token")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>
}
