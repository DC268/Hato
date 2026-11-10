package com.hato.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.Animal
import com.hato.domain.model.HealthStatus
import com.hato.ui.map.MapViewModel

@Composable
fun AnimalsScreen(
    viewModel: MapViewModel,
    onNavigateToMap: () -> Unit = {}
) {
    var showAddAnimalDialog by remember { mutableStateOf(false) }
    var selectedForAction by remember { mutableStateOf<Animal?>(null) }
    var selectedForEdit by remember { mutableStateOf<Animal?>(null) }

    // Dialog de opciones al presionar el animal (Editar o Ubicar en Mapa)
    selectedForAction?.let { animal ->
        AlertDialog(
            onDismissRequest = { selectedForAction = null },
            icon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text(
                    text = "${animal.name} (${animal.displayNumber})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Arete: ${animal.tagNumber} • Raza: ${animal.breed}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        "Collar GPS: ${animal.trackerId}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Seleccione qué desea hacer con este animal:")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = animal
                        selectedForAction = null
                        selectedForEdit = target
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hacer Cambios")
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.selectedAnimal = animal
                        selectedForAction = null
                        onNavigateToMap()
                    }
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ubicar en el Mapa")
                }
            }
        )
    }

    // Modal de edición del animal
    selectedForEdit?.let { animal ->
        EditAnimalDialog(
            animal = animal,
            onDismiss = { selectedForEdit = null },
            onSave = { updated ->
                viewModel.editAnimal(updated)
                selectedForEdit = null
            },
            onDelete = {
                viewModel.deleteAnimal(animal.id)
                selectedForEdit = null
            }
        )
    }

    // Modal para añadir animal nuevo
    if (showAddAnimalDialog) {
        AddAnimalDialog(
            onDismiss = { showAddAnimalDialog = false },
            onSave = { newAnimal ->
                viewModel.animals.add(newAnimal)
                showAddAnimalDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddAnimalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Animal", fontWeight = FontWeight.Bold)
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
                text = "HATO (ANIMALES REGISTRADOS)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Toca cualquier animal para ubicarlo en el mapa o editar sus datos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.animals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐮", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No tienes animales registrados aún.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Presiona 'Añadir Animal' para registrar el primero.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.animals) { animal ->
                        val isAlert = animal.healthStatus == HealthStatus.ALERT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedForAction = animal },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            animal.displayNumber,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            "${animal.name} • ${animal.breed}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Tag: ${animal.tagNumber} | Edad: ${animal.ageYears} años",
                                            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Collar: ${animal.trackerId}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Ver opciones",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
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
fun EditAnimalDialog(
    animal: Animal,
    onDismiss: () -> Unit,
    onSave: (Animal) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(animal.name) }
    var breed by remember { mutableStateOf(animal.breed) }
    var displayNumber by remember { mutableStateOf(animal.displayNumber) }
    var tagNumber by remember { mutableStateOf(animal.tagNumber) }
    var trackerId by remember { mutableStateOf(animal.trackerId) }
    var ageText by remember { mutableStateOf(animal.ageYears.toString()) }

    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Animal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Animal *") },
                    isError = !isNameValid,
                    singleLine = true
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Raza") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = displayNumber,
                        onValueChange = { displayNumber = it },
                        label = { Text("Número (Ej: 07)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tagNumber,
                        onValueChange = { tagNumber = it },
                        label = { Text("Arete") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Edad (Años)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = trackerId,
                        onValueChange = { trackerId = it },
                        label = { Text("ID Collar GPS") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        onSave(
                            animal.copy(
                                name = name.trim(),
                                breed = breed.trim().ifEmpty { "Cruce" },
                                displayNumber = displayNumber.trim().ifEmpty { "00" },
                                tagNumber = tagNumber.trim().ifEmpty { "#0000" },
                                trackerId = trackerId.trim().ifEmpty { "Sin GPS" },
                                ageYears = ageText.toIntOrNull() ?: animal.ageYears
                            )
                        )
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

@Composable
fun AddAnimalDialog(onDismiss: () -> Unit, onSave: (Animal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var displayNumber by remember { mutableStateOf("") }
    var tagNumber by remember { mutableStateOf("") }
    var trackerId by remember { mutableStateOf("") }

    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Animal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (Ej: Pinta) *") },
                    isError = !isNameValid,
                    supportingText = {
                        if (!isNameValid) Text("El nombre es requerido", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true
                )
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = displayNumber, onValueChange = { displayNumber = it }, label = { Text("Número (Ej: 07)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = tagNumber, onValueChange = { tagNumber = it }, label = { Text("Arete") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = trackerId,
                    onValueChange = { trackerId = it },
                    label = { Text("Conectar Collar GPS (ID o MAC)") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        val newAnimal = Animal(
                            id = "a-${System.currentTimeMillis()}",
                            farmId = "farm-01",
                            name = name.trim(),
                            tagNumber = tagNumber.trim().ifEmpty { "#0000" },
                            displayNumber = displayNumber.trim().ifEmpty { "00" },
                            breed = breed.trim().ifEmpty { "Cruce" },
                            ageYears = 2,
                            trackerId = trackerId.trim().ifEmpty { "Sin GPS" },
                            healthStatus = HealthStatus.HEALTHY
                        )
                        onSave(newAnimal)
                    }
                },
                enabled = isNameValid
            ) {
                Text("Guardar y Sincronizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
