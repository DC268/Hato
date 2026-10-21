package com.hato.ui.map

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hato.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CreateZoneState { HIDDEN, OPTIONS, DRAWING, FORM }

class MapViewModel : ViewModel() {
    var createZoneState by mutableStateOf(CreateZoneState.HIDDEN)
    var isBottomSheetExpanded by mutableStateOf(true) // Estado para colapsar/expandir pestaña
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

    // Posiciones en tiempo real de los animales en el mapa
    val animalPositions = mutableStateMapOf<String, GeoPoint>()
    var mapUpdateTrigger by mutableStateOf(0)

    val connectedDevices = mutableStateListOf<String>()
    
    // Rastreo de Movimiento (Recorridos y Alerta de Inactividad)
    val animalTrails = mutableStateMapOf<String, List<GeoPoint>>()
    private val lastMovementTimes = mutableMapOf<String, Long>()
    private val lastKnownPositions = mutableMapOf<String, GeoPoint>()

    init {
        animalPositions["a-03"] = GeoPoint(10.4665, -84.6455)
        animalPositions["a-21"] = GeoPoint(10.4650, -84.6440)
        animalPositions["a-12"] = GeoPoint(10.4670, -84.6460)
        
        animalTrails["a-03"] = listOf(GeoPoint(10.4670, -84.6450), GeoPoint(10.4668, -84.6452), GeoPoint(10.4665, -84.6455))

        // Timer para chequear inactividad
        viewModelScope.launch {
            while(true) {
                delay(5000)
                val currentTime = System.currentTimeMillis()
                for ((id, time) in lastMovementTimes) {
                    if (currentTime - time > 30_000) { // 30 segundos de inactividad
                        val animal = animals.find { it.id == id }
                        if (animal != null && animal.healthStatus != HealthStatus.ATTENTION) {
                            val alertTitle = "INACTIVIDAD: ${animal.name}"
                            val alreadyHasAlert = appAlerts.any { it.title == alertTitle }
                            if (!alreadyHasAlert) {
                                appAlerts.add(0, AppAlert(alertTitle, "El dispositivo ha estado quieto por más de 30s.", "Ahora"))
                                // Cambiar estado usando una re-asignación para que Compose lo detecte
                                val index = animals.indexOf(animal)
                                if (index != -1) {
                                    animals[index] = animal.copy(healthStatus = HealthStatus.ATTENTION)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
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

    // Funciones del Tracker Telefónico
    var phoneAsTrackerEnabled by mutableStateOf(false)
    val appAlerts = mutableStateListOf<AppAlert>()

    fun processPhoneLocation(lat: Double, lng: Double) {
        if (!phoneAsTrackerEnabled || savedZones.isEmpty()) return
        val currentPoint = GeoPoint(lat, lng)
        val currentTime = System.currentTimeMillis()
        
        animalPositions["a-bt"] = currentPoint
        
        // Lógica de Movimiento e Inactividad
        val oldPoint = lastKnownPositions["a-bt"]
        if (oldPoint == null || oldPoint.distanceMetersTo(currentPoint) > 2.0) { // Si se movió más de 2 metros
            lastMovementTimes["a-bt"] = currentTime
            lastKnownPositions["a-bt"] = currentPoint
            
            // Añadir al recorrido (Trail)
            val currentTrail = animalTrails["a-bt"] ?: emptyList()
            animalTrails["a-bt"] = currentTrail + currentPoint
            
            // Restaurar estado si estaba en ATTENTION
            val animal = animals.find { it.id == "a-bt" }
            if (animal != null && animal.healthStatus == HealthStatus.ATTENTION) {
                val index = animals.indexOf(animal)
                if (index != -1) animals[index] = animal.copy(healthStatus = HealthStatus.HEALTHY)
            }
        }
        
        mapUpdateTrigger++
        
        // Lógica de Geocerca (Fuga)
        val isInsideAny = savedZones.any { it.contains(currentPoint) }
        
        if (!isInsideAny) {
            val lastAlert = appAlerts.firstOrNull()
            if (lastAlert == null || lastAlert.title != "FUGA DEL DISPOSITIVO") {
                appAlerts.add(0, AppAlert("FUGA DEL DISPOSITIVO", "El celular rastreador salió de los linderos definidos.", "Ahora"))
            }
        }
    }

    val scannedDevices = mutableStateListOf<ScanResult>()
    var isScanning by mutableStateOf(false)

    @SuppressLint("MissingPermission")
    fun startRealBluetoothScan(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        val scanner = adapter?.bluetoothLeScanner
        
        if (scanner == null) {
            appAlerts.add(0, AppAlert("Bluetooth", "No está disponible o apagado.", "Ahora"))
            return
        }
        
        isScanning = true
        scannedDevices.clear()
        
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Solo añadir dispositivos que no estén ya en la lista
                if (!scannedDevices.any { it.device.address == result.device.address }) {
                    scannedDevices.add(result)
                }
            }
        }
        
        scanner.startScan(callback)
        
        viewModelScope.launch {
            delay(10000) // Detener tras 10 segundos
            scanner.stopScan(callback)
            isScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(result: ScanResult) {
        val deviceName = result.device.name ?: "Dispositivo"
        val mac = result.device.address
        connectedDevices.add("$deviceName ($mac)")
        phoneAsTrackerEnabled = true // Simulamos que recibimos datos de este MAC
        
        if (animals.none { it.trackerId == mac }) {
            val btCow = Animal(
                id = "a-${mac}", farmId = "farm-01", name = "Vaca Collar BT", 
                tagNumber = "#${mac.takeLast(4)}", displayNumber = "BT", breed = "Registrada", 
                ageYears = 2, weightKg = 400f, photoUrl = null, notes = null, 
                trackerId = mac, currentZoneId = null, healthStatus = HealthStatus.HEALTHY
            )
            animals.add(0, btCow)
            selectedAnimal = btCow
            lastMovementTimes[btCow.id] = System.currentTimeMillis()
        }
    }
}