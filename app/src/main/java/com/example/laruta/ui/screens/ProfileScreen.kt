package com.example.laruta.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.laruta.model.PermissionStatus
import com.example.laruta.model.SensorInfo
import com.example.laruta.model.ThemeMode
import com.example.laruta.model.UserSettings
import com.example.laruta.sensors.AmbientLightState
import com.example.laruta.ui.components.ScreenTopBar
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.AppPermissions
import com.example.laruta.util.initialsOf
import com.example.laruta.util.openAppSettings
import com.example.laruta.viewmodel.TrackingViewModel
import java.util.Locale

/** Pantalla "Yo" con estado. */
@Composable
fun ProfileRoute(viewModel: TrackingViewModel, lightState: AmbientLightState, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Re-lee los permisos cada vez que la pantalla vuelve al frente (p. ej. al regresar de Ajustes)
    var refresh by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val permissions = remember(refresh) { AppPermissions.status(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refresh++
        viewModel.onPermissionsChanged()
    }

    ProfileScreen(
        settings = settings,
        lux = lightState.lux,
        isDarkTheme = isDarkTheme,
        sensors = viewModel.sensorInfo,
        permissions = permissions,
        poiCount = state.pois.size,
        onNameChange = viewModel::updateUserName,
        onStrideChange = viewModel::updateStride,
        onThemeModeChange = viewModel::updateThemeMode,
        onRequestPermissions = { launcher.launch(AppPermissions.START_TRACKING) },
        onOpenSettings = { context.openAppSettings() },
        onClearCache = viewModel::clearPois
    )
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

/** Fila "✔ Sensor" / "✖ Sensor". */
@Composable
private fun StatusRow(label: String, ok: Boolean, detail: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (detail != null) {
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val ThemeMode.icon: ImageVector
    get() = when (this) {
        ThemeMode.AUTO -> Icons.Filled.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Filled.LightMode
        ThemeMode.DARK -> Icons.Filled.DarkMode
    }

/** Pantalla "Yo" (SIN estado): perfil, zancada, tema por sensor de luz, sensores y permisos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settings: UserSettings,
    lux: Float,
    isDarkTheme: Boolean,
    sensors: SensorInfo,
    permissions: PermissionStatus,
    poiCount: Int,
    onNameChange: (String) -> Unit,
    onStrideChange: (Float) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        ScreenTopBar(title = "Yo", subtitle = "Perfil y configuración")

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ----- Perfil -----
            ProfileSection("Perfil") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initialsOf(settings.userName), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = settings.userName,
                        onValueChange = onNameChange,
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ----- Zancada -----
            ProfileSection("Longitud de zancada") {
                Text(
                    "Distancia = pasos × zancada. Ajústala a tu paso: ${String.format(Locale.US, "%.2f", settings.strideMeters)} m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.strideMeters,
                    onValueChange = { onStrideChange((it * 100).toInt() / 100f) },
                    valueRange = 0.40f..1.20f
                )
            }

            // ----- Tema por sensor de luz -----
            ProfileSection("Apariencia") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            icon = { Icon(mode.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text(if (mode == ThemeMode.AUTO) "Auto" else mode.label, maxLines = 1) }
                        )
                    }
                }
                val reading = if (lux >= 0) "${lux.toInt()} lx" else "sin lectura"
                Text(
                    "Sensor de luz: $reading · modo ${if (isDarkTheme) "oscuro" else "claro"}. " +
                        "En Auto cambia a oscuro con menos de ${AmbientLightState.DARK_BELOW_LUX.toInt()} lx " +
                        "y vuelve a claro con más de ${AmbientLightState.LIGHT_ABOVE_LUX.toInt()} lx.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ----- Sensores -----
            ProfileSection("Sensores del dispositivo") {
                StatusRow("Contador de pasos", sensors.stepCounter, "TYPE_STEP_COUNTER")
                StatusRow("Detector de pasos", sensors.stepDetector, "TYPE_STEP_DETECTOR")
                StatusRow("Acelerómetro", sensors.accelerometer, "respaldo")
                StatusRow("Sensor de luz", sensors.light, "TYPE_LIGHT")
                StatusRow("GPS", sensors.gps)
                StatusRow("Cámara", sensors.camera)
            }

            // ----- Permisos -----
            ProfileSection("Permisos") {
                StatusRow("Ubicación (GPS)", permissions.location)
                StatusRow("Cámara", permissions.camera)
                StatusRow("Actividad física (pasos)", permissions.activityRecognition)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!permissions.allGranted) {
                        OutlinedButton(onClick = onRequestPermissions) { Text("Conceder") }
                    }
                    TextButton(onClick = onOpenSettings) { Text("Ajustes de la app") }
                }
            }

            // ----- Caché -----
            ProfileSection("Almacenamiento temporal") {
                Text(
                    "$poiCount fotos guardadas en la caché de la app. Android puede borrarlas si necesita espacio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onClearCache, enabled = poiCount > 0) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Borrar fotos de la caché")
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Previews

@Preview(name = "Yo · oscuro", showSystemUi = true)
@Composable
private fun ProfileDarkPreview() {
    LaRutaTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreen(
                settings = PreviewData.settings, lux = 8f, isDarkTheme = true,
                sensors = SensorInfo(stepCounter = true, stepDetector = true, accelerometer = true, light = true, gps = true, camera = true),
                permissions = PermissionStatus(location = true, camera = true, activityRecognition = false),
                poiCount = 3,
                onNameChange = {}, onStrideChange = {}, onThemeModeChange = {},
                onRequestPermissions = {}, onOpenSettings = {}, onClearCache = {}
            )
        }
    }
}

@Preview(name = "Yo · claro", showSystemUi = true)
@Composable
private fun ProfileLightPreview() {
    LaRutaTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileScreen(
                settings = PreviewData.settings, lux = 320f, isDarkTheme = false,
                sensors = SensorInfo(accelerometer = true, light = true, gps = true, camera = true),
                permissions = PermissionStatus(),
                poiCount = 0,
                onNameChange = {}, onStrideChange = {}, onThemeModeChange = {},
                onRequestPermissions = {}, onOpenSettings = {}, onClearCache = {}
            )
        }
    }
}
