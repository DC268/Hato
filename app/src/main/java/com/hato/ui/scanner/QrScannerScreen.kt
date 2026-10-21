package com.hato.ui.scanner

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import com.hato.domain.Animal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QrScannerScreen(
    animals: List<Animal>,
    onClose: () -> Unit,
    onAnimalScanned: (Animal) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) isScanning = true
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser_animation")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )

    fun simulateScanTag(animal: Animal) {
        isScanning = false
        coroutineScope.launch {
            delay(400)
            onAnimalScanned(animal)
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9191C18))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Escáner RFID & QR de Campo",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = laserOffset.dp)
                                .background(Color.Red)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Acerque el lector al arete RFID\no código de barra",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simular Detección de Arete en el Brete:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        animals.take(3).forEach { animal ->
                            Button(
                                onClick = { simulateScanTag(animal) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = animal.displayNumber,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = animal.tagNumber,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
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