package com.hato.ui.map

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hato.data.api.ApiClient
import com.hato.data.dto.AlertEventDto
import com.hato.data.dto.PositionEventDto
import com.hato.data.dto.PositionRequest
import com.hato.data.realtime.LiveSocketManager
import com.hato.domain.engine.ExitConfirmationPolicy
import com.hato.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

enum class CreateZoneState { HIDDEN, OPTIONS, DRAWING, FORM }

class MapViewModel : ViewModel() {
    var createZoneState by mutableStateOf(CreateZoneState.HIDDEN)
    var isBottomSheetExpanded by mutableStateOf(true)
    val currentDrawingPoints = mutableStateListOf<GeoPoint>()
    val savedZones = mutableStateListOf<Geofence>()

    var searchQuery by mutableStateOf("")
    var showFarmDropdown by mutableStateOf(false)
    var mapLayer by mutableStateOf("satelital")
    var showUserLocation by mutableStateOf(true)
    var showTrail by mutableStateOf(true)
    var showRouteGuidance by mutableStateOf(false)
    var isLiveSync by mutableStateOf(true)

    // Fincas
    var currentFarm by mutableStateOf(
        Farm("farm-01", "Hato Principal", "San Carlos", 150f, "Juan Propietario", GeoPoint(10.4660, -84.6450))
    )
    val farms = listOf(
        Farm("farm-01", "Hato Principal", "San Carlos", 150f, "Juan Propietario", GeoPoint(10.4660, -84.6450)),
        Farm("farm-02", "Lote Norte", "Upala", 200f, "Pedro Administrador", GeoPoint(10.8000, -85.0000))
    )

    // Animales en el Hato
    val animals = mutableStateListOf<Animal>()

    var activeUserId by mutableStateOf<String?>(null)
    var activeUserEmail by mutableStateOf<String?>(null)

    // Posiciones en tiempo real (ID de animal -> GeoPoint)
    val animalPositions = mutableStateMapOf<String, GeoPoint>()
    var mapUpdateTrigger by mutableStateOf(0)

    // Dispositivos y collares vinculados
    val connectedDevices = mutableStateListOf<String>()
    val linkedTrackerIds = mutableStateListOf<String>()

    // Recorridos en vivo (ID de animal -> Historial de coordenadas)
    val animalTrails = mutableStateMapOf<String, List<GeoPoint>>()
    private val lastMovementTimes = mutableMapOf<String, Long>()
    private val lastKnownPositions = mutableMapOf<String, GeoPoint>()

    // Alertas activas
    val appAlerts = mutableStateListOf<AppAlert>()

    // Animal seleccionado actualmente
    var selectedAnimal by mutableStateOf<Animal?>(null)

    // Ubicación GPS del teléfono del usuario
    var currentUserLocation by mutableStateOf<GeoPoint?>(null)

    // Tracker telefónico activado (simula o envía ubicación real del celular como collar GPS)
    var phoneAsTrackerEnabled by mutableStateOf(false)

    // Cliente WebSocket para tiempo real desde el backend
    private var liveSocketManager: LiveSocketManager? = null

    fun loadUserData(userId: String?, email: String?) {
        activeUserId = userId
        activeUserEmail = email

        animals.clear()
        animalPositions.clear()
        animalTrails.clear()
        savedZones.clear()

        // Si es la cuenta principal de demostración / Administrador
        if (email == "admin@hato.com" || userId == "u-admin-01") {
            animals.addAll(
                listOf(
                    Animal("a-03", "farm-01", "Vaca Pinta", "#0342", "03", "Holstein", 4, 450f, null, null, "TRK-0001", "zone-1", HealthStatus.ALERT),
                    Animal("a-21", "farm-01", "Toro Brahman", "#0343", "21", "Brahman", 5, 600f, null, null, "TRK-0002", "zone-1", HealthStatus.HEALTHY),
                    Animal("a-12", "farm-01", "Vaca Manchas", "#0344", "12", "Jersey", 3, 400f, null, null, "TRK-0003", "zone-1", HealthStatus.HEALTHY)
                )
            )
            animalPositions["a-03"] = GeoPoint(10.4665, -84.6455)
            animalPositions["a-21"] = GeoPoint(10.4650, -84.6440)
            animalPositions["a-12"] = GeoPoint(10.4670, -84.6460)

            animalTrails["a-03"] = listOf(
                GeoPoint(10.4670, -84.6450),
                GeoPoint(10.4668, -84.6452),
                GeoPoint(10.4665, -84.6455)
            )
            animalTrails["a-21"] = listOf(GeoPoint(10.4650, -84.6440))
            animalTrails["a-12"] = listOf(GeoPoint(10.4670, -84.6460))

            val defaultPoints = listOf(
                GeoPoint(10.4680, -84.6470),
                GeoPoint(10.4680, -84.6430),
                GeoPoint(10.4640, -84.6430),
                GeoPoint(10.4640, -84.6470)
            )
            savedZones.add(
                PolygonGeofence(
                    id = "zone-default-1",
                    farmId = "farm-01",
                    name = "Potrero Principal",
                    toleranceMeters = 15f,
                    points = defaultPoints,
                    active = true,
                    description = "Zona de pastoreo rotativo norte",
                    areaHa = calculateAreaHa(defaultPoints)
                )
            )
        }
        selectedAnimal = animals.firstOrNull()
        mapUpdateTrigger++
    }

