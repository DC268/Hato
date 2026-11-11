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
import com.hato.data.repository.SessionManager
import com.hato.domain.engine.ExitConfirmationPolicy
import com.hato.domain.model.*
import com.hato.ui.theme.ThemeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

enum class CreateZoneState { HIDDEN, OPTIONS, DRAWING, FORM }

class MapViewModel : ViewModel() {
    var createZoneState by mutableStateOf(CreateZoneState.HIDDEN)
    var isBottomSheetExpanded by mutableStateOf(true)
    val currentDrawingPoints = mutableStateListOf<GeoPoint>()
    val savedZones = mutableStateListOf<Geofence>()
    var selectedZone by mutableStateOf<Geofence?>(null)

    var searchQuery by mutableStateOf("")
    var showZoneDropdown by mutableStateOf(false)
    var mapLayer by mutableStateOf("satelital")
    var showUserLocation by mutableStateOf(true)
    var showTrail by mutableStateOf(true)
    var showRouteGuidance by mutableStateOf(false)
    var isLiveSync by mutableStateOf(true)

    // Finca activa
    var currentFarm by mutableStateOf(
        Farm("farm-01", "Hato Principal", "Zona Ganadera", 150f, "Propietario", GeoPoint(10.4660, -84.6450))
    )

    // Animales en el Hato (solo creados por el usuario o vinculados)
    val animals = mutableStateListOf<Animal>()

    var activeUserId by mutableStateOf<String?>(null)
    var activeUserEmail by mutableStateOf<String?>(null)

    // Posiciones en tiempo real (ID de animal -> GeoPoint)
    val animalPositions = mutableStateMapOf<String, GeoPoint>()
    var mapUpdateTrigger by mutableStateOf(0)

    // Dispositivos y collares vinculados (Persistentes)
    val connectedDevices = mutableStateListOf<String>()
    val linkedTrackerIds = mutableStateListOf<String>()
    val deviceBatteryLevels = mutableStateMapOf<String, Int>()

    // Recorridos en vivo (ID de animal -> Historial de coordenadas)
    val animalTrails = mutableStateMapOf<String, List<GeoPoint>>()
    private val lastMovementTimes = mutableMapOf<String, Long>()
    private val lastKnownPositions = mutableMapOf<String, GeoPoint>()

    // Alertas activas en 3 niveles (INFO, WARNING, CRITICAL)
    val appAlerts = mutableStateListOf<AppAlert>()

    // Animal seleccionado actualmente
    var selectedAnimal by mutableStateOf<Animal?>(null)

    // Ubicación GPS del teléfono del usuario (solo para centrado y ruta hacia el animal, no es un animal)
    var currentUserLocation by mutableStateOf<GeoPoint?>(null)

    // Cliente WebSocket para tiempo real desde el backend
    private var liveSocketManager: LiveSocketManager? = null
    private var sessionManager: SessionManager? = null

    fun initContext(context: Context) {
        val sm = SessionManager(context)
        sessionManager = sm

        // Restaurar modo oscuro guardado
        ThemeState.isDarkMode.value = sm.isDarkMode()

        // Restaurar dispositivos vinculados persistentes
        val savedDevs = sm.getConnectedDevices()
        if (savedDevs.isNotEmpty()) {
            connectedDevices.clear()
            connectedDevices.addAll(savedDevs)
        }

        val savedTrackers = sm.getLinkedTrackerIds()
        if (savedTrackers.isNotEmpty()) {
            linkedTrackerIds.clear()
            linkedTrackerIds.addAll(savedTrackers)
        }

        // Restaurar animales guardados por el usuario
        val savedAnimalsList = sm.getAnimals()
        if (savedAnimalsList.isNotEmpty()) {
            animals.clear()
            animals.addAll(savedAnimalsList)
            selectedAnimal = animals.firstOrNull()
        }

        // Restaurar potreros creados por el usuario
        val savedZonesList = sm.getZones()
        if (savedZonesList.isNotEmpty()) {
            savedZones.clear()
            savedZones.addAll(savedZonesList)
            selectedZone = savedZones.firstOrNull()
        }

        // Alerta inicial informativa
        if (connectedDevices.isNotEmpty()) {
            appAlerts.add(
                0,
                AppAlert(
                    title = "Dispositivos Reconectados",
                    description = "Se restauraron ${connectedDevices.size} collares GPS previamente vinculados.",
                    time = "Ahora",
                    level = AlertLevel.INFO
                )
            )
        }

        mapUpdateTrigger++
    }

