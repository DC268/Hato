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
import com.hato.domain.HealthStatus
import com.hato.domain.PolygonGeofence
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

    // Configuraciones de Ubicación
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapView = remember { MapView(context) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val myLocationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }

    // Launcher para pedir permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                  permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (locationPermissionGranted) {
            myLocationOverlay.enableMyLocation()
            // Auto-centrar en la última ubicación conocida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mapView.controller.setCenter(OsmGeoPoint(location.latitude, location.longitude))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(16.0)
        
        // Agregar el listener de clicks
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                viewModel.addDrawingPoint(com.hato.domain.GeoPoint(p.latitude, p.longitude))
                return true
            }
            override fun longPressHelper(p: OsmGeoPoint): Boolean = false
        }
        mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))
        
        // Agregar la capa de mi ubicación
        mapView.overlays.add(myLocationOverlay)
        
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // Efecto para el Tracking GPS
    LaunchedEffect(viewModel.phoneAsTrackerEnabled, locationPermissionGranted) {
        if (viewModel.phoneAsTrackerEnabled && locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E312D))) {
        
        // 1. EL MAPA (AL FONDO - OPENSTREETMAP LIBRE)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                val trigger = viewModel.mapUpdateTrigger // Leer estado para forzar recomposición
                // Solo eliminamos los polígonos y marcadores que dibujamos nosotros para no borrar la capa base
                view.overlays.removeAll { it is Polygon || it is Marker }

                // Dibujar animales reales y simulados
                viewModel.animals.forEach { animal ->
                    viewModel.animalPositions[animal.id]?.let { pos ->
                        val marker = Marker(view).apply {
                            position = OsmGeoPoint(pos.lat, pos.lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = "${animal.name} (${animal.displayNumber})"
                            if (animal.id.contains("bt") || animal.displayNumber == "BT") {
                                snippet = "📍 GPS Vinculado a este animal"
                            }
                            
                            val isAlert = animal.healthStatus == HealthStatus.ALERT || animal.healthStatus == HealthStatus.ATTENTION
                            icon = createCowMarker(context, animal.displayNumber, isAlert)
                        }
                        view.overlays.add(marker)
                    }
                }

                if (viewModel.showTrail) {
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.first()
                    val trail = viewModel.animalTrails[animal.id]
                    if (!trail.isNullOrEmpty()) {
                        val polyline = Polyline().apply {
                            setPoints(trail.map { OsmGeoPoint(it.lat, it.lng) })
                            outlinePaint.color = android.graphics.Color.argb(255, 255, 221, 181) // Naranja claro
                            outlinePaint.strokeWidth = 8f
                        }
                        view.overlays.add(polyline)
                    }
                }

                if (viewModel.showRouteGuidance) {
                    val loc = myLocationOverlay.myLocation
                    val animal = viewModel.selectedAnimal ?: viewModel.animals.first()
                    val animalPos = viewModel.animalPositions[animal.id]
                    
                    if (loc != null && animalPos != null) {
                        val polyline = Polyline().apply {
                            setPoints(listOf(loc, OsmGeoPoint(animalPos.lat, animalPos.lng)))
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 10f
                        }
                        view.overlays.add(polyline)
                    }
                }

                // Dibujar zonas guardadas
                viewModel.savedZones.forEach { zone ->
                    if (zone is PolygonGeofence) {
                        val polygon = Polygon().apply {
                            points = zone.points.map { OsmGeoPoint(it.lat, it.lng) }
                            fillPaint.color = android.graphics.Color.argb(51, 46, 107, 52)
                            outlinePaint.color = android.graphics.Color.argb(255, 18, 82, 30)
                            outlinePaint.strokeWidth = 5f
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
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de Finca
                var showFarmDropdown by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { showFarmDropdown = true }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.currentFarm.name.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
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

                // Badge Sync Live
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EN VIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Búsqueda y Escáner QR
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("Buscar animal...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showQrScanner = true },
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Escanear QR")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil del Ganadero", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // 3. CONTROLES FLOTANTES DEL MAPA (DERECHA)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = if (viewModel.isBottomSheetExpanded) 320.dp else 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    viewModel.createZoneState = CreateZoneState.OPTIONS
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = "Modificar Geocerca")
            }
            FloatingActionButton(
                onClick = { 
                    val loc = myLocationOverlay.myLocation
                    if (loc != null) {
                        mapView.controller.setZoom(20.0) // Máximo acercamiento
                        mapView.controller.animateTo(loc)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ir a GPS")
            }
            FloatingActionButton(
                onClick = onNavigateToSettings,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Ajustes")
            }
        }

        // Overlay Dibujo
        if (viewModel.createZoneState == CreateZoneState.DRAWING) {
            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 130.dp)) {
                DrawingOverlay(
                    onFinish = { viewModel.finishDrawing() },
                    onCancel = { viewModel.cancelCreation() }
                )
            }
        }

        // 4. EMERGENT TACTICAL BOTTOM SHEET (PANTALLA DE RESUMEN)
        if (viewModel.createZoneState == CreateZoneState.HIDDEN) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                // Drag handle (Cerrar / Abrir)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.isBottomSheetExpanded = !viewModel.isBottomSheetExpanded }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                if (viewModel.isBottomSheetExpanded) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // Resumen de Animales (Counters)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("RESUMEN DE ANIMALES", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Total: ${viewModel.animals.size} cabezas", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CounterCard(Modifier.weight(1f), "Dentro", "2", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                            CounterCard(Modifier.weight(1f), "Fuera", "1", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                            CounterCard(Modifier.weight(1f), "Sin Señal", "0", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Ficha del Animal Seleccionado (Vaca 03)
                        val activeAnimal = viewModel.selectedAnimal ?: viewModel.animals.first()
                        val isAlert = activeAnimal.healthStatus == HealthStatus.ALERT

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(activeAnimal.displayNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("${activeAnimal.name} • ${activeAnimal.breed}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text(
                                                if (isAlert) "A 140m del Potrero (Fuga)" else "Pastando dentro del lindero",
                                                color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp, fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            if (isAlert) "FUGA ACTIVA" else "DENTRO",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Grid de Telemetría
                                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TelemetryItem("Velocidad", "${viewModel.activePosition.speedKmh} km/h")
                                    TelemetryItem("Batería", "${viewModel.activePosition.batteryPercent}%")
                                    TelemetryItem("Último reporte", "Hace 45s")
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botones de acción rápida
                                Button(
                                    onClick = { viewModel.showRouteGuidance = !viewModel.showRouteGuidance },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary,
                                        contentColor = if (viewModel.showRouteGuidance) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (viewModel.showRouteGuidance) "RUTA ACTIVA" else "CÓMO LLEGAR (RUTA RÁPIDA)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.showTrail = !viewModel.showTrail }, 
                                        modifier = Modifier.weight(1f).height(48.dp), 
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (viewModel.showTrail) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                            contentColor = if (viewModel.showTrail) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(if (viewModel.showTrail) "OCULTAR RASTRO" else "VER RECORRIDO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { /* TODO: Mostrar dialog con ficha */ }, 
                                        modifier = Modifier.weight(1f).height(48.dp), 
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                                    ) {
                                        Text("VER FICHA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Estado minimizado
                    Text(
                        "Resumen: ${viewModel.animals.size} cabezas (Toque para expandir)",
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Geocerca Modal
        if (viewModel.createZoneState == CreateZoneState.OPTIONS || 
            viewModel.createZoneState == CreateZoneState.FORM) {
            CreateZoneBottomSheet(viewModel)
        }
    }
}

@Composable
fun CounterCard(modifier: Modifier, label: String, count: String, bgColor: Color, fgColor: Color) {
    Column(
        modifier = modifier.background(bgColor, RoundedCornerShape(12.dp)).border(1.dp, fgColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count, color = fgColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), color = fgColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TelemetryItem(label: String, value: String) {
    Column {
        Text(label.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DrawingOverlay(onFinish: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium, shadowElevation = 4.dp) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
        
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium, shadowElevation = 4.dp) {
            IconButton(onClick = onFinish) {
                Icon(Icons.Default.Check, contentDescription = "Confirmar", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

fun createCowMarker(context: android.content.Context, label: String, isAlert: Boolean): android.graphics.drawable.BitmapDrawable {
    val size = 120
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // Dibujar circulo de fondo (Rojo si es alerta, Verde si está bien)
    paint.color = if (isAlert) android.graphics.Color.parseColor("#BA1A1A") else android.graphics.Color.parseColor("#2E6B34")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // Dibujar Emoji de Vaca 🐮
    paint.textSize = 55f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText("🐮", size / 2f, (size / 2f) - ((paint.descent() + paint.ascent()) / 2f) - 15f, paint)

    // Dibujar el número del arete/BT
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 28f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    canvas.drawText(label, size / 2f, size - 15f, paint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}