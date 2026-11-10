package com.hato.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.CircleGeofence
import com.hato.domain.model.Geofence
import com.hato.domain.model.PolygonGeofence
import com.hato.ui.map.MapViewModel
import com.hato.ui.theme.*

@Composable
fun ZonesScreen(
    viewModel: MapViewModel,
    onNavigateToMap: () -> Unit
) {
    var selectedForEdit by remember { mutableStateOf<Geofence?>(null) }

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

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToMap,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Potrero", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Potreros y Geocercas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = SecondaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "${viewModel.savedZones.size} Zonas",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Toca cualquier potrero para calibrar sus límites, cambiar nombre o administrar su pastoreo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (viewModel.savedZones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌾", fontSize = 32.sp)
                            }
                        }
                        Text(
                            text = "No hay potreros guardados aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Pulsa 'Crear Potrero' para trazar un polígono o círculo en el mapa y asignar el perímetro de seguridad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.savedZones, key = { it.id }) { zone ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedForEdit = zone },
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = SurfaceContainerHigh
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🌾", fontSize = 22.sp)
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = zone.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${String.format("%.1f", zone.areaHa)} ha • ${zone.description}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Surface(
                                                color = SurfaceContainerHigh,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "TOLERANCIA ${zone.toleranceMeters.toInt()}m",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