    private fun persistData() {
        sessionManager?.let { sm ->
            sm.saveConnectedDevices(connectedDevices.toList())
            sm.saveLinkedTrackerIds(linkedTrackerIds.toList())
            sm.saveAnimals(animals.toList())
            sm.saveZones(savedZones.filterIsInstance<PolygonGeofence>())
        }
    }

    fun getBatteryLevel(animalId: String): Int {
        return deviceBatteryLevels[animalId] ?: 94
    }

    fun loadUserData(userId: String?, email: String?) {
        activeUserId = userId
        activeUserEmail = email

        // Si ya hay datos persistidos, se mantienen; de lo contrario inicia limpio
        sessionManager?.let { sm ->
            val savedAnimalsList = sm.getAnimals()
            if (savedAnimalsList.isNotEmpty()) {
                animals.clear()
                animals.addAll(savedAnimalsList)
            }
            val savedZonesList = sm.getZones()
            if (savedZonesList.isNotEmpty()) {
                savedZones.clear()
                savedZones.addAll(savedZonesList)
            }
            val savedDevs = sm.getConnectedDevices()
            if (savedDevs.isNotEmpty()) {
                connectedDevices.clear()
                connectedDevices.addAll(savedDevs)
            }
        }

        selectedAnimal = animals.firstOrNull()
        selectedZone = savedZones.firstOrNull()
        mapUpdateTrigger++
    }

    init {
        // Monitoreo continuo de inactividad de animales
        viewModelScope.launch {
            while (true) {
                delay(8000)
                val currentTime = System.currentTimeMillis()
                for ((id, time) in lastMovementTimes.toMap()) {
                    if (currentTime - time > 45_000) { // 45 segundos inmóvil
                        val animal = animals.find { it.id == id }
                        if (animal != null && animal.healthStatus != HealthStatus.ATTENTION) {
                            val alertTitle = "Inactividad Prolongada: ${animal.name}"
                            if (appAlerts.none { it.title == alertTitle }) {
                                appAlerts.add(
                                    0,
                                    AppAlert(
                                        title = alertTitle,
                                        description = "El animal ${animal.name} (#${animal.tagNumber}) ha estado quieto por más de 45 segundos.",
                                        time = "Ahora",
                                        level = AlertLevel.WARNING,
                                        severity = "MEDIUM",
                                        animalId = animal.id
                                    )
                                )
                            }
                            animal.healthStatus = HealthStatus.ATTENTION
                            mapUpdateTrigger++
                        }
                    }
                }
            }
        }
    }

    fun connectWebSocket() {
        if (liveSocketManager != null) return
        liveSocketManager = LiveSocketManager(
            onPositionReceived = { posEvent ->
                viewModelScope.launch { handleLivePosition(posEvent) }
            },
            onAlertReceived = { alertEvent ->
                viewModelScope.launch { handleLiveAlert(alertEvent) }
            }
        )
        liveSocketManager?.connect(currentFarm.id)
    }

    private fun handleLivePosition(dto: PositionEventDto) {
        val targetAnimal = animals.find { it.id == dto.animalId || it.trackerId == dto.animalId } ?: return
        val newPoint = GeoPoint(dto.lat, dto.lng)
        deviceBatteryLevels[targetAnimal.id] = dto.battery
        updateAnimalLiveLocation(targetAnimal.id, newPoint)
    }

    private fun handleLiveAlert(dto: AlertEventDto) {
        val level = when (dto.severity.uppercase()) {
            "CRITICAL", "HIGH" -> AlertLevel.CRITICAL
            "MEDIUM", "WARNING" -> AlertLevel.WARNING
            else -> AlertLevel.INFO
        }
        appAlerts.add(
            0,
            AppAlert(
                title = dto.event,
                description = "${dto.animalName} en ${dto.zoneName ?: "potrero"}",
                time = "Ahora",
                level = level,
                severity = dto.severity,
                animalId = null
            )
        )
    }

