package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.laruta.model.TrackingState
import com.example.laruta.model.TravelMode
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatDistance
import com.example.laruta.util.formatDuration
import com.example.laruta.util.formatSpeed
import com.example.laruta.util.formatSteps

@Composable
private fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TrackingStatsCard(state: TrackingState, strideMeters: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = mapControlColor(),
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn("Pasos", formatSteps(state.steps), Modifier.weight(1f))
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                StatColumn("Distancia", formatDistance(state.mainDistanceMeters(strideMeters)), Modifier.weight(1f))
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                StatColumn("Tiempo", formatDuration(state.elapsedMillis), Modifier.weight(1.1f))
                VerticalDivider(color = MaterialTheme.colorScheme.outline)
                StatColumn("Velocidad", formatSpeed(state.speedMps), Modifier.weight(1.1f))
            }
            Spacer(Modifier.height(8.dp))
            val source = if (state.mode == TravelMode.MOTO) "Distancia por GPS (modo moto)" else state.stepSource.label
            val gps = state.accuracyMeters?.let { "GPS ±${it.toInt()} m" } ?: "Buscando GPS…"
            Text(
                text = "$source · $gps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun StatTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TrackingSummaryGrid(state: TrackingState, strideMeters: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Icons.Filled.PhotoLibrary, "Fotos", "${state.pois.size}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            StatTile(Icons.Filled.Straighten, "Distancia", formatDistance(state.mainDistanceMeters(strideMeters)), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Icons.AutoMirrored.Filled.DirectionsWalk, "Pasos", formatSteps(state.steps), MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            StatTile(Icons.Filled.Timer, "Tiempo", formatDuration(state.elapsedMillis), Color(0xFF9B7BFF), Modifier.weight(1f))
        }
    }
}


@Preview(name = "Tarjeta de estadísticas", showBackground = true, backgroundColor = 0xFF242F3E, widthDp = 380)
@Composable
private fun TrackingStatsCardPreview() {
    LaRutaTheme(darkTheme = true) {
        TrackingStatsCard(PreviewData.trackingState, 0.75f, Modifier.padding(16.dp))
    }
}

@Preview(name = "Resumen 2x2 · claro", showBackground = true, backgroundColor = 0xFFF3F5F9, widthDp = 380)
@Composable
private fun TrackingSummaryGridPreview() {
    LaRutaTheme(darkTheme = false) {
        TrackingSummaryGrid(PreviewData.trackingState, 0.75f, Modifier.padding(16.dp))
    }
}
