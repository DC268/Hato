package com.hato.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.Animal
import com.hato.domain.model.HealthStatus
import com.hato.ui.map.MapViewModel
import com.hato.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Constante de referencia de venta de ganado en pie en Colombia (Feria ganadera / Subastar / Fedegán)
const val ESTIMATED_COP_PER_KG = 9000L

fun formatCopCurrency(amount: Long): String {
    val symbols = DecimalFormatSymbols(Locale("es", "CO")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val formatter = DecimalFormat("$#,### COP", symbols)
    return formatter.format(amount)
}

val COMMON_CATTLE_BREEDS = listOf(
    "Brahman",
    "Holstein",
    "Jersey",
    "Angus",
    "Simmental",
    "Gyr",
    "Charolais",
    "Brangus",
    "Nelore",
    "Normando",
    "Pardo Suizo",
    "Mestizo / Criollo",
    "Otra"
)

@Composable
fun AnimalsScreen(
    viewModel: MapViewModel,
    onNavigateToMap: () -> Unit = {}
) {
    var showAddAnimalDialog by remember { mutableStateOf(false) }
    var selectedForAction by remember { mutableStateOf<Animal?>(null) }
    var selectedForEdit by remember { mutableStateOf<Animal?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    // Filtrado en vivo de animales creados
    val filteredAnimals = viewModel.animals.filter { animal ->
        val matchesSearch = searchQuery.isBlank() ||
                animal.name.contains(searchQuery, ignoreCase = true) ||
                animal.tagNumber.contains(searchQuery, ignoreCase = true) ||
                animal.trackerId.contains(searchQuery, ignoreCase = true) ||
                animal.breed.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ACTIVOS" -> animal.healthStatus != HealthStatus.ALERT
            "ALERTA" -> animal.healthStatus == HealthStatus.ALERT
            else -> true
        }
        matchesSearch && matchesFilter
    }

    // Modal de selección de acción: Editar vs Ubicar en el Mapa
    selectedForAction?.let { animal ->
        val weight = animal.weightKg ?: 450f
        val estValueCop = (weight * ESTIMATED_COP_PER_KG).toLong()

        AlertDialog(
            onDismissRequest = { selectedForAction = null },
            icon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text(
                    text = "${animal.name} (${animal.displayNumber})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Arete: #${animal.tagNumber} • Raza: ${animal.breed} • Edad: ${animal.ageYears} años",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Peso en pie: ${weight.toInt()} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                "Valor Estimado de Venta (Colombia):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCopCurrency(estValueCop),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        "Collar / Dispositivo: ${animal.trackerId}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Selecciona la acción a realizar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    Text("Hacer Cambios", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.selectedAnimal = animal
                        selectedForAction = null
                        onNavigateToMap()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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

    // Modal para registrar un nuevo animal
    if (showAddAnimalDialog) {
        AddAnimalDialog(
            onDismiss = { showAddAnimalDialog = false },
            onSave = { newAnimal ->
                viewModel.addAnimal(newAnimal)
                showAddAnimalDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddAnimalDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Animal", style = MaterialTheme.typography.labelLarge)
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
            // Tira superior de telemetría y estado de red
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        text = "MONITOREO SATELITAL · 4G / BLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "EN LÍNEA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Título de pantalla y contador de cabezas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hato Registrado",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "${viewModel.animals.size} Cabezas",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Toca cualquier animal para ubicarlo en el mapa o editar sus datos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Buscar por arete, collar o raza...", style = MaterialTheme.typography.bodySmall)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Filtros tipo píldoras
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                val filters = listOf(
                    "TODOS" to "${viewModel.animals.size}",
                    "ACTIVOS" to "${viewModel.animals.count { it.healthStatus != HealthStatus.ALERT }}",
                    "ALERTA" to "${viewModel.animals.count { it.healthStatus == HealthStatus.ALERT }}"
                )
                items(filters) { (filterKey, count) ->
                    val isSelected = selectedFilter == filterKey
                    Surface(
                        modifier = Modifier.clickable { selectedFilter = filterKey },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(99.dp),
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = filterKey.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(99.dp)
                            ) {
                                Text(
                                    text = count,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Lista de tarjetas de animales
            if (viewModel.animals.isEmpty()) {
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
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🐮", fontSize = 32.sp)
                            }
                        }
                        Text(
                            text = "No hay animales registrados aún",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Presiona 'Añadir Animal' para dar de alta tu primera cabeza y vincular su collar o dispositivo GPS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (filteredAnimals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron animales con el filtro seleccionado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredAnimals, key = { it.id }) { animal ->
                        val isAlert = animal.healthStatus == HealthStatus.ALERT
                        val battery = viewModel.getBatteryLevel(animal.id)
                        val weight = animal.weightKg ?: 450f
                        val estCop = (weight * ESTIMATED_COP_PER_KG).toLong()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedForAction = animal },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Insignia con Emoji de Cabeza de Vaca 🐮
                                    Surface(
                                        modifier = Modifier.size(46.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🐮", fontSize = 24.sp)
                                        }
                                    }

                                    // Contenido principal
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = animal.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            // Pill de estado
                                            Surface(
                                                color = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(99.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(
                                                                if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                                CircleShape
                                                            )
                                                    )
                                                    Text(
                                                        text = if (isAlert) "ALERTA" else "EN VIVO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isAlert) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${animal.breed} · ${if (animal.currentZoneId != null) "Potrero Asignado" else "Sin Potrero"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )

                                        // Metadatos principales (Arete, Edad, Sincronización)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "#${animal.tagNumber}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text("·", color = MaterialTheme.colorScheme.outline)

                                            Text(
                                                text = "Edad: ${animal.ageYears} años",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text("·", color = MaterialTheme.colorScheme.outline)

                                            Text(
                                                text = "Sincronizado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Fila con Peso en pie y Estimado de Venta en Colombia
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Peso: ${weight.toInt()} kg",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Est. Venta: ${formatCopCurrency(estCop)}",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Micro Footer de Telemetría
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${animal.trackerId} · 4G LTE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = "$battery% ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (battery < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var weightText by remember { mutableStateOf(animal.weightKg?.toInt()?.toString() ?: "450") }

    var isBreedDropdownExpanded by remember { mutableStateOf(false) }

    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Animal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Animal *") },
                    isError = !isNameValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // DESPLEGABLE DE RAZAS (VACAS)
                ExposedDropdownMenuBox(
                    expanded = isBreedDropdownExpanded,
                    onExpandedChange = { isBreedDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        readOnly = true,
                        label = { Text("Raza del Ganado *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBreedDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isBreedDropdownExpanded,
                        onDismissRequest = { isBreedDropdownExpanded = false }
                    ) {
                        COMMON_CATTLE_BREEDS.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    breed = b
                                    isBreedDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // IDENTIFICADORES: TECLADO NUMÉRICO Y FILTRADO AUTOMÁTICO
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = displayNumber,
                        onValueChange = { input -> displayNumber = input.filter { it.isDigit() } },
                        label = { Text("Número (Ej: 07)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tagNumber,
                        onValueChange = { input -> tagNumber = input.filter { it.isDigit() } },
                        label = { Text("Arete (Ej: 0342)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // EDAD Y PESO EN PIE
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { input -> ageText = input.filter { it.isDigit() } },
                        label = { Text("Edad (Años)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { input -> weightText = input.filter { it.isDigit() } },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // BANNER DE CÁLCULO DE GANANCIA / VENTA ESTIMADA EN COLOMBIA
                val currentWeight = weightText.toFloatOrNull() ?: 0f
                val estValCop = (currentWeight * ESTIMATED_COP_PER_KG).toLong()

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Venta Estimada (Colombia):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tasa ref. $9.000 COP/kg en pie",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = formatCopCurrency(estValCop),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = trackerId,
                    onValueChange = { trackerId = it },
                    label = { Text("Collar / Tracker GPS") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        onSave(
                            animal.copy(
                                name = name.trim(),
                                breed = breed.trim().ifEmpty { "Mestizo / Criollo" },
                                displayNumber = displayNumber.trim().ifEmpty { animal.displayNumber },
                                tagNumber = tagNumber.trim().ifEmpty { animal.tagNumber },
                                trackerId = trackerId.trim().ifEmpty { animal.trackerId },
                                ageYears = ageText.toIntOrNull() ?: animal.ageYears,
                                weightKg = weightText.toFloatOrNull() ?: (animal.weightKg ?: 450f)
                            )
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalDialog(
    onDismiss: () -> Unit,
    onSave: (Animal) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("Brahman") }
    var displayNumber by remember { mutableStateOf("") }
    var tagNumber by remember { mutableStateOf("") }
    var trackerId by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("2") }
    var weightText by remember { mutableStateOf("450") }

    var isBreedDropdownExpanded by remember { mutableStateOf(false) }

    val isNameValid = name.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Animal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Animal *") },
                    isError = !isNameValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // DESPLEGABLE DE RAZA (VACAS)
                ExposedDropdownMenuBox(
                    expanded = isBreedDropdownExpanded,
                    onExpandedChange = { isBreedDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        readOnly = true,
                        label = { Text("Raza del Ganado *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBreedDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isBreedDropdownExpanded,
                        onDismissRequest = { isBreedDropdownExpanded = false }
                    ) {
                        COMMON_CATTLE_BREEDS.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    breed = b
                                    isBreedDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // IDENTIFICADORES: TECLADO NUMÉRICO Y FILTRADO AUTOMÁTICO
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = displayNumber,
                        onValueChange = { input -> displayNumber = input.filter { it.isDigit() } },
                        label = { Text("Número (Ej: 07)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = tagNumber,
                        onValueChange = { input -> tagNumber = input.filter { it.isDigit() } },
                        label = { Text("Arete (Ej: 0342)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // EDAD Y PESO EN PIE
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { input -> ageText = input.filter { it.isDigit() } },
                        label = { Text("Edad (Años)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { input -> weightText = input.filter { it.isDigit() } },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // BANNER DE CÁLCULO DE GANANCIA / VENTA ESTIMADA EN COLOMBIA
                val currentWeight = weightText.toFloatOrNull() ?: 0f
                val estValCop = (currentWeight * ESTIMATED_COP_PER_KG).toLong()

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Venta Estimada (Colombia):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tasa ref. $9.000 COP/kg en pie",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = formatCopCurrency(estValCop),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = trackerId,
                    onValueChange = { trackerId = it },
                    label = { Text("Collar / Tracker GPS") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNameValid) {
                        val num = displayNumber.trim().ifEmpty { (10..99).random().toString() }
                        val tag = tagNumber.trim().ifEmpty { "${(1000..9999).random()}" }
                        val trk = trackerId.trim().ifEmpty { "TRK-${(1000..9999).random()}" }
                        val newAnimal = Animal(
                            id = "a-${System.currentTimeMillis()}",
                            farmId = "farm-01",
                            name = name.trim(),
                            tagNumber = tag,
                            displayNumber = num,
                            breed = breed.trim().ifEmpty { "Mestizo / Criollo" },
                            ageYears = ageText.toIntOrNull() ?: 2,
                            weightKg = weightText.toFloatOrNull() ?: 450f,
                            photoUrl = null,
                            notes = null,
                            trackerId = trk,
                            currentZoneId = null,
                            healthStatus = HealthStatus.HEALTHY
                        )
                        onSave(newAnimal)
                    }
                },
                enabled = isNameValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Registrar", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
