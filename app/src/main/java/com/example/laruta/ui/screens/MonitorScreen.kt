package com.example.laruta.ui.screens

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.TrackingState
import com.example.laruta.model.TrackingStatus
import com.example.laruta.model.UiAction
import com.example.laruta.model.UserSettings
import com.example.laruta.ui.components.ActionRail
import com.example.laruta.ui.components.AddPoiBottomSheet
import com.example.laruta.ui.components.MonitorTab
import com.example.laruta.ui.components.MonitorTopBar
import com.example.laruta.ui.components.PermissionDialog
import com.example.laruta.ui.components.PoiDetailDialog
import com.example.laruta.ui.components.RecenterButton
import com.example.laruta.ui.components.RecordingChip
import com.example.laruta.ui.components.RouteControls
import com.example.laruta.ui.components.StartTrackingButton
import com.example.laruta.ui.components.TrackingStatsCard
import com.example.laruta.ui.components.ZoomControls
import com.example.laruta.ui.map.BOGOTA
import com.example.laruta.ui.map.MapPreviewPlaceholder
import com.example.laruta.ui.map.MapTypeOption
import com.example.laruta.ui.map.TrackingMap
import com.example.laruta.ui.preview.PreviewData
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.util.AppPermissions
import com.example.laruta.util.findActivity
import com.example.laruta.util.openAppSettings
import com.example.laruta.viewmodel.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Pantalla MONITOR con estado: conecta el ViewModel con permisos, cámara y la cámara del mapa.
 * El diseño visual está en [MonitorScreen] (sin estado, con @Preview).
 */
