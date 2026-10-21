package com.example.laruta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.TrackingState
import com.example.laruta.model.TrackingStatus
import com.example.laruta.model.UserSettings
import com.example.laruta.ui.components.InfoLine
import com.example.laruta.ui.components.PoiRow
import com.example.laruta.ui.components.StatTile
import com.example.laruta.ui.components.TrackingSummaryGrid
import com.example.laruta.ui.components.icon
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatAverageSpeed
import com.example.laruta.util.formatCoordinates
import com.example.laruta.util.formatDistance
import com.example.laruta.util.formatSpeed

/** Tarjeta con título para agrupar información. */
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

/** Pestaña "Lista" del Monitor: el recorrido en números + las fotos tomadas. */
@Composable
fun RouteSummaryList(
    state: TrackingState,
    settings: UserSettings,
    onPoiClick: (PointOfInterest) -> Unit,
    onOpenPois: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val statusText = when (state.status) {
                TrackingStatus.IDLE -> "Sin iniciar"
                TrackingStatus.TRACKING -> "En curso"
                TrackingStatus.FINISHED -> "Finalizado"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(state.mode.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Recorrido ${statusText.lowercase()} · ${state.mode.label}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item { TrackingSummaryGrid(state, settings.strideMeters) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.Speed, "Velocidad actual", formatSpeed(state.speedMps), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatTile(Icons.Filled.Speed, "Velocidad media", formatAverageSpeed(state.mainDistanceMeters(settings.strideMeters), state.elapsedMillis), MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }

        item {
            SectionCard("Sensores y GPS") {
                InfoLine(Icons.Filled.Sensors, "Pasos: ${state.stepSource.label}")
                InfoLine(Icons.Filled.Straighten, "Distancia por pasos: ${formatDistance(state.sensorDistanceMeters(settings.strideMeters))} (zancada ${settings.strideMeters} m)")
                InfoLine(Icons.Filled.Route, "Distancia por GPS: ${formatDistance(state.gpsDistanceMeters)} · ${state.route.size} puntos en la ruta")
                InfoLine(
                    Icons.Filled.GpsFixed,
                    state.currentLocation?.let {
                        "Ubicación: ${formatCoordinates(it.latitude, it.longitude)}" +
                            (state.accuracyMeters?.let { a -> " (±${a.toInt()} m)" } ?: "")
                    } ?: "Ubicación: sin señal todavía"
                )
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Puntos de interés (${state.pois.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (state.pois.isNotEmpty()) TextButton(onClick = onOpenPois) { Text("Ver dashboard") }
            }
        }

        if (state.pois.isEmpty()) {
            item {
                Text(
                    "Toca \"Agregar\" en el mapa para tomar la primera foto del recorrido.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            itemsIndexed(state.pois, key = { _, poi -> poi.id }) { index, poi ->
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    PoiRow(poi = poi, number = index + 1, onClick = { onPoiClick(poi) })
                }
            }
        }
    }
}

@Preview(name = "Lista del recorrido", showBackground = true, backgroundColor = 0xFF12161F, heightDp = 900)
@Composable
private fun RouteSummaryListPreview() {
    LaRutaTheme(darkTheme = true) {
        RouteSummaryList(PreviewData.trackingState, PreviewData.settings, onPoiClick = {}, onOpenPois = {}, modifier = Modifier.fillMaxSize())
    }
}
