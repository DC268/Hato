package com.hato.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.CircleGeofence
import com.hato.domain.model.Geofence
import com.hato.domain.model.PolygonGeofence
import com.hato.ui.map.MapViewModel
import com.hato.ui.theme.ThemeState

@Composable
fun ZonesScreen(
    viewModel: MapViewModel,
    onNavigateToMap: () -> Unit
) {
    var selectedForEdit by remember { mutableStateOf<Geofence?>(null) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    // Modal de edición del potrero
    selectedForEdit?.let { zone ->
        EditZoneDialog(
            zone = zone,
            onDismiss = { selectedForEdit = null },
            onSave = { updated ->
                viewModel.editGeofence(updated)
                selectedForEdit = null
            },
            onDelete = {
                viewModel.deleteGeofence(zone.id)
                selectedForEdit = null
            }
        )
    }

    val activeCount = viewModel.savedZones.count { it.active }
    val restingCount = viewModel.savedZones.count { !it.active }

    val filterOptions = listOf(
        "Todos (${viewModel.savedZones.size})" to "Todos",
        "En Pastoreo ($activeCount)" to "Pastoreo",
        "En Descanso ($restingCount)" to "Descanso",
        "Reserva Hídrica" to "Reserva"
    )

    val filteredZones = remember(viewModel.savedZones, selectedFilter) {
        when (selectedFilter) {
            "Pastoreo" -> viewModel.savedZones.filter { it.active }
            "Descanso" -> viewModel.savedZones.filter { !it.active }
            "Reserva" -> viewModel.savedZones.filter { it.name.contains("reserva", ignoreCase = true) || it.name.contains("agua", ignoreCase = true) }
            else -> viewModel.savedZones
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToMap,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Potrero", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingPadding(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Barra Superior Brand Hato Digital
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🚜", fontSize = 16.sp)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Hato",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        " Digital",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "EN LÍNEA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Encabezado de la Pantalla y Botón de Recarga
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Potreros y Geocercas",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(99.dp)
                            ) {
                                Text(
                                    text = "${viewModel.savedZones.size} Activos",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Actualizar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Toca cualquier potrero para calibrar geocercas, límites de pastoreo o rotación de lote.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 3. Filtros Horizontales en Carrusel
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { (label, key) ->
                        val isSelected = selectedFilter == key
                        Surface(
                            onClick = { selectedFilter = key },
                            shape = RoundedCornerShape(99.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Banner Índice NDVI Satelital
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ThemeState.isDarkMode.value) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color(0xFFF0FDF4)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (ThemeState.isDarkMode.value) MaterialTheme.colorScheme.outlineVariant else Color(0xFFD1EFE0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🛰️", fontSize = 18.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ÍNDICE NDVI SATELITAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Biomasa vigorosa en ${viewModel.savedZones.firstOrNull()?.name ?: "Lote Norte"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Calidad de forraje estimada: 84% óptima",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 5. Lista de Potreros
            if (filteredZones.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌾", fontSize = 32.sp)
                                }
                            }
                            Text(
                                text = "No hay potreros guardados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Pulsa '+ Crear Potrero' para trazar los límites en el mapa y configurar geocercas satelitales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(filteredZones, key = { _, z -> z.id }) { index, zone ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedForEdit = zone },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column {
                            // Cabecera con imagen o gradiente pastoral
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(125.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF285E3B),
                                                Color(0xFF163E24)
                                            )
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                // Etiquetas superiores
                                Row(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = Color.White.copy(alpha = 0.9f)
                                    ) {
                                        Text(
                                            text = "Pastoreo #${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F1E17),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = if (zone.active) Color(0xFF006C49) else Color.White.copy(alpha = 0.9f)
                                    ) {
                                        Text(
                                            text = if (zone.active) "Ocupado" else "En Descanso",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (zone.active) Color.White else Color(0xFF0F1E17),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                // Botón circular de editar
                                Surface(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.TopEnd)
                                        .clickable { selectedForEdit = zone },
                                    shape = CircleShape,
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(0xFF0F1E17),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Nombre y Área en la parte inferior del banner
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = zone.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "${String.format("%.1f", zone.areaHa)} ha",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.95f)
                                    )
                                }
                            }

                            // Cuerpo de la tarjeta
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Descripción de rotación
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (zone.description.isNotBlank()) zone.description else "Pastoreo rotacional intensivo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Datos de telemetría y cabezas asignadas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("📍", fontSize = 12.sp)
                                            Text(
                                                "±${zone.toleranceMeters.toInt()}m GPS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("🐾", fontSize = 12.sp)
                                            Column {
                                                Text(
                                                    "Hato Asignado",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    "${(zone.areaHa * 1.6).toInt().coerceAtLeast(1)} cabezas",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                // Barra de Progreso de Rebrote Forrajero
                                val regrowthPct = (65 + (zone.id.hashCode().mod(30))).coerceIn(60, 98)
                                val daysChange = (zone.id.hashCode().mod(5) + 1)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Rebrote al $regrowthPct%",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            "Cambio en $daysChange días →",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { regrowthPct / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Tarjeta Resumen: Capacidad Forrajera Consolidada
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📊", fontSize = 14.sp)
                                Text(
                                    "CAPACIDAD FORRAJERA CONSOLIDADA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    "Actualizado hoy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Superficie Total Mapeada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${String.format("%.1f", viewModel.savedZones.sumOf { it.areaHa.toDouble() })} ha",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Carga Animal Global",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "1.27 UGM / ha",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper class for Padding
private fun PaddingPadding(top: androidx.compose.ui.unit.Dp, bottom: androidx.compose.ui.unit.Dp): PaddingValues {
    return PaddingValues(top = top, bottom = bottom)
}

@Composable
fun EditZoneDialog(
    zone: Geofence,
    onDismiss: () -> Unit,
    onSave: (Geofence) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(zone.name) }
    var description by remember { mutableStateOf(zone.description) }
    var toleranceText by remember { mutableStateOf(zone.toleranceMeters.toString()) }
    var areaText by remember { mutableStateOf(zone.areaHa.toString()) }

    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Potrero / Geocerca", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Potrero *") },
                    isError = !isNameValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isNameValid) {
                    Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Tipo de Pasto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text("Superficie (ha)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toleranceText,
                        onValueChange = { toleranceText = it },
                        label = { Text("Tolerancia (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        val updated = when (zone) {
                            is PolygonGeofence -> PolygonGeofence(
                                id = zone.id,
                                farmId = zone.farmId,
                                name = name.trim(),
                                toleranceMeters = toleranceText.toFloatOrNull() ?: zone.toleranceMeters,
                                points = zone.points,
                                active = zone.active,
                                description = description.trim(),
                                areaHa = areaText.toFloatOrNull() ?: zone.areaHa
                            )
                            is CircleGeofence -> CircleGeofence(
                                id = zone.id,
                                farmId = zone.farmId,
                                name = name.trim(),
                                toleranceMeters = toleranceText.toFloatOrNull() ?: zone.toleranceMeters,
                                center = zone.center,
                                radiusMeters = zone.radiusMeters,
                                active = zone.active,
                                description = description.trim(),
                                areaHa = areaText.toFloatOrNull() ?: zone.areaHa
                            )
                            else -> zone
                        }
                        onSave(updated)
                    }
                },
                enabled = isNameValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar Cambios", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
