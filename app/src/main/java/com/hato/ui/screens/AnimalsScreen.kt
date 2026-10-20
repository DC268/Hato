package com.hato.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.Animal
import com.hato.domain.HealthStatus
import com.hato.ui.map.MapViewModel

@Composable
fun AnimalsScreen(viewModel: MapViewModel) {
    var showAddAnimalDialog by remember { mutableStateOf(false) }

    if (showAddAnimalDialog) {
        AddAnimalDialog(
            onDismiss = { showAddAnimalDialog = false },
            onSave = { animal ->
                viewModel.animals.add(animal)
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
                text = "HATO (ANIMALES)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.animals) { animal ->
                    val isAlert = animal.healthStatus == HealthStatus.ALERT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    animal.displayNumber,
                                    color = androidx.compose.ui.graphics.Color.White,
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
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddAnimalDialog(onDismiss: () -> Unit, onSave: (Animal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var displayNumber by remember { mutableStateOf("") }
    var tagNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Animal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre (Ej: Pinta)") })
                OutlinedTextField(value = breed, onValueChange = { breed = it }, label = { Text("Raza") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = displayNumber, onValueChange = { displayNumber = it }, label = { Text("Número (Ej: 07)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = tagNumber, onValueChange = { tagNumber = it }, label = { Text("Arete") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newAnimal = Animal(
                    id = "a-${System.currentTimeMillis()}",
                    farmId = "farm-01",
                    name = name.ifEmpty { "Sin Nombre" },
                    tagNumber = tagNumber.ifEmpty { "#0000" },
                    displayNumber = displayNumber.ifEmpty { "00" },
                    breed = breed.ifEmpty { "Cruce" },
                    ageYears = 2,
                    trackerId = "t-${System.currentTimeMillis()}",
                    healthStatus = HealthStatus.HEALTHY
                )
                onSave(newAnimal)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}