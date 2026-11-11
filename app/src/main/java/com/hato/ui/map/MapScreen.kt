package com.hato.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.*
import com.hato.domain.model.*
import com.hato.ui.scanner.QrScannerScreen
import com.hato.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var showQrScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val myLocationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            myLocationOverlay.enableMyLocation()
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mapView.controller.setCenter(OsmGeoPoint(location.latitude, location.longitude))
                    viewModel.processPhoneLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        mapView.controller.setZoom(16.5)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                viewModel.addDrawingPoint(GeoPoint(p.latitude, p.longitude))
                return true
            }
            override fun longPressHelper(p: OsmGeoPoint): Boolean = false
        }
        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
        mapView.overlays.add(myLocationOverlay)

        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // Efecto para el Tracking GPS continuo del usuario
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                .setMinUpdateIntervalMillis(1500L)
                .build()
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { loc ->
                        viewModel.processPhoneLocation(loc.latitude, loc.longitude)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // 1. EL MAPA (AL FONDO - OPENSTREETMAP LIBRE)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                val trigger = viewModel.mapUpdateTrigger

                // Limpiar únicamente polígonos y marcadores creados dinámicamente
                view.overlays.removeAll { it is Polygon || it is Marker }

                // Dibujar animales en el mapa (todos con emoji de cabeza de vaca 🐮)
                viewModel.animals.forEach { animal ->
                    viewModel.animalPositions[animal.id]?.let { pos ->
                        val marker = Marker(view).apply {
                            position = OsmGeoPoint(pos.lat, pos.lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "${animal.name} (${animal.displayNumber})"

                            val isLinked = animal.isLinkedDevice || viewModel.linkedTrackerIds.contains(animal.trackerId)
                            val battery = viewModel.getBatteryLevel(animal.id)
                            snippet = if (isLinked) "🐮 Collar Conectado · Batería $battery%" else "Raza: ${animal.breed} · Arete: #${animal.tagNumber}"

                            val isAlert = animal.healthStatus == HealthStatus.ALERT || animal.healthStatus == HealthStatus.ATTENTION
                            icon = createCowMarker(context, animal.displayNumber, isAlert, isLinked)

                            setOnMarkerClickListener { _, _ ->
                                viewModel.selectedAnimal = animal
                                true
                            }
                        }
                        view.overlays.add(marker)
                    }
                }

                // Dibujar rastro / recorrido (Trail)
                if (viewModel.showTrail) {
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()
                    if (animal != null) {
                        val trail = viewModel.animalTrails[animal.id]
                        if (!trail.isNullOrEmpty() && trail.size >= 2) {
                            val polyline = Polyline().apply {
                                setPoints(trail.map { OsmGeoPoint(it.lat, it.lng) })
                                outlinePaint.color = android.graphics.Color.argb(255, 255, 140, 0) // Naranja dinámico
                                outlinePaint.strokeWidth = 9f
                            }
                            view.overlays.add(polyline)
                        }
                    }
                }

                // Dibujar ruta guiada (desde ubicación del ganadero hasta el animal)
                if (viewModel.showRouteGuidance) {
                    val loc = myLocationOverlay.myLocation
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()
                    val animalPos = animal?.let { viewModel.animalPositions[it.id] }

                    if (loc != null && animalPos != null) {
                        val polyline = Polyline().apply {
                            setPoints(listOf(loc, OsmGeoPoint(animalPos.lat, animalPos.lng)))
                            outlinePaint.color = android.graphics.Color.argb(255, 16, 185, 129) // Verde esmeralda de ruta
                            outlinePaint.strokeWidth = 10f
                        }
                        view.overlays.add(polyline)
                    }
                }

                // Dibujar potreros guardados creados por el usuario
                viewModel.savedZones.forEach { zone ->
                    if (zone is PolygonGeofence) {
                        val polygon = Polygon().apply {
                            points = zone.points.map { OsmGeoPoint(it.lat, it.lng) }
                            fillPaint.color = android.graphics.Color.argb(55, 16, 185, 129) // Esmeralda suave
                            outlinePaint.color = android.graphics.Color.argb(255, 16, 185, 129)
                            outlinePaint.strokeWidth = 6f
                            title = "${zone.name} (${String.format("%.1f", zone.areaHa)} Ha)"
                        }
                        view.overlays.add(polygon)
                    }
                }

                // Dibujar potrero en proceso de trazado (ordenado de forma convexa para evitar reloj de arena)
                if (viewModel.currentDrawingPoints.isNotEmpty()) {
                    val orderedPoints = orderPointsConvex(viewModel.currentDrawingPoints)
                    val drawingPolygon = Polygon().apply {
                        points = orderedPoints.map { OsmGeoPoint(it.lat, it.lng) }
                        fillPaint.color = android.graphics.Color.argb(85, 251, 191, 36) // Amarillo amber de trazado
                        outlinePaint.color = android.graphics.Color.argb(255, 251, 191, 36)
                        outlinePaint.strokeWidth = 6f
                    }
                    view.overlays.add(drawingPolygon)

                    orderedPoints.forEach { point ->
                        val marker = Marker(view).apply {
                            position = OsmGeoPoint(point.lat, point.lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        view.overlays.add(marker)
                    }
                }

                view.invalidate()
            }
        )

        // 2. BARRA SUPERIOR: SELECTOR DE POTREROS DEL USUARIO
        // (Ajustes de la esquina superior derecha quitado según requerimiento)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showZoneDropdown by remember { mutableStateOf(false) }

                // Selector exclusivo para los potreros creados por el usuario
                Box {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = viewModel.savedZones.isNotEmpty()) { showZoneDropdown = true }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌾", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.savedZones.isEmpty()) {
                                "SIN POTREROS CREADOS"
                            } else {
                                (viewModel.selectedZone?.name ?: "TODOS LOS POTREROS").uppercase()
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (viewModel.savedZones.isNotEmpty()) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (viewModel.savedZones.isNotEmpty()) {
                        DropdownMenu(
                            expanded = showZoneDropdown,
                            onDismissRequest = { showZoneDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ver Todos los Potreros", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    viewModel.selectedZone = null
                                    showZoneDropdown = false
                                }
                            )
                            viewModel.savedZones.forEach { zone ->
                                DropdownMenuItem(
                                    text = { Text("${zone.name} (${String.format("%.1f", zone.areaHa)} ha)") },
                                    onClick = {
                                        viewModel.selectedZone = zone
                                        showZoneDropdown = false
                                        if (zone is PolygonGeofence && zone.points.isNotEmpty()) {
                                            val avgLat = zone.points.map { it.lat }.average()
                                            val avgLng = zone.points.map { it.lng }.average()
                                            mapView.controller.animateTo(OsmGeoPoint(avgLat, avgLng))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Indicador de estado satelital / dispositivos vinculados
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (viewModel.connectedDevices.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(99.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Text(
                                    "${viewModel.connectedDevices.size} VINCULADOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. BARRA FLOTANTE DE DIBUJO DE GEOCERCA
        if (viewModel.createZoneState == CreateZoneState.DRAWING) {
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)) {
                DrawingOverlay(
                    onFinish = { viewModel.finishDrawing() },
                    onCancel = { viewModel.cancelCreation() }
                )
            }
        }

        // 4. BOTÓN ÚNICO DE ACERCAMIENTO INTELIGENTE
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val currentZoom = mapView.zoomLevelDouble
                    val targetZoom = if (currentZoom >= 18.0) 14.5 else currentZoom + 1.5
                    mapView.controller.zoomTo(targetZoom)

                    val target = viewModel.selectedAnimal
                    val targetPos = target?.let { viewModel.animalPositions[it.id] }
                    if (targetPos != null) {
                        mapView.controller.animateTo(OsmGeoPoint(targetPos.lat, targetPos.lng))
                    } else if (myLocationOverlay.myLocation != null) {
                        mapView.controller.animateTo(myLocationOverlay.myLocation)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = "Zoom y Centrado")
            }
        }

        // 5. HOJA INFERIOR (BOTTOM SHEET): DESPLEGABLE Y CARRUSEL DE GANADO
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                // Manija para expandir / colapsar
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                        .clickable { viewModel.isBottomSheetExpanded = !viewModel.isBottomSheetExpanded }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.isBottomSheetExpanded) {
                    if (viewModel.animals.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🐮", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "No hay ganado registrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Añade tu primer animal en la sección 'Hato' o vincula un collar GPS en 'Ajustes'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Título y contador de cabezas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ganado Monitoreado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${viewModel.animals.size} Cabezas Registradas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // CARRUSEL HORIZONTAL SCROLLABLE DE ANIMALES
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(viewModel.animals, key = { it.id }) { animal ->
                                val isSelected = viewModel.selectedAnimal?.id == animal.id
                                val battery = viewModel.getBatteryLevel(animal.id)
                                val isAlert = animal.healthStatus == HealthStatus.ALERT

                                Surface(
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.selectedAnimal = animal
                                            viewModel.animalPositions[animal.id]?.let { pos ->
                                                mapView.controller.animateTo(OsmGeoPoint(pos.lat, pos.lng))
                                            }
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🐮", fontSize = 22.sp)

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = animal.name,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(
                                                            if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "#${animal.displayNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text("•", color = MaterialTheme.colorScheme.outline)
                                                // BATERÍA DEL DISPOSITIVO
                                                Text(
                                                    text = "⚡ $battery%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (battery < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // TARJETA DE DETALLES DEL ANIMAL SELECCIONADO
                        val selectedAnimal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()
                        if (selectedAnimal != null) {
                            val pos = viewModel.animalPositions[selectedAnimal.id]
                            val battery = viewModel.getBatteryLevel(selectedAnimal.id)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${selectedAnimal.name} (${selectedAnimal.displayNumber})",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Raza: ${selectedAnimal.breed} · Arete: #${selectedAnimal.tagNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Estado de Salud / Geocerca
                                        Surface(
                                            color = when (selectedAnimal.healthStatus) {
                                                HealthStatus.ALERT -> MaterialTheme.colorScheme.errorContainer
                                                HealthStatus.ATTENTION -> Color(0xFFFEF3C7)
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                            shape = RoundedCornerShape(99.dp)
                                        ) {
                                            Text(
                                                text = when (selectedAnimal.healthStatus) {
                                                    HealthStatus.ALERT -> "FUERA DE POTRERO"
                                                    HealthStatus.ATTENTION -> "QUIETO / INACTIVO"
                                                    else -> "DENTRO / SEGURO"
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (selectedAnimal.healthStatus) {
                                                    HealthStatus.ALERT -> MaterialTheme.colorScheme.onErrorContainer
                                                    HealthStatus.ATTENTION -> Color(0xFFB45309)
                                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val weight = selectedAnimal.weightKg ?: 450f
                                    val estCop = (weight * 9000L).toLong()

                                    // Métricas de Telemetría, Batería, Peso y Estimado Comercial Colombia
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TelemetryItem("Batería", "$battery% ⚡")
                                        TelemetryItem("Peso", "${weight.toInt()} kg")
                                        TelemetryItem("Est. Venta COP", "$${estCop / 1000000}.${(estCop % 1000000) / 100000}M")
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Botones de acción en el mapa
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.showTrail = !viewModel.showTrail },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (viewModel.showTrail) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                                contentColor = if (viewModel.showTrail) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                if (viewModel.showTrail) "Ocultar Rastro" else "Ver Recorrido",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = { viewModel.showRouteGuidance = !viewModel.showRouteGuidance },
                                            modifier = Modifier.weight(1f).height(40.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                contentColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(
                                                if (viewModel.showRouteGuidance) "Ruta Activa" else "Guiar a la Vaca",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Ganado Monitoreado: ${viewModel.animals.size} cabezas (Toque para expandir)",
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Modal para crear o guardar geocerca
        if (viewModel.createZoneState == CreateZoneState.OPTIONS ||
            viewModel.createZoneState == CreateZoneState.FORM) {
            CreateZoneBottomSheet(viewModel)
        }
    }
}

@Composable
fun TelemetryItem(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DrawingOverlay(onFinish: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp
        ) {
            Text(
                "Marca los vértices del potrero (sin cruces)",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            IconButton(onClick = onFinish) {
                Icon(Icons.Default.Check, contentDescription = "Guardar Potrero", tint = Color.White)
            }
        }
    }
}

/**
 * Crea el icono personalizado para el mapa con el emoji de la cabeza de vaca (🐮)
 */
fun createCowMarker(
    context: android.content.Context,
    label: String,
    isAlert: Boolean,
    isLinkedDevice: Boolean = false
): android.graphics.drawable.BitmapDrawable {
    val size = 130
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // Borde exterior verde brillante para dispositivos vinculados
    if (isLinkedDevice) {
        paint.color = android.graphics.Color.parseColor("#4EDEA3")
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f), paint)
    }

    // Fondo del marcador
    val radius = if (isLinkedDevice) (size / 2f) - 6f else (size / 2f)
    paint.color = when {
        isAlert -> android.graphics.Color.parseColor("#BA1A1A") // Rojo alerta
        isLinkedDevice -> android.graphics.Color.parseColor("#1565C0") // Azul enlace
        else -> android.graphics.Color.parseColor("#10B981") // Verde esmeralda normal
    }
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    // Emoji de Cabeza de Vaca constante
    paint.textSize = 50f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText("🐮", size / 2f, (size / 2f) - ((paint.descent() + paint.ascent()) / 2f) - 14f, paint)

    // Texto con el número identificador
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 26f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText(label, size / 2f, size - 18f, paint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}
