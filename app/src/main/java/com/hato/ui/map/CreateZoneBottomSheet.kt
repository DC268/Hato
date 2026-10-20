package com.hato.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateZoneBottomSheet(viewModel: MapViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.cancelCreation() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("NUEVA GEOCERCA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            when (viewModel.createZoneState) {
                CreateZoneState.OPTIONS -> OptionsContent(viewModel)
                CreateZoneState.FORM -> FormContent(viewModel)
                else -> {}
            }
        }
    }
}

@Composable
fun OptionsContent(viewModel: MapViewModel) {
    Text("Seleccione el modo de trazado del potrero", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(16.dp))

    ListItem(
        headlineContent = { Text("Dibujar en el mapa", fontWeight = FontWeight.Bold) },
        supportingContent = { Text("Marque los postes perimetrales sobre la imagen satelital") },
        leadingContent = { Icon(Icons.Default.Create, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.clickable { viewModel.startDrawing() }
    )
    
    ListItem(
        headlineContent = { Text("Recorrer a caballo / vehículo", fontWeight = FontWeight.Bold) },
        supportingContent = { Text("Graba los linderos usando el GPS en tiempo real") },
        leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
        modifier = Modifier.clickable { /* Lógica de Live GPS Tracking */ }
    )
}

@Composable
fun FormContent(viewModel: MapViewModel) {
    var zoneName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("Lote de pastoreo rotacional") }
    var areaHa by remember { mutableStateOf("15.0") }
    var tolerance by remember { mutableStateOf("15") }

    OutlinedTextField(
        value = zoneName,
        onValueChange = { zoneName = it },
        label = { Text("Nombre del Potrero") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Descripción / Tipo de Pasto") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = areaHa,
            onValueChange = { areaHa = it },
            label = { Text("Superficie (ha)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = tolerance,
            onValueChange = { tolerance = it },
            label = { Text("Tolerancia GPS (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = {
            viewModel.saveGeofence(
                zoneName,
                description,
                areaHa.toFloatOrNull() ?: 0f,
                tolerance.toFloatOrNull() ?: 15f
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("GUARDAR Y ACTIVAR GEOCERCA")
    }
}