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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.CircleGeofence
import com.hato.domain.model.Geofence
import com.hato.domain.model.PolygonGeofence
import com.hato.ui.map.MapViewModel

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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Potrero", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "POTREROS Y GEOCERCAS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Toca cualquier potrero para editar su nombre, límites o eliminarlo",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.savedZones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No tienes potreros guardados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pulsa 'Crear Potrero' para delimitar uno en el mapa.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.savedZones) { zone ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedForEdit = zone },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        zone.name,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${String.format("%.1f", zone.areaHa)} ha • ${zone.description}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Tolerancia GPS: ${zone.toleranceMeters}m",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
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
        title = { Text("Editar Potrero / Geocerca", fontWeight = FontWeight.Bold) },
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
                    Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
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
                enabled = isNameValid
            ) {
                Text("Guardar Cambios")
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