@Composable
fun MonitorRoute(
    viewModel: TrackingViewModel,
    isDarkTheme: Boolean,
    onOpenPois: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val focusTarget by viewModel.focusTarget.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var selectedTab by rememberSaveable { mutableStateOf(MonitorTab.MAP) }
    var mapType by rememberSaveable { mutableStateOf(MapTypeOption.NORMAL) }
    var followUser by rememberSaveable { mutableStateOf(true) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var newPoiTitle by rememberSaveable { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var selectedPoi by remember { mutableStateOf<PointOfInterest?>(null) }

    // ---------------- Cámara del mapa ----------------
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.currentLocation ?: BOGOTA, if (state.currentLocation != null) 17f else 11f)
    }

    // Si el usuario arrastra el mapa, dejamos de seguirlo hasta que pulse "centrar"
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving &&
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
        ) followUser = false
    }

    // Seguir al usuario con cada lectura del GPS
    LaunchedEffect(state.currentLocation, followUser) {
        val location = state.currentLocation ?: return@LaunchedEffect
        if (!followUser) return@LaunchedEffect
        val zoom = cameraPositionState.position.zoom.coerceAtLeast(16f)
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(location, zoom), 700)
    }

    // "Ver en el mapa" desde el dashboard de Puntos de Interés
    LaunchedEffect(focusTarget) {
        val target = focusTarget ?: return@LaunchedEffect
        selectedTab = MonitorTab.MAP
        followUser = false
        viewModel.consumeFocus()
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 18f), 800)
    }

    // ---------------- Cámara de fotos ----------------
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onPhotoResult(saved)
    }

    fun launchCamera() {
        try {
            takePicture.launch(viewModel.preparePhoto(newPoiTitle))
            newPoiTitle = ""
            showAddSheet = false
        } catch (e: ActivityNotFoundException) {
            viewModel.onPhotoResult(false)
            viewModel.showMessage("No se encontró una app de cámara en el dispositivo")
        }
    }

    val photoPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionsChanged()
        if (AppPermissions.hasCamera(context) && AppPermissions.hasLocation(context)) {
            launchCamera()
        } else {
            viewModel.showMessage(
                "Para tomar fotos georreferenciadas se necesitan cámara y ubicación",
                actionLabel = "Ajustes",
                action = UiAction.OPEN_SETTINGS
            )
        }
    }

    fun openCamera() {
        if (AppPermissions.hasCamera(context) && AppPermissions.hasLocation(context)) launchCamera()
        else photoPermissions.launch(AppPermissions.PHOTO)
    }

    // ---------------- Permisos al pulsar "Iniciar recorrido" ----------------
    val startPermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onPermissionsChanged()
        if (AppPermissions.hasLocation(context)) {
            followUser = true
            viewModel.startTracking()
            if (result[android.Manifest.permission.CAMERA] == false) {
                viewModel.showMessage("Sin permiso de cámara no podrás agregar fotos")
            }
        } else {
            // Si el sistema ya no muestra el diálogo, el usuario marcó "No volver a preguntar"
            val activity = context.findActivity()
            permanentlyDenied = activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity, android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            showPermissionDialog = true
        }
    }

    fun onStartClick() {
        if (AppPermissions.status(context).allGranted) {
            followUser = true
            viewModel.startTracking()
        } else {
            startPermissions.launch(AppPermissions.START_TRACKING) // GPS + Cámara (+ actividad física)
        }
    }

    // ---------------- Acciones del mapa ----------------
    fun animateTo(update: com.google.android.gms.maps.CameraUpdate) {
        scope.launch { runCatching { cameraPositionState.animate(update, 500) } }
    }

    fun showWholeRoute() {
        val points = state.route + state.pois.map { it.latLng }
        if (points.size < 2) {
            viewModel.showMessage("Aún no hay un recorrido para mostrar")
            return
        }
        followUser = false
        selectedTab = MonitorTab.MAP
        val bounds = LatLngBounds.builder().apply { points.forEach { include(it) } }.build()
        val padding = with(density) { 96.dp.roundToPx() }
        animateTo(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    MonitorScreen(
        state = state,
        settings = settings,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        mapType = mapType,
        onMapTypeClick = {
            mapType = mapType.next()
            viewModel.showMessage("Vista: ${mapType.label}")
        },
        isFollowing = followUser,
        onStart = ::onStartClick,
        onStop = viewModel::stopTracking,
        onAddClick = { showAddSheet = true },
        onShowRoute = ::showWholeRoute,
        onCycleMode = viewModel::cycleTravelMode,
        onZoomIn = { animateTo(CameraUpdateFactory.zoomIn()) },
        onZoomOut = { animateTo(CameraUpdateFactory.zoomOut()) },
        onRecenter = {
            followUser = true
            state.currentLocation?.let { animateTo(CameraUpdateFactory.newLatLngZoom(it, 17f)) }
                ?: viewModel.showMessage("Inicia un recorrido para obtener tu ubicación")
        },
        onPoiClick = { selectedPoi = it },
        onOpenPois = onOpenPois,
        map = {
            TrackingMap(
                state = state,
                isDarkTheme = isDarkTheme,
                mapType = mapType,
                cameraPositionState = cameraPositionState,
                onPoiClick = { selectedPoi = it },
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 150.dp)
            )
        }
    )

    // ---------------- Modales y diálogos ----------------
    if (showAddSheet) {
        AddPoiBottomSheet(
            location = state.currentLocation,
            accuracyMeters = state.accuracyMeters,
            isTracking = state.isTracking,
            title = newPoiTitle,
            onTitleChange = { newPoiTitle = it },
            onOpenCamera = ::openCamera,
            onDismiss = { showAddSheet = false }
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            permanentlyDenied = permanentlyDenied,
            onConfirm = {
                showPermissionDialog = false
                if (permanentlyDenied) context.openAppSettings()
                else startPermissions.launch(AppPermissions.START_TRACKING)
            },
            onDismiss = { showPermissionDialog = false }
        )
    }

    selectedPoi?.let { poi ->
        PoiDetailDialog(
            poi = poi,
            onDismiss = { selectedPoi = null },
            onShowOnMap = {
                selectedPoi = null
                viewModel.focusOn(poi.latLng)
            },
            onDelete = {
                selectedPoi = null
                viewModel.deletePoi(poi)
            }
        )
    }
}

