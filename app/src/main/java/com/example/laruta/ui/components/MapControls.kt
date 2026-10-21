package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.laruta.model.TrackingStatus
import com.example.laruta.model.TravelMode
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatDuration

val TravelMode.icon: ImageVector
    get() = when (this) {
        TravelMode.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        TravelMode.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
        TravelMode.MOTO -> Icons.Filled.TwoWheeler
    }

@Composable
fun mapControlColor(): Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)

@Composable
fun MapSquareButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    containerColor: Color = mapControlColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 56.dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        modifier = modifier.size(width = size, height = if (label != null) size + 8.dp else size)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val tint = if (enabled) contentColor else contentColor.copy(alpha = 0.35f)
            Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(26.dp))
            if (label != null) {
                Spacer(Modifier.height(2.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1)
            }
        }
    }
}

@Composable
fun RouteControls(
    status: TrackingStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTracking = status == TrackingStatus.TRACKING
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MapSquareButton(
            icon = Icons.Filled.PlayArrow,
            contentDescription = "Iniciar recorrido",
            label = if (isTracking) "Grabando" else "Iniciar",
            onClick = onStart,
            enabled = !isTracking,
            containerColor = if (isTracking) MaterialTheme.colorScheme.tertiary else mapControlColor(),
            contentColor = if (isTracking) Color.White else MaterialTheme.colorScheme.primary
        )
        MapSquareButton(
            icon = Icons.Filled.Stop,
            contentDescription = "Parar recorrido",
            label = "Parar",
            onClick = onStop,
            enabled = isTracking,
            contentColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun RailButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color) {
    Surface(onClick = onClick, color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActionRail(
    mode: TravelMode,
    onAddClick: () -> Unit,
    onShowRoute: () -> Unit,
    onCycleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = mapControlColor(),
        shadowElevation = 6.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RailButton(Icons.Filled.AddAPhoto, "Agregar", onAddClick, MaterialTheme.colorScheme.secondary)
            HorizontalDivider(Modifier.width(36.dp), color = MaterialTheme.colorScheme.outline)
            RailButton(Icons.Filled.Route, "Ruta", onShowRoute, MaterialTheme.colorScheme.primary)
            HorizontalDivider(Modifier.width(36.dp), color = MaterialTheme.colorScheme.outline)
            RailButton(mode.icon, mode.label, onCycleMode, MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Zoom + / − (abajo a la izquierda). */
@Composable
fun ZoomControls(onZoomIn: () -> Unit, onZoomOut: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = mapControlColor(),
        shadowElevation = 6.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(onClick = onZoomIn, color = Color.Transparent, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, "Acercar") }
            }
            HorizontalDivider(Modifier.width(28.dp), color = MaterialTheme.colorScheme.outline)
            Surface(onClick = onZoomOut, color = Color.Transparent, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Remove, "Alejar") }
            }
        }
    }
}

/** Centrar en mi ubicación (abajo a la derecha). Azul cuando el mapa te está siguiendo. */
@Composable
fun RecenterButton(isFollowing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MapSquareButton(
        icon = if (isFollowing) Icons.Filled.GpsFixed else Icons.Filled.LocationSearching,
        contentDescription = "Centrar en mi ubicación",
        onClick = onClick,
        modifier = modifier,
        size = 52.dp,
        contentColor = if (isFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
}

/** Botón grande "Iniciar recorrido" (visible cuando no se está grabando). */
@Composable
fun StartTrackingButton(status: TrackingStatus, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
        text = {
            Text(
                if (status == TrackingStatus.FINISHED) "Nuevo recorrido" else "Iniciar recorrido",
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

/** Chip superior: "● REC 00:12:34" mientras se graba. */
@Composable
fun RecordingChip(status: TrackingStatus, elapsedMillis: Long, modifier: Modifier = Modifier) {
    val (dotColor, text) = when (status) {
        TrackingStatus.TRACKING -> MaterialTheme.colorScheme.error to "REC  ${formatDuration(elapsedMillis)}"
        TrackingStatus.FINISHED -> MaterialTheme.colorScheme.tertiary to "Finalizado · ${formatDuration(elapsedMillis)}"
        TrackingStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant to "Listo para salir"
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = mapControlColor(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ------------------------------------------------------------------ Previews

@Preview(name = "Controles izquierda", showBackground = true, backgroundColor = 0xFF242F3E)
@Composable
private fun RouteControlsPreview() {
    LaRutaTheme(darkTheme = true) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RouteControls(status = TrackingStatus.IDLE, onStart = {}, onStop = {})
            RouteControls(status = TrackingStatus.TRACKING, onStart = {}, onStop = {})
        }
    }
}

@Preview(name = "Riel derecho", showBackground = true, backgroundColor = 0xFF242F3E)
@Composable
private fun ActionRailPreview() {
    LaRutaTheme(darkTheme = true) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionRail(mode = TravelMode.WALK, onAddClick = {}, onShowRoute = {}, onCycleMode = {})
            LaRutaTheme(darkTheme = false) {
                ActionRail(mode = TravelMode.MOTO, onAddClick = {}, onShowRoute = {}, onCycleMode = {})
            }
        }
    }
}

@Preview(name = "Zoom, centrar e iniciar", showBackground = true, backgroundColor = 0xFF242F3E)
@Composable
private fun BottomControlsPreview() {
    LaRutaTheme(darkTheme = true) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            ZoomControls(onZoomIn = {}, onZoomOut = {})
            StartTrackingButton(status = TrackingStatus.IDLE, onClick = {})
            RecenterButton(isFollowing = true, onClick = {})
        }
    }
}

@Preview(name = "Chip de grabación", showBackground = true, backgroundColor = 0xFF242F3E)
@Composable
private fun RecordingChipPreview() {
    LaRutaTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordingChip(status = TrackingStatus.IDLE, elapsedMillis = 0)
            RecordingChip(status = TrackingStatus.TRACKING, elapsedMillis = 754_000)
            RecordingChip(status = TrackingStatus.FINISHED, elapsedMillis = 2_745_000)
        }
    }
}
