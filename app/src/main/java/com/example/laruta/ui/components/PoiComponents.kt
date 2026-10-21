package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.laruta.model.PointOfInterest
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatCoordinates
import com.example.laruta.util.formatDate
import com.example.laruta.util.formatDateTime
import com.example.laruta.util.formatTime
import java.io.File

@Composable
fun PoiPhoto(poi: PointOfInterest, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f), MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
        if (!LocalInspectionMode.current && poi.photoPath.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(poi.photoPath))
                    .crossfade(true)
                    .build(),
                contentDescription = poi.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun InfoLine(icon: ImageVector, text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PoiCard(poi: PointOfInterest, number: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            PoiPhoto(
                poi,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            )
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "#$number",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(poi.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            InfoLine(Icons.Filled.Schedule, "${formatDate(poi.timestamp)} · ${formatTime(poi.timestamp)}")
            InfoLine(Icons.Filled.GpsFixed, formatCoordinates(poi.latitude, poi.longitude))
            InfoLine(Icons.Filled.Place, poi.address ?: "Buscando dirección…", maxLines = 2)
        }
    }
}

@Composable
fun PoiRow(poi: PointOfInterest, number: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PoiPhoto(
            poi,
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("#$number · ${poi.title}", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            InfoLine(Icons.Filled.Schedule, formatDateTime(poi.timestamp))
            InfoLine(Icons.Filled.GpsFixed, formatCoordinates(poi.latitude, poi.longitude))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PoiDetailDialog(
    poi: PointOfInterest,
    onDismiss: () -> Unit,
    onShowOnMap: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(poi.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PoiPhoto(
                    poi,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.height(4.dp))
                InfoLine(Icons.Filled.Schedule, "Tomada el ${formatDateTime(poi.timestamp)}")
                InfoLine(Icons.Filled.GpsFixed, "Lat/Lng: ${formatCoordinates(poi.latitude, poi.longitude)}")
                poi.accuracyMeters?.let { InfoLine(Icons.Filled.GpsFixed, "Precisión del GPS: ±${it.toInt()} m") }
                InfoLine(Icons.Filled.Place, poi.address ?: "Dirección no disponible", maxLines = 3)
            }
        },
        confirmButton = {
            TextButton(onClick = onShowOnMap) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ver en el mapa")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    )
}

@Preview(name = "Tarjeta de foto", showBackground = true, widthDp = 200)
@Composable
private fun PoiCardPreview() {
    LaRutaTheme(darkTheme = true) {
        PoiCard(PreviewData.pois.first(), number = 1, onClick = {}, modifier = Modifier.padding(8.dp))
    }
}

@Preview(name = "Fila de foto", showBackground = true, widthDp = 360)
@Composable
private fun PoiRowPreview() {
    LaRutaTheme(darkTheme = false) {
        Surface { PoiRow(PreviewData.pois[1], number = 2, onClick = {}) }
    }
}

@Preview(name = "Detalle de foto", showBackground = true)
@Composable
private fun PoiDetailDialogPreview() {
    LaRutaTheme(darkTheme = true) {
        PoiDetailDialog(PreviewData.pois.first(), onDismiss = {}, onShowOnMap = {}, onDelete = {})
    }
}
