package com.example.laruta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.TrackingState
import com.example.laruta.ui.components.ConfirmClearDialog
import com.example.laruta.ui.components.PoiCard
import com.example.laruta.ui.components.PoiDetailDialog
import com.example.laruta.ui.components.ScreenTopBar
import com.example.laruta.ui.components.TrackingSummaryGrid
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.viewmodel.TrackingViewModel

/** "Puntos de Interés" con estado: lee el ViewModel y maneja los diálogos. */
@Composable
fun PoiDashboardRoute(viewModel: TrackingViewModel, onShowOnMap: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<PointOfInterest?>(null) }
    var confirmClear by remember { mutableStateOf(false) }

    PoiDashboardScreen(
        state = state,
        strideMeters = settings.strideMeters,
        onPoiClick = { selected = it },
        onClearAll = { confirmClear = true }
    )

    selected?.let { poi ->
        PoiDetailDialog(
            poi = poi,
            onDismiss = { selected = null },
            onShowOnMap = {
                selected = null
                viewModel.focusOn(poi.latLng)
                onShowOnMap()
            },
            onDelete = {
                selected = null
                viewModel.deletePoi(poi)
            }
        )
    }

    if (confirmClear) {
        ConfirmClearDialog(
            onConfirm = {
                confirmClear = false
                viewModel.clearPois()
            },
            onDismiss = { confirmClear = false }
        )
    }
}

/**
 * Dashboard "Puntos de Interés" (SIN estado): resumen del recorrido en mosaicos
 * y la galería de todas las fotos con su información.
 */
@Composable
fun PoiDashboardScreen(
    state: TrackingState,
    strideMeters: Float,
    onPoiClick: (PointOfInterest) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "Puntos de Interés",
            subtitle = "${state.pois.size} fotos guardadas en caché",
            actions = {
                if (state.pois.isNotEmpty()) {
                    IconButton(onClick = onClearAll) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Borrar todo")
                    }
                }
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TrackingSummaryGrid(state, strideMeters)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Galería del recorrido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.pois.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EmptyPois() }
            } else {
                // Las más recientes primero; el número (#) respeta el orden en que se tomaron
                val numbered = state.pois.mapIndexed { index, poi -> index + 1 to poi }.asReversed()
                items(numbered, key = { it.second.id }) { (number, poi) ->
                    PoiCard(poi = poi, number = number, onClick = { onPoiClick(poi) })
                }
            }
        }
    }
}

@Composable
private fun EmptyPois() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.AddAPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(48.dp)
        )
        Text("Aún no hay puntos de interés", style = MaterialTheme.typography.titleMedium)
        Text(
            "En el mapa toca \"Agregar\" y toma una foto: quedará aquí con su ubicación, fecha y hora.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ------------------------------------------------------------------ Previews

@Preview(name = "Dashboard · oscuro", showSystemUi = true)
@Composable
private fun PoiDashboardDarkPreview() {
    LaRutaTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PoiDashboardScreen(PreviewData.trackingState, 0.75f, onPoiClick = {}, onClearAll = {})
        }
    }
}

@Preview(name = "Dashboard · vacío · claro", showSystemUi = true)
@Composable
private fun PoiDashboardEmptyPreview() {
    LaRutaTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PoiDashboardScreen(PreviewData.idleState, 0.75f, onPoiClick = {}, onClearAll = {})
        }
    }
}
