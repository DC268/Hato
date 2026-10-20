package com.hato.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel) {
    var showQrScanner by remember { mutableStateOf(false) }

    if (showQrScanner) {
        QrScannerScreen(
            animals = viewModel.animals,
            onClose = { showQrScanner = false },
            onAnimalScanned = { animal ->
                viewModel.selectedAnimal = animal
                showQrScanner = false
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E312D))) {
        
        // 1. EL MAPA (AL FONDO - OPENSTREETMAP LIBRE)
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            Configuration.getInstance().userAgentValue = context.packageName
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(OsmGeoPoint(10.4660, -84.6450))

                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                            viewModel.addDrawingPoint(com.hato.domain.GeoPoint(p.latitude, p.longitude))
                            return true
                        }
                        override fun longPressHelper(p: OsmGeoPoint): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(mapEventsReceiver))
                }
            },
            update = { mapView ->
                // NUNCA hacer .clear() porque elimina las texturas del mapa base (TilesOverlay)
                // En su lugar, solo eliminamos los polígonos y marcadores que hayamos dibujado nosotros
                mapView.overlays.removeAll { it is Polygon || it is Marker }

                // Dibujar zonas guardadas
                viewModel.savedZones.forEach { zone ->
                    if (zone is PolygonGeofence) {
                        val polygon = Polygon().apply {
                            points = zone.points.map { OsmGeoPoint(it.lat, it.lng) }
                            fillPaint.color = android.graphics.Color.argb(51, 46, 107, 52)
                            outlinePaint.color = android.graphics.Color.argb(255, 18, 82, 30)
                            outlinePaint.strokeWidth = 5f
                        }
                        mapView.overlays.add(polygon)
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
                    mapView.overlays.add(drawingPolygon)

                    viewModel.currentDrawingPoints.forEach { point ->
                        val marker = Marker(mapView).apply {
                            position = OsmGeoPoint(point.lat, point.lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
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
                                    // El mapa se centrará si extraemos la referencia o simplemente guardamos el centro en el estado
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
                    Icon(Icons.Default.AccountBox, contentDescription = "Escanear QR")
                }
            }
        }

        // 3. CONTROLES FLOTANTES DEL MAPA (DERECHA)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { /* Zoom fit */ },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
            }
            FloatingActionButton(
                onClick = { /* Ubicación */ },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            }
            FloatingActionButton(
                onClick = { /* Capas */ },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = null)
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
                    .padding(16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Resumen de Animales (Counters)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("RESUMEN DE ANIMALES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                    Text("${activeAnimal.name} • ${activeAnimal.breed}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary // Forzar texto claro (blanco) sobre el botón azul
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CÓMO LLEGAR (RUTA RÁPIDA)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { }, 
                                modifier = Modifier.weight(1f).height(48.dp), 
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Text("VER RECORRIDO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { }, 
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