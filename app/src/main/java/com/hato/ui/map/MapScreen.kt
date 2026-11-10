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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.*
import com.hato.domain.model.*
import com.hato.ui.scanner.QrScannerScreen
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
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
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

    // Efecto para el Tracking GPS continuo
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

                // Limpiar únicamente polígonos y marcadores que dibujamos nosotros
                view.overlays.removeAll { it is Polygon || it is Marker }

                // Dibujar animales reales y simulados
                viewModel.animals.forEach { animal ->
                    viewModel.animalPositions[animal.id]?.let { pos ->
                        val marker = Marker(view).apply {
                            position = OsmGeoPoint(pos.lat, pos.lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "${animal.name} (${animal.displayNumber})"

                            val isLinked = animal.isLinkedDevice || animal.id.contains("bt") || animal.id.contains("phone") || animal.displayNumber == "BT"
                            if (isLinked) {
                                snippet = "📍 GPS Vinculado en Vivo · Batería 94%"
                            } else {
                                snippet = "Raza: ${animal.breed} · Arete: ${animal.tagNumber}"
                            }

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
                                outlinePaint.color = android.graphics.Color.argb(255, 255, 140, 0) // Naranja vivo
                                outlinePaint.strokeWidth = 9f
                            }
                            view.overlays.add(polyline)
                        }
                    }
                }

                // Dibujar ruta guiada (desde mi ubicación al animal seleccionado)
                if (viewModel.showRouteGuidance) {
                    val loc = myLocationOverlay.myLocation
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()
                    val animalPos = animal?.let { viewModel.animalPositions[it.id] }

                    if (loc != null && animalPos != null) {
                        val polyline = Polyline().apply {
                            setPoints(listOf(loc, OsmGeoPoint(animalPos.lat, animalPos.lng)))
                            outlinePaint.color = android.graphics.Color.argb(255, 30, 144, 255) // Azul guía
                            outlinePaint.strokeWidth = 10f
                        }
                        view.overlays.add(polyline)
                    }
                }

                // Dibujar zonas guardadas (potreros)
                viewModel.savedZones.forEach { zone ->
                    if (zone is PolygonGeofence) {
                        val polygon = Polygon().apply {
                            points = zone.points.map { OsmGeoPoint(it.lat, it.lng) }
                            fillPaint.color = android.graphics.Color.argb(55, 46, 125, 50) // Verde potrero
                            outlinePaint.color = android.graphics.Color.argb(255, 27, 94, 32)
                            outlinePaint.strokeWidth = 6f
                            title = "${zone.name} (${String.format("%.1f", zone.areaHa)} Ha)"
                        }
                        view.overlays.add(polygon)
                    }
                }

                // Dibujar zona en progreso de creación
                if (viewModel.currentDrawingPoints.isNotEmpty()) {
                    val drawingPolygon = Polygon().apply {
                        points = viewModel.currentDrawingPoints.map { OsmGeoPoint(it.lat, it.lng) }
                        fillPaint.color = android.graphics.Color.argb(85, 255, 193, 7)
                        outlinePaint.color = android.graphics.Color.argb(255, 255, 193, 7)
                        outlinePaint.strokeWidth = 5f
                    }
                    view.overlays.add(drawingPolygon)

                    viewModel.currentDrawingPoints.forEach { point ->
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

        // 2. BARRA SUPERIOR DE CONTEXTO
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
                var showFarmDropdown by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { showFarmDropdown = true }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = viewModel.currentFarm.name.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenu(
                        expanded = showFarmDropdown,
                        onDismissRequest = { showFarmDropdown = false }
                    ) {
                        viewModel.farms.forEach { farm ->
                            DropdownMenuItem(
                                text = { Text(farm.name) },
                                onClick = {
                                    viewModel.currentFarm = farm
                                    showFarmDropdown = false
                                    mapView.controller.animateTo(OsmGeoPoint(farm.center.lat, farm.center.lng))
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicador de dispositivos vinculados en vivo
                    if (viewModel.connectedDevices.isNotEmpty() || viewModel.phoneAsTrackerEnabled) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "GPS EN VIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.onSurface)
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

        // 4. BOTÓN ÚNICO DE ACERCAMIENTO Y CENTRADO INTELIGENTE
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 120.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()
                    val pos = animal?.let { viewModel.animalPositions[it.id] }
                    if (pos != null) {
                        mapView.controller.animateTo(OsmGeoPoint(pos.lat, pos.lng))
                        mapView.controller.setZoom(18.0)
                    } else {
                        val myLoc = myLocationOverlay.myLocation
                        if (myLoc != null) {
                            mapView.controller.animateTo(myLoc)
                            mapView.controller.setZoom(17.5)
                        } else {
                            mapView.controller.setZoom(mapView.zoomLevelDouble + 1.0)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Acercar y Centrar")
            }
        }

        // 5. PESTAÑA INFERIOR DE ESTADO Y TELEMETRÍA DEL GANADO
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                // Indicador para expandir/colapsar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                        .clickable { viewModel.isBottomSheetExpanded = !viewModel.isBottomSheetExpanded }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (viewModel.isBottomSheetExpanded) {
                    val selectedAnimal = viewModel.selectedAnimal ?: viewModel.animals.firstOrNull()

                    if (selectedAnimal != null) {
                        val pos = viewModel.animalPositions[selectedAnimal.id]
                        val isLinked = selectedAnimal.isLinkedDevice || selectedAnimal.displayNumber == "BT"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedAnimal.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isLinked) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "VINCULADO",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Arete: ${selectedAnimal.tagNumber} · Collar: ${selectedAnimal.trackerId}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Estado de salud / alerta
                            Surface(
                                color = when (selectedAnimal.healthStatus) {
                                    HealthStatus.ALERT -> MaterialTheme.colorScheme.errorContainer
                                    HealthStatus.ATTENTION -> Color(0xFFFFF3E0)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when (selectedAnimal.healthStatus) {
                                        HealthStatus.ALERT -> "FUERA DE ZONA"
                                        HealthStatus.ATTENTION -> "INACTIVO"
                                        else -> "DENTRO / SEGURO"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (selectedAnimal.healthStatus) {
                                        HealthStatus.ALERT -> MaterialTheme.colorScheme.onErrorContainer
                                        HealthStatus.ATTENTION -> Color(0xFFE65100)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Métricas de Telemetría
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TelemetryItem("Batería", "94% ⚡")
                            TelemetryItem("Velocidad", "1.4 km/h")
                            TelemetryItem("Coordenadas", pos?.let { "${String.format("%.4f", it.lat)}, ${String.format("%.4f", it.lng)}" } ?: "Buscando...")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Acciones de mapa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.showTrail = !viewModel.showTrail },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.showTrail) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (viewModel.showTrail) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(if (viewModel.showTrail) "Ocultar Rastro" else "Ver Recorrido", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.showRouteGuidance = !viewModel.showRouteGuidance },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(if (viewModel.showRouteGuidance) "Ruta Activa" else "Guiar a la Vaca", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(
                        "Ganado Monitoreado: ${viewModel.animals.size} cabezas (Toque para expandir)",
                        modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally),
                        fontSize = 12.sp,
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
        Text(label.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                "Toque en el mapa los vértices del potrero",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                fontSize = 12.sp,
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
 * Crea el icono personalizado para el mapa:
 * Si es un dispositivo vinculado, muestra un borde destacado y distintivo azul/verde con símbolo de GPS/collar.
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

    // Borde exterior si es un dispositivo vinculado
    if (isLinkedDevice) {
        paint.color = android.graphics.Color.parseColor("#00E676") // Verde fluorescente
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f), paint)
    }

    // Fondo del marcador
    val radius = if (isLinkedDevice) (size / 2f) - 6f else (size / 2f)
    paint.color = when {
        isAlert -> android.graphics.Color.parseColor("#BA1A1A") // Rojo alarma
        isLinkedDevice -> android.graphics.Color.parseColor("#1565C0") // Azul tracker
        else -> android.graphics.Color.parseColor("#2E6B34") // Verde normal
    }
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    // Emoji de Vaca o Tracker
    paint.textSize = 50f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    val emoji = if (isLinkedDevice) "📡" else "🐮"
    canvas.drawText(emoji, size / 2f, (size / 2f) - ((paint.descent() + paint.ascent()) / 2f) - 14f, paint)

    // Texto con el arete o ID
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 26f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText(label, size / 2f, size - 18f, paint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}