    /**
     * Procesa la ubicación GPS del usuario.
     * EXCLUSIÓN: El propio teléfono celular donde corre la app NO se toma como un animal.
     * Solo sirve como punto de referencia del ganadero (punto azul y guía de ruta).
     */
    fun processPhoneLocation(lat: Double, lng: Double) {
        val currentPoint = GeoPoint(lat, lng)
        currentUserLocation = currentPoint

        // Si hay collares Bluetooth GPS vinculados físicamente al teléfono en campo,
        // actualizar su telemetría
        val currentTime = System.currentTimeMillis()
        linkedTrackerIds.forEach { trackerId ->
            val linkedAnimal = animals.find { it.trackerId == trackerId }
            if (linkedAnimal != null) {
                val oldPoint = lastKnownPositions[linkedAnimal.id]
                if (oldPoint == null || oldPoint.distanceMetersTo(currentPoint) > 1.0) {
                    lastMovementTimes[linkedAnimal.id] = currentTime
                    lastKnownPositions[linkedAnimal.id] = currentPoint
                    updateAnimalLiveLocation(linkedAnimal.id, currentPoint)
                }
            }
        }
    }

    fun updateAnimalLiveLocation(animalId: String, currentPoint: GeoPoint) {
        val animal = animals.find { it.id == animalId } ?: return

        // 1. Actualizar mapa de posiciones
        animalPositions[animalId] = currentPoint

        // 2. Actualizar rastro (Trail)
        val currentTrail = animalTrails[animalId]?.toMutableList() ?: mutableListOf()
        currentTrail.add(currentPoint)
        if (currentTrail.size > 50) currentTrail.removeAt(0)
        animalTrails[animalId] = currentTrail

        // 3. Evaluar Geocercas (Polígono / Círculo)
        val assignedZone = savedZones.find { it.id == animal.currentZoneId } ?: savedZones.firstOrNull()

        if (assignedZone != null) {
            val isInside = assignedZone.contains(currentPoint)
            if (!isInside) {
                animal.healthStatus = HealthStatus.ALERT
                val alertTitle = "¡Fuera de Potrero! ${animal.name}"
                if (appAlerts.none { it.title == alertTitle }) {
                    appAlerts.add(
                        0,
                        AppAlert(
                            title = alertTitle,
                            description = "¡ALERTA URGENTE! El animal ${animal.name} (${animal.displayNumber}) ha cruzado los linderos de ${assignedZone.name}.",
                            time = "Ahora",
                            level = AlertLevel.CRITICAL,
                            severity = "HIGH",
                            animalId = animal.id
                        )
                    )
                }
            } else {
                if (animal.healthStatus == HealthStatus.ALERT) {
                    animal.healthStatus = HealthStatus.HEALTHY
                    appAlerts.add(
                        0,
                        AppAlert(
                            title = "Retorno a Potrero: ${animal.name}",
                            description = "El animal ${animal.name} reingresó a zona segura (${assignedZone.name}).",
                            time = "Ahora",
                            level = AlertLevel.INFO,
                            severity = "LOW",
                            animalId = animal.id
                        )
                    )
                }
            }
        } else {
            animal.healthStatus = HealthStatus.HEALTHY
        }

        mapUpdateTrigger++
    }

