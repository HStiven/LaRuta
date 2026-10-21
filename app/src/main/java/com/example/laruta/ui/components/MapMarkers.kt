package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.laruta.model.TravelMode
import com.example.laruta.ui.theme.LaRutaTheme

/*
 * Estos composables se dibujan DENTRO del mapa con MarkerComposable (maps-compose):
 * Compose los convierte en la imagen del marcador. Como son composables normales,
 * tienen @Preview y puedes cambiar su diseño aquí.
 */

/** Marcador del usuario: persona caminando, corredor o moto, con halo. */
@Composable
fun UserMarkerContent(mode: TravelMode, isTracking: Boolean, modifier: Modifier = Modifier) {
    val accent = if (isTracking) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Box(modifier = modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(56.dp)
                .background(accent.copy(alpha = 0.22f), CircleShape)
        )
        Box(
            Modifier
                .size(38.dp)
                .background(accent, CircleShape)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(mode.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

/** Marcador de un punto de interés (foto): pin naranja con cámara. */
@Composable
fun PoiMarkerContent(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(40.dp)
                .background(color, CircleShape)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        // Punta del pin: un cuadrado girado 45°
        Box(
            Modifier
                .offset(y = (-7).dp)
                .size(12.dp)
                .rotate(45f)
                .background(color)
        )
    }
}

/** Marcador del punto de inicio del recorrido. */
@Composable
fun StartMarkerContent(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(28.dp)
            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Preview(name = "Marcadores del mapa", showBackground = true, backgroundColor = 0xFF242F3E)
@Composable
private fun MarkersPreview() {
    LaRutaTheme(darkTheme = true) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserMarkerContent(mode = TravelMode.WALK, isTracking = false)
            UserMarkerContent(mode = TravelMode.RUN, isTracking = true)
            UserMarkerContent(mode = TravelMode.MOTO, isTracking = true)
            PoiMarkerContent()
            StartMarkerContent()
        }
    }
}
