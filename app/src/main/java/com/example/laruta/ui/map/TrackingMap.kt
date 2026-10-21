package com.example.laruta.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.example.laruta.R
import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.TrackingState
import com.example.laruta.ui.components.PoiMarkerContent
import com.example.laruta.ui.components.StartMarkerContent
import com.example.laruta.ui.components.UserMarkerContent
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatCoordinates
import com.example.laruta.util.formatDateTime
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState

/** Tipos de mapa de Google Maps que se alternan con el botón de capas. */
enum class MapTypeOption(val label: String, val icon: ImageVector, val googleType: MapType) {
    NORMAL("Mapa", Icons.Filled.Map, MapType.NORMAL),
    HYBRID("Satélite", Icons.Filled.Satellite, MapType.HYBRID),
    TERRAIN("Relieve", Icons.Filled.Terrain, MapType.TERRAIN);

    fun next(): MapTypeOption = entries[(ordinal + 1) % entries.size]
}

/** Centro de Bogotá: posición inicial de la cámara antes de tener GPS. */
val BOGOTA = LatLng(4.6533, -74.0836)

/**
 * Mapa de GOOGLE MAPS (Maps SDK for Android vía maps-compose) con:
 *  - marcador del usuario (persona / corredor / moto)
 *  - PolyLine de la ruta recorrida
 *  - marcador por cada foto (punto de interés)
 *  - estilo oscuro cuando el sensor de luz activa el modo oscuro
 */
@Composable
fun TrackingMap(
    state: TrackingState,
    isDarkTheme: Boolean,
    mapType: MapTypeOption,
    cameraPositionState: CameraPositionState,
    onPoiClick: (PointOfInterest) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    // El SDK de mapas no se puede dibujar en el panel de Preview: mostramos un fondo de ejemplo
    if (LocalInspectionMode.current) {
        MapPreviewPlaceholder(isDarkTheme, modifier)
        return
    }

    val context = LocalContext.current
    val darkStyle = remember(isDarkTheme) {
        if (isDarkTheme) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null
    }
    val properties = remember(darkStyle, mapType) {
        MapProperties(
            mapType = mapType.googleType,
            // El estilo solo aplica al mapa normal; satélite/relieve se ven igual de día y de noche
            mapStyleOptions = if (mapType == MapTypeOption.NORMAL) darkStyle else null,
            isBuildingEnabled = true
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,      // usamos nuestros propios botones
            myLocationButtonEnabled = false,  // idem
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }
    val routeColor = MaterialTheme.colorScheme.primary

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        contentPadding = contentPadding
    ) {
        // ----- Ruta recorrida (PolyLine) -----
        if (state.route.size >= 2) {
            Polyline(
                points = state.route,
                color = routeColor,
                width = 14f,
                jointType = JointType.ROUND,
                startCap = RoundCap(),
                endCap = RoundCap()
            )
        }

        // ----- Inicio del recorrido -----
        state.route.firstOrNull()?.let { start ->
            key(start) {
                MarkerComposable(
                    start,
                    state = rememberMarkerState(position = start),
                    title = "Inicio del recorrido",
                    anchor = Offset(0.5f, 0.5f),
                    zIndex = 1f
                ) {
                    StartMarkerContent()
                }
            }
        }

        // ----- Puntos de interés (una foto = un marcador) -----
        state.pois.forEach { poi ->
            key(poi.id) {
                MarkerComposable(
                    poi.id,
                    state = rememberMarkerState(position = poi.latLng),
                    title = poi.title,
                    snippet = "${formatDateTime(poi.timestamp)} · ${formatCoordinates(poi.latitude, poi.longitude)}",
                    anchor = Offset(0.5f, 1f),
                    zIndex = 2f,
                    onInfoWindowClick = { onPoiClick(poi) }
                ) {
                    PoiMarkerContent()
                }
            }
        }

        // ----- Usuario -----
        state.currentLocation?.let { location ->
            val userMarker = rememberMarkerState(position = location)
            LaunchedEffect(location) { userMarker.position = location } // se mueve con el GPS
            MarkerComposable(
                state.mode, state.isTracking, isDarkTheme,
                state = userMarker,
                title = "Tú estás aquí",
                anchor = Offset(0.5f, 0.5f),
                zIndex = 10f
            ) {
                UserMarkerContent(mode = state.mode, isTracking = state.isTracking)
            }
        }
    }
}

/** Fondo falso tipo mapa (solo para los @Preview). */
@Composable
fun MapPreviewPlaceholder(isDarkTheme: Boolean, modifier: Modifier = Modifier) {
    val background = if (isDarkTheme) Color(0xFF242F3E) else Color(0xFFE8EAED)
    val street = if (isDarkTheme) Color(0xFF38414E) else Color(0xFFFFFFFF)
    val avenue = if (isDarkTheme) Color(0xFF746855) else Color(0xFFF9D78C)
    val route = MaterialTheme.colorScheme.primary
    Box(modifier.background(background), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / 6f
            for (i in -6..12) {
                drawLine(street, Offset(i * step, 0f), Offset(i * step + size.height * 0.4f, size.height), strokeWidth = 10f)
            }
            for (i in 0..12) {
                drawLine(street, Offset(0f, i * step), Offset(size.width, i * step - size.width * 0.3f), strokeWidth = 8f)
            }
            drawLine(avenue, Offset(0f, size.height * 0.75f), Offset(size.width, size.height * 0.25f), strokeWidth = 26f)
            drawLine(route, Offset(size.width * 0.2f, size.height * 0.85f), Offset(size.width * 0.5f, size.height * 0.55f), strokeWidth = 14f)
            drawLine(route, Offset(size.width * 0.5f, size.height * 0.55f), Offset(size.width * 0.55f, size.height * 0.42f), strokeWidth = 14f)
        }
        Text(
            "Google Maps (vista previa)",
            color = if (isDarkTheme) Color(0xFF9AA4B5) else Color(0xFF5B6578),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(name = "Mapa (placeholder)", showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun MapPlaceholderPreview() {
    LaRutaTheme(darkTheme = true) { MapPreviewPlaceholder(isDarkTheme = true, Modifier.fillMaxSize()) }
}