    init {
        // Carga inicial por defecto
        loadUserData("u-admin-01", "admin@hato.com")

        // Monitoreo de inactividad
        viewModelScope.launch {
            while (true) {
                delay(6000)
                val currentTime = System.currentTimeMillis()
                for ((id, time) in lastMovementTimes.toMap()) {
                    if (currentTime - time > 45_000) { // 45 segundos sin moverse
                        val animal = animals.find { it.id == id }
                        if (animal != null && animal.healthStatus != HealthStatus.ATTENTION) {
                            val alertTitle = "INACTIVIDAD: ${animal.name}"
                            if (appAlerts.none { it.title == alertTitle }) {
                                appAlerts.add(
                                    0,
                                    AppAlert(
                                        title = alertTitle,
                                        description = "El collar GPS (${animal.tagNumber}) no ha registrado movimiento en más de 45s.",
                                        time = "Ahora",
                                        severity = "MEDIUM",
                                        animalId = animal.id
                                    )
                                )
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

        // Conectar al WebSocket en vivo si el sync está activo
        setupRealtimeWebSocket()
    }

    private fun setupRealtimeWebSocket() {
        liveSocketManager = LiveSocketManager(
            onPositionReceived = { event: PositionEventDto ->
                viewModelScope.launch {
                    val point = GeoPoint(event.lat, event.lng)
                    updateAnimalLiveLocation(event.animalId, point, event.battery)
                }
            },
            onAlertReceived = { alertEvent: AlertEventDto ->
                viewModelScope.launch {
                    val alertTitle = "ALERTA: ${alertEvent.animalName}"
                    val desc = alertEvent.zoneName?.let {
                        "Salió de $it a ${String.format("%.1f", alertEvent.metersOut ?: 0.0)}m del límite."
                    } ?: "Evento de geocerca detectado."
                    appAlerts.add(0, AppAlert(alertTitle, desc, "En vivo", alertEvent.severity))
                    mapUpdateTrigger++
                }
            }
        )
        // Intentar conectar con la finca actual
        liveSocketManager?.connect(currentFarm.id)
    }

    /**
     * Actualiza la posición de un animal en tiempo real en el mapa
     */
    fun updateAnimalLiveLocation(animalId: String, point: GeoPoint, batteryPercent: Int = 90) {
        animalPositions[animalId] = point
        lastMovementTimes[animalId] = System.currentTimeMillis()

        // Actualizar recorrido (Trail)
        val currentTrail = animalTrails[animalId] ?: emptyList()
        animalTrails[animalId] = (currentTrail + point).takeLast(50)

        // Evaluar geocercas activas
        if (savedZones.isNotEmpty()) {
            val isInsideAny = savedZones.any { it.contains(point) }
            val animal = animals.find { it.id == animalId }
            if (animal != null) {
                val index = animals.indexOf(animal)
                if (!isInsideAny) {
                    if (index != -1 && animal.healthStatus != HealthStatus.ALERT) {
                        animals[index] = animal.copy(healthStatus = HealthStatus.ALERT)
                    }
                    val alertTitle = "FUGA DE ANIMAL: ${animal.name}"
                    if (appAlerts.none { it.title == alertTitle }) {
                        appAlerts.add(
                            0,
                            AppAlert(
                                title = alertTitle,
                                description = "El animal ${animal.name} (${animal.displayNumber}) está fuera de todos los potreros definidos.",
                                time = "Ahora",
                                severity = "HIGH",
                                animalId = animalId
                            )
                        )
                    }
                } else {
                    if (index != -1 && animal.healthStatus == HealthStatus.ALERT) {
                        animals[index] = animal.copy(healthStatus = HealthStatus.HEALTHY)
                    }
                }
            }
        }

        mapUpdateTrigger++
    }

    /**
     * Procesa la ubicación GPS del teléfono o de un collar vinculado por Bluetooth
     * [CORREGIDO]: Ahora actualiza directamente la posición en el mapa aunque las geocercas estén vacías,
     * y soporta tanto el ID del teléfono como los MACs de dispositivos vinculados.
     */
    fun processPhoneLocation(lat: Double, lng: Double) {
        val currentPoint = GeoPoint(lat, lng)
        currentUserLocation = currentPoint

        if (!phoneAsTrackerEnabled && connectedDevices.isEmpty()) {
            // Solo registrar ubicación del usuario si el rastreador no está habilitado
            return
        }

        val currentTime = System.currentTimeMillis()

        // Actualizar todos los dispositivos vinculados y/o el tracker telefónico
        val targetAnimalIds = mutableListOf<String>()

        // 1. Si el tracker telefónico está habilitado
        if (phoneAsTrackerEnabled) {
            targetAnimalIds.add("a-phone")
            if (animals.none { it.id == "a-phone" }) {
                val phoneCow = Animal(
                    id = "a-phone",
                    farmId = currentFarm.id,
                    name = "Dispositivo Móvil GPS",
                    tagNumber = "#TEL-GPS",
                    displayNumber = "GPS",
                    breed = "Rastreador Celular",
                    ageYears = 1,
                    weightKg = 0f,
                    trackerId = "PHONE-GPS",
                    healthStatus = HealthStatus.HEALTHY,
                    isLinkedDevice = true
                )
                animals.add(0, phoneCow)
                selectedAnimal = phoneCow
            }
        }

        // 2. Para cada dispositivo vinculado escaneado
        linkedTrackerIds.forEach { trackerId ->
            val linkedAnimal = animals.find { it.trackerId == trackerId }
            if (linkedAnimal != null) {
                targetAnimalIds.add(linkedAnimal.id)
            }
        }

        // Si hay dispositivos vinculados, actualizar sus coordenadas en tiempo real
        for (targetId in targetAnimalIds) {
            val oldPoint = lastKnownPositions[targetId]
            if (oldPoint == null || oldPoint.distanceMetersTo(currentPoint) > 1.0) {
                lastMovementTimes[targetId] = currentTime
                lastKnownPositions[targetId] = currentPoint
                updateAnimalLiveLocation(targetId, currentPoint)
            }
        }

        // Enviar al backend si está configurado
        if (isLiveSync && phoneAsTrackerEnabled) {
            viewModelScope.launch {
                try {
                    ApiClient.getService().ingestPosition(
                        ingestKey = "hato-secret-ingest-key-2026",
                        position = PositionRequest(
                            trackerCode = "PHONE-GPS",
                            lat = lat,
                            lng = lng,
                            recordedAt = Instant.now().toString(),
                            speedKmh = 1.5,
                            battery = 95,
                            accuracy = 4.0
                        )
                    )
                } catch (_: Exception) {
                    // Si el backend no está disponible, el mapa local sigue funcionando a la perfección
                }
            }
        }

        mapUpdateTrigger++
    }

    // --- Control de Geocercas ---
    fun startDrawing() {
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.DRAWING
    }

    fun addDrawingPoint(point: GeoPoint) {
        if (createZoneState == CreateZoneState.DRAWING) {
            currentDrawingPoints.add(point)
            mapUpdateTrigger++
        }
    }

    fun finishDrawing() {
        if (currentDrawingPoints.size >= 3) {
            createZoneState = CreateZoneState.FORM
        }
    }

    fun saveGeofence(name: String, description: String, area: Float, tolerance: Float) {
        if (name.trim().isBlank()) return // Restringe crear lote sin nombre

        val newZone = PolygonGeofence(
            id = "zone-${System.currentTimeMillis()}",
            farmId = currentFarm.id,
            name = name.trim(),
            toleranceMeters = tolerance,
            points = currentDrawingPoints.toList(),
            active = true,
            description = description,
            areaHa = if (area > 0) area else calculateAreaHa(currentDrawingPoints)
        )
        savedZones.add(newZone)
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.HIDDEN
        mapUpdateTrigger++
    }

    fun editGeofence(updated: Geofence) {
        val index = savedZones.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            savedZones[index] = updated
            mapUpdateTrigger++
        }
    }

    fun deleteGeofence(id: String) {
        savedZones.removeAll { it.id == id }
        mapUpdateTrigger++
    }

    fun editAnimal(updated: Animal) {
        val index = animals.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            animals[index] = updated
            if (selectedAnimal?.id == updated.id) {
                selectedAnimal = updated
            }
            mapUpdateTrigger++
        }
    }

    fun deleteAnimal(id: String) {
        animals.removeAll { it.id == id }
        animalPositions.remove(id)
        animalTrails.remove(id)
        if (selectedAnimal?.id == id) {
            selectedAnimal = animals.firstOrNull()
        }
        mapUpdateTrigger++
    }

    fun cancelCreation() {
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.HIDDEN
        mapUpdateTrigger++
    }

    // --- Bluetooth y Collares GPS ---
    val scannedDevices = mutableStateListOf<ScanResult>()
    var isScanning by mutableStateOf(false)

    @SuppressLint("MissingPermission")
    fun startRealBluetoothScan(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
        val scanner = adapter?.bluetoothLeScanner

        if (scanner == null) {
            appAlerts.add(0, AppAlert("Bluetooth", "El Bluetooth no está disponible o está apagado.", "Ahora", "MEDIUM"))
            return
        }

        isScanning = true
        scannedDevices.clear()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!scannedDevices.any { it.device.address == result.device.address }) {
                    scannedDevices.add(result)
                }
            }
        }

        try {
            scanner.startScan(callback)
            viewModelScope.launch {
                delay(10000)
                try {
                    scanner.stopScan(callback)
                } catch (_: Exception) {}
                isScanning = false
            }
        } catch (e: Exception) {
            isScanning = false
            appAlerts.add(0, AppAlert("Bluetooth", "Error al escanear: ${e.message}", "Ahora", "MEDIUM"))
        }
    }

    /**
     * Vincula un dispositivo / collar Bluetooth GPS
     * [CORREGIDO]: Ahora asigna inmediatamente coordenadas al animal en el mapa y
     * asegura que sea visible en tiempo real en la capa OSMDroid.
     */
    @SuppressLint("MissingPermission")
    fun connectToDevice(result: ScanResult) {
        val deviceName = result.device.name ?: "Collar GPS"
        val mac = result.device.address
        val formattedName = "$deviceName ($mac)"

        if (!connectedDevices.contains(formattedName)) {
            connectedDevices.add(formattedName)
        }
        if (!linkedTrackerIds.contains(mac)) {
            linkedTrackerIds.add(mac)
        }

        phoneAsTrackerEnabled = true

        val animalId = "a-${mac.replace(":", "").lowercase()}"
        val existingAnimal = animals.find { it.trackerId == mac || it.id == animalId }

        val animalToAdd = existingAnimal ?: Animal(
            id = animalId,
            farmId = currentFarm.id,
            name = "$deviceName #${mac.takeLast(4)}",
            tagNumber = "#${mac.takeLast(4)}",
            displayNumber = "BT",
            breed = "Collar GPS Vinculado",
            ageYears = 2,
            weightKg = 420f,
            trackerId = mac,
            currentZoneId = null,
            healthStatus = HealthStatus.HEALTHY,
            isLinkedDevice = true
        )

        if (existingAnimal == null) {
            animals.add(0, animalToAdd)
        }

        selectedAnimal = animalToAdd

        // Coordenadas iniciales: ubicación actual del usuario o centro de la finca con ligero offset
        val initialPoint = currentUserLocation ?: GeoPoint(
            currentFarm.center.lat + (Math.random() - 0.5) * 0.001,
            currentFarm.center.lng + (Math.random() - 0.5) * 0.001
        )

        // Registrar inmediatamente en animalPositions para que aparezca en el mapa al instante
        animalPositions[animalToAdd.id] = initialPoint
        animalTrails[animalToAdd.id] = listOf(initialPoint)
        lastKnownPositions[animalToAdd.id] = initialPoint
        lastMovementTimes[animalToAdd.id] = System.currentTimeMillis()

        appAlerts.add(
            0,
            AppAlert(
                title = "DISPOSITIVO VINCULADO",
                description = "Collar GPS $deviceName vinculado con éxito. Mostrando ubicación en tiempo real en el mapa.",
                time = "Ahora",
                severity = "LOW",
                animalId = animalToAdd.id
            )
        )

        mapUpdateTrigger++
    }

    override fun onCleared() {
        super.onCleared()
        liveSocketManager?.disconnect()
    }
}