    // --- Dibujo de Geocercas sin reloj de arena (Convex Ordering) ---
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
            // Ordenar automáticamente los vértices para evitar la forma de reloj de arena
            val ordered = orderPointsConvex(currentDrawingPoints)
            currentDrawingPoints.clear()
            currentDrawingPoints.addAll(ordered)
            createZoneState = CreateZoneState.FORM
        }
    }

    fun saveGeofence(name: String, description: String, toleranceMeters: Float, areaHa: Float) {
        val orderedPoints = orderPointsConvex(currentDrawingPoints)
        val area = calculateAreaHa(orderedPoints)

        val newZone = PolygonGeofence(
            id = "zone-${System.currentTimeMillis()}",
            farmId = currentFarm.id,
            name = name.ifBlank { "Potrero ${savedZones.size + 1}" },
            toleranceMeters = toleranceMeters,
            points = orderedPoints,
            active = true,
            description = description,
            areaHa = if (areaHa > 0) areaHa else area
        )
        savedZones.add(newZone)
        selectedZone = newZone
        currentDrawingPoints.clear()
        createZoneState = CreateZoneState.HIDDEN
        persistData()

        appAlerts.add(
            0,
            AppAlert(
                title = "Potrero Creado",
                description = "Se delimitó con éxito '${newZone.name}' con ${String.format("%.1f", newZone.areaHa)} ha.",
                time = "Ahora",
                level = AlertLevel.INFO
            )
        )

        mapUpdateTrigger++
    }

    fun editGeofence(updated: Geofence) {
        val index = savedZones.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            savedZones[index] = updated
            if (selectedZone?.id == updated.id) {
                selectedZone = updated
            }
            persistData()
            mapUpdateTrigger++
        }
    }

    fun deleteGeofence(id: String) {
        savedZones.removeAll { it.id == id }
        if (selectedZone?.id == id) {
            selectedZone = savedZones.firstOrNull()
        }
        persistData()
        mapUpdateTrigger++
    }

    fun addAnimal(animal: Animal) {
        animals.add(0, animal)
        selectedAnimal = animal
        persistData()
        appAlerts.add(
            0,
            AppAlert(
                title = "Animal Registrado",
                description = "Se dio de alta a ${animal.name} (#${animal.tagNumber}) con collar ${animal.trackerId}.",
                time = "Ahora",
                level = AlertLevel.INFO
            )
        )
        mapUpdateTrigger++
    }

    fun editAnimal(updated: Animal) {
        val index = animals.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            animals[index] = updated
            if (selectedAnimal?.id == updated.id) {
                selectedAnimal = updated
            }
            persistData()
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
        persistData()
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
            appAlerts.add(
                0,
                AppAlert(
                    title = "Bluetooth Inactivo",
                    description = "Enciende el Bluetooth de tu dispositivo para escanear collares.",
                    time = "Ahora",
                    level = AlertLevel.WARNING
                )
            )
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
            appAlerts.add(
                0,
                AppAlert(
                    title = "Fallo en Escaneo",
                    description = "Error al escanear: ${e.message}",
                    time = "Ahora",
                    level = AlertLevel.WARNING
                )
            )
        }
    }

    /**
     * Vincula un dispositivo / collar Bluetooth GPS y lo guarda permanentemente
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

        val animalId = "a-${mac.replace(":", "").lowercase()}"
        val existingAnimal = animals.find { it.trackerId == mac || it.id == animalId }

        val animalToAdd = existingAnimal ?: Animal(
            id = animalId,
            farmId = currentFarm.id,
            name = "$deviceName #${mac.takeLast(4)}",
            tagNumber = mac.filter { it.isDigit() }.takeLast(4).ifEmpty { "1001" },
            displayNumber = mac.filter { it.isDigit() }.takeLast(2).ifEmpty { "01" },
            breed = "Holstein",
            ageYears = 2,
            weightKg = 420f,
            trackerId = mac,
            currentZoneId = selectedZone?.id,
            healthStatus = HealthStatus.HEALTHY,
            isLinkedDevice = true
        )

        deviceBatteryLevels[animalToAdd.id] = 96

        if (existingAnimal == null) {
            animals.add(0, animalToAdd)
        }

        selectedAnimal = animalToAdd

        // Asignar posición inicial cerca del usuario o de la finca
        val basePoint = currentUserLocation ?: currentFarm.center
        val randomOffsetLat = (Math.random() - 0.5) * 0.002
        val randomOffsetLng = (Math.random() - 0.5) * 0.002
        val initialPoint = GeoPoint(basePoint.lat + randomOffsetLat, basePoint.lng + randomOffsetLng)

        lastMovementTimes[animalToAdd.id] = System.currentTimeMillis()
        lastKnownPositions[animalToAdd.id] = initialPoint
        updateAnimalLiveLocation(animalToAdd.id, initialPoint)

        persistData()

        appAlerts.add(
            0,
            AppAlert(
                title = "Collar Vinculado",
                description = "Dispositivo '$formattedName' conectado con éxito. Transmitiendo telemetría en tiempo real.",
                time = "Ahora",
                level = AlertLevel.INFO
            )
        )
    }

    fun unlinkDevice(deviceFormatted: String) {
        connectedDevices.remove(deviceFormatted)
        val mac = deviceFormatted.substringAfterLast("(").substringBeforeLast(")")
        linkedTrackerIds.remove(mac)
        persistData()
        mapUpdateTrigger++
    }
}
