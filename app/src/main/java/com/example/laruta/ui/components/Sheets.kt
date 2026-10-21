package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.formatCoordinates
import com.google.android.gms.maps.model.LatLng

@Composable
fun AddPoiSheetContent(
    location: LatLng?,
    accuracyMeters: Float?,
    isTracking: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Agregar punto de interés", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "La foto se guarda con tu ubicación, fecha y hora",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoLine(
                    Icons.Filled.GpsFixed,
                    location?.let {
                        "Ubicación: ${formatCoordinates(it.latitude, it.longitude)}" +
                            (accuracyMeters?.let { a -> " (±${a.toInt()} m)" } ?: "")
                    } ?: "Ubicación: se tomará al capturar la foto"
                )
                InfoLine(Icons.Filled.Schedule, "Fecha y hora: se registran al tomar la foto")
                if (!isTracking) {
                    InfoLine(Icons.Filled.Info, "No hay un recorrido activo: el punto se guarda igual")
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Nombre (opcional)") },
            placeholder = { Text("Ej: Mural, mirador, cafetería…") },
            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onOpenCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Abrir cámara", fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPoiBottomSheet(
    location: LatLng?,
    accuracyMeters: Float?,
    isTracking: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    onOpenCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AddPoiSheetContent(
            location = location,
            accuracyMeters = accuracyMeters,
            isTracking = isTracking,
            title = title,
            onTitleChange = onTitleChange,
            onOpenCamera = onOpenCamera,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
fun PermissionDialog(
    permanentlyDenied: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Permisos necesarios") },
        text = {
            Text(
                if (permanentlyDenied)
                    "Negaste el permiso de ubicación. Para registrar tu recorrido actívalo en Ajustes → Permisos → Ubicación."
                else
                    "LaRuta usa el GPS para dibujar tu recorrido en el mapa, la cámara para tus puntos de interés " +
                        "y el sensor de actividad física para contar tus pasos."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(if (permanentlyDenied) "Abrir ajustes" else "Conceder permisos") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ahora no") } }
    )
}

@Composable
fun ConfirmClearDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Borrar todos los puntos?") },
        text = { Text("Se eliminarán las fotos y su información de la caché. No se puede deshacer.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Preview(name = "Modal agregar · oscuro", showBackground = true, backgroundColor = 0xFF1B2230)
@Composable
private fun AddPoiSheetDarkPreview() {
    LaRutaTheme(darkTheme = true) {
        Surface {
            AddPoiSheetContent(
                location = LatLng(4.65844, -74.09371),
                accuracyMeters = 5f,
                isTracking = true,
                title = "",
                onTitleChange = {},
                onOpenCamera = {},
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(name = "Modal agregar · claro", showBackground = true)
@Composable
private fun AddPoiSheetLightPreview() {
    LaRutaTheme(darkTheme = false) {
        Surface {
            AddPoiSheetContent(
                location = null,
                accuracyMeters = null,
                isTracking = false,
                title = "Mirador",
                onTitleChange = {},
                onOpenCamera = {},
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(name = "Diálogo de permisos", showBackground = true)
@Composable
private fun PermissionDialogPreview() {
    LaRutaTheme(darkTheme = true) {
        PermissionDialog(permanentlyDenied = false, onConfirm = {}, onDismiss = {})
    }
}
