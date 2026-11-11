package com.hato.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hato.domain.model.AlertLevel
import com.hato.ui.map.MapViewModel
import com.hato.ui.theme.*

@Composable
fun AlertsScreen(viewModel: MapViewModel) {
    var selectedFilter by remember { mutableStateOf<AlertLevel?>(null) } // null = Todas

    val infoCount = viewModel.appAlerts.count { it.level == AlertLevel.INFO }
    val warningCount = viewModel.appAlerts.count { it.level == AlertLevel.WARNING }
    val criticalCount = viewModel.appAlerts.count { it.level == AlertLevel.CRITICAL }

    val filteredAlerts = if (selectedFilter == null) {
        viewModel.appAlerts
    } else {
        viewModel.appAlerts.filter { it.level == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Título de Pantalla
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Centro de Alertas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = if (criticalCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(99.dp)
            ) {
                Text(
                    text = "${viewModel.appAlerts.size} Registros",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (criticalCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = "Monitoreo en 3 niveles: Informativas (Verde), Preventivas (Amarillo) y Urgencias (Rojo).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // PÍLDORAS DE FILTRO POR NIVEL DE ALERTA (3 NIVELES)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            item {
                FilterTabPill(
                    label = "Todas",
                    count = viewModel.appAlerts.size,
                    isSelected = selectedFilter == null,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { selectedFilter = null }
                )
            }
            item {
                FilterTabPill(
                    label = "Informativas",
                    count = infoCount,
                    isSelected = selectedFilter == AlertLevel.INFO,
                    color = Color(0xFF10B981), // Verde establecido en la app
                    onClick = { selectedFilter = AlertLevel.INFO }
                )
            }
            item {
                FilterTabPill(
                    label = "Preventivas",
                    count = warningCount,
                    isSelected = selectedFilter == AlertLevel.WARNING,
                    color = Color(0xFFFBBF24), // Amarillo suave
                    onClick = { selectedFilter = AlertLevel.WARNING }
                )
            }
            item {
                FilterTabPill(
                    label = "Urgentes",
                    count = criticalCount,
                    isSelected = selectedFilter == AlertLevel.CRITICAL,
                    color = Color(0xFFEF4444), // Rojo de urgencia
                    onClick = { selectedFilter = AlertLevel.CRITICAL }
                )
            }
        }

        if (viewModel.appAlerts.isEmpty()) {
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
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = "Sin alertas registradas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Los collares GPS y geocercas se encuentran en estado nominal. Las alertas se registrarán aquí automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (filteredAlerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No hay alertas en esta categoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredAlerts) { alert ->
                    val (cardBorderColor, badgeBg, badgeTextColor, badgeLabel, icon) = when (alert.level) {
                        AlertLevel.INFO -> {
                            Quint(
                                Color(0xFF10B981).copy(alpha = 0.6f),
                                Color(0xFF10B981).copy(alpha = 0.15f),
                                Color(0xFF10B981),
                                "INFORMATIVA",
                                Icons.Default.CheckCircle
                            )
                        }
                        AlertLevel.WARNING -> {
                            Quint(
                                Color(0xFFFBBF24).copy(alpha = 0.6f),
                                Color(0xFFFBBF24).copy(alpha = 0.15f),
                                Color(0xFFF59E0B),
                                "PREVENTIVA",
                                Icons.Default.Warning
                            )
                        }
                        AlertLevel.CRITICAL -> {
                            Quint(
                                Color(0xFFEF4444).copy(alpha = 0.7f),
                                Color(0xFFEF4444).copy(alpha = 0.18f),
                                Color(0xFFEF4444),
                                "URGENCIA",
                                Icons.Default.Notifications
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                        icon,
                                        contentDescription = null,
                                        tint = badgeTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = alert.time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = alert.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(99.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Surface(
                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(99.dp)
            ) {
                Text(
                    text = "$count",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