/**
 * Diseño de la pantalla Monitor (SIN estado): barra superior con pestañas Lista/Mapa,
 * mapa con controles flotantes como la app de referencia, o la lista de resumen.
 *
 * @param map el mapa real se pasa como slot; en los @Preview se usa un fondo de ejemplo.
 */
@Composable
fun MonitorScreen(
    state: TrackingState,
    settings: UserSettings,
    selectedTab: MonitorTab,
    onTabSelected: (MonitorTab) -> Unit,
    mapType: MapTypeOption,
    onMapTypeClick: () -> Unit,
    isFollowing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onAddClick: () -> Unit,
    onShowRoute: () -> Unit,
    onCycleMode: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenter: () -> Unit,
    onPoiClick: (PointOfInterest) -> Unit,
    onOpenPois: () -> Unit,
    map: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        MonitorTopBar(
            title = settings.userName,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            actionIcon = mapType.next().icon,
            actionDescription = "Cambiar a vista ${mapType.next().label}",
            onActionClick = onMapTypeClick
        )

        when (selectedTab) {
            MonitorTab.MAP -> Box(Modifier.fillMaxSize()) {
                map()

                RecordingChip(
                    status = state.status,
                    elapsedMillis = state.elapsedMillis,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )

                RouteControls(
                    status = state.status,
                    onStart = onStart,
                    onStop = onStop,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )

                ActionRail(
                    mode = state.mode,
                    onAddClick = onAddClick,
                    onShowRoute = onShowRoute,
                    onCycleMode = onCycleMode,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        ZoomControls(onZoomIn = onZoomIn, onZoomOut = onZoomOut)
                        if (state.status != TrackingStatus.TRACKING) {
                            StartTrackingButton(status = state.status, onClick = onStart)
                        }
                        RecenterButton(isFollowing = isFollowing, onClick = onRecenter)
                    }
                    TrackingStatsCard(state = state, strideMeters = settings.strideMeters, modifier = Modifier.fillMaxWidth())
                }
            }

            MonitorTab.LIST -> RouteSummaryList(
                state = state,
                settings = settings,
                onPoiClick = onPoiClick,
                onOpenPois = onOpenPois,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ------------------------------------------------------------------ Previews

@Composable
private fun MonitorPreviewContent(state: TrackingState, dark: Boolean, tab: MonitorTab = MonitorTab.MAP) {
    LaRutaTheme(darkTheme = dark) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonitorScreen(
                state = state,
                settings = PreviewData.settings,
                selectedTab = tab,
                onTabSelected = {},
                mapType = MapTypeOption.NORMAL,
                onMapTypeClick = {},
                isFollowing = true,
                onStart = {}, onStop = {}, onAddClick = {}, onShowRoute = {}, onCycleMode = {},
                onZoomIn = {}, onZoomOut = {}, onRecenter = {}, onPoiClick = {}, onOpenPois = {},
                map = { MapPreviewPlaceholder(isDarkTheme = dark, modifier = Modifier.fillMaxSize()) }
            )
        }
    }
}

@Preview(name = "Monitor · grabando · oscuro", showSystemUi = true)
@Composable
private fun MonitorTrackingDarkPreview() = MonitorPreviewContent(PreviewData.trackingState, dark = true)

@Preview(name = "Monitor · sin iniciar · claro", showSystemUi = true)
@Composable
private fun MonitorIdleLightPreview() = MonitorPreviewContent(PreviewData.idleState, dark = false)

@Preview(name = "Monitor · pestaña Lista", showSystemUi = true)
@Composable
private fun MonitorListPreview() = MonitorPreviewContent(PreviewData.trackingState, dark = true, tab = MonitorTab.LIST)
