package com.hato.ui.map

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.hato.domain.*

enum class CreateZoneState { HIDDEN, OPTIONS, DRAWING, FORM }

class MapViewModel : ViewModel() {
    var createZoneState by mutableStateOf(CreateZoneState.HIDDEN)
    val currentDrawingPoints = mutableStateListOf<GeoPoint>()
    val savedZones = mutableStateListOf<Geofence>()

    var searchQuery by mutableStateOf("")
    var showFarmDropdown by mutableStateOf(false)
    var mapLayer by mutableStateOf("satelital")
    var showUserLocation by mutableStateOf(false)
    var showTrail by mutableStateOf(false)
    var showRouteGuidance by mutableStateOf(false)
    var isLiveSync by mutableStateOf(true)

    // Dummy Data
    var currentFarm by mutableStateOf(Farm("farm-01", "Hato Principal", "San Carlos", 150f, "Juan", GeoPoint(10.4660, -84.6450)))
    val farms = listOf(
        Farm("farm-01", "Hato Principal", "San Carlos", 150f, "Juan", GeoPoint(10.4660, -84.6450)),
        Farm("farm-02", "Lote Norte", "Upala", 200f, "Pedro", GeoPoint(10.8000, -85.0000))
    )
    
    val animals = mutableStateListOf(
        Animal("a-03", "farm-01", "Pinta", "#0342", "03", "Holstein", 4, 450f, null, null, "t-1", "zone-1", HealthStatus.ALERT),
        Animal("a-21", "farm-01", "Toro", "#0343", "21", "Brahman", 5, 600f, null, null, "t-2", "zone-1", HealthStatus.ATTENTION),
        Animal("a-12", "farm-01", "Manchas", "#0344", "12", "Jersey", 3, 400f, null, null, "t-3", "zone-1", HealthStatus.HEALTHY)
    )
    
    var selectedAnimal by mutableStateOf<Animal?>(animals[0])
    
    val activePosition = Position("t-1", GeoPoint(10.466, -84.645), "now", "now", 2.5f, 78, 5f)

    fun startDrawing() {
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.DRAWING
    }

    fun addDrawingPoint(point: GeoPoint) {
        if (createZoneState == CreateZoneState.DRAWING) {
            currentDrawingPoints.add(point)
        }
    }

    fun finishDrawing() {
        createZoneState = CreateZoneState.FORM
    }

    fun saveGeofence(name: String, description: String, area: Float, tolerance: Float) {
        val newZone = PolygonGeofence(
            id = "zone-${System.currentTimeMillis()}",
            farmId = "farm-01",
            name = name,
            toleranceMeters = tolerance,
            points = currentDrawingPoints.toList(),
            active = true,
            description = description,
            areaHa = area
        )
        savedZones.add(newZone)
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.HIDDEN
    }
    
    fun cancelCreation() {
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.HIDDEN
    }
}