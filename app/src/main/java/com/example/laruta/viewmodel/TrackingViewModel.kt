package com.example.laruta.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laruta.data.GeocodingRepository
import com.example.laruta.data.PoiCache
import com.example.laruta.data.SettingsStore
import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.SensorInfo
import com.example.laruta.model.ThemeMode
import com.example.laruta.model.TrackingState
import com.example.laruta.model.TrackingStatus
import com.example.laruta.model.TravelMode
import com.example.laruta.model.UiAction
import com.example.laruta.model.UiMessage
import com.example.laruta.model.UserSettings
import com.example.laruta.sensors.StepTracker
import com.example.laruta.util.AppPermissions
import com.example.laruta.util.formatDistance
import com.example.laruta.util.formatDuration
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Cerebro del recorrido. Vive mientras la Activity exista (sobrevive a rotaciones y
 * a abrir la cámara), así que aquí viven el GPS, el sensor de pasos, el cronómetro y las fotos.
 *
 * La UI solo LEE [state] / [settings] y llama funciones: nunca toca los sensores directamente.
 */
class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    // ---------- Estado expuesto a la UI ----------
    private val poiCache = PoiCache(application)
    private val settingsStore = SettingsStore(application)
    private val geocoding = GeocodingRepository(application)

    private val _state = MutableStateFlow(TrackingState(pois = poiCache.load()))
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    /** Punto al que la pantalla del mapa debe mover la cámara (p. ej. "Ver en el mapa" desde el dashboard). */
    private val _focusTarget = MutableStateFlow<LatLng?>(null)
    val focusTarget: StateFlow<LatLng?> = _focusTarget.asStateFlow()

    /** Sensores disponibles en este teléfono. */
    val sensorInfo: SensorInfo = detectSensors(application)

    // ---------- Hardware ----------
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private val stepTracker = StepTracker(application) { steps ->
        _state.update { it.copy(steps = steps) }
    }

    private var locationUpdatesActive = false
    private var appInForeground = true
    private var timerJob: Job? = null
    private var trackingStartElapsed = 0L
    private var trackingStartWallClock = 0L

    // Foto pendiente: la cámara escribe aquí y luego nos devuelve el control
    private var pendingPhotoFile: File? = null
    private var pendingPhotoTitle: String = ""
    private val cacheMutex = Mutex()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::onNewLocation)
        }
    }

    init {
        // Si el permiso ya estaba concedido, mostramos la ubicación en el mapa desde el inicio
        onPermissionsChanged()
    }

    // =====================================================================
    // Ciclo de vida de la app (lo llama la UI en ON_START / ON_STOP)
    // =====================================================================

    fun onAppForeground() {
        appInForeground = true
        onPermissionsChanged()
    }

    /** En segundo plano dejamos de usar el GPS, salvo que haya un recorrido en curso. */
    fun onAppBackground() {
        appInForeground = false
        if (!_state.value.isTracking) stopLocationUpdates()
    }

    /** Se llama después de que el usuario responde un diálogo de permisos. */
    fun onPermissionsChanged() {
        if (hasLocationPermission() && (appInForeground || _state.value.isTracking)) {
            startLocationUpdates()
        }
    }

    // =====================================================================
    // Recorrido
    // =====================================================================

    fun startTracking() {
        if (!hasLocationPermission()) {
            showMessage("Necesitamos el permiso de ubicación para registrar el recorrido")
            return
        }
        if (_state.value.isTracking) return

        trackingStartElapsed = SystemClock.elapsedRealtime()
        trackingStartWallClock = System.currentTimeMillis()
        val stepSource = stepTracker.start() // Empieza a contar pasos con el sensor de movimiento

        _state.update {
            it.copy(
                status = TrackingStatus.TRACKING,
                steps = 0,
                stepSource = stepSource,
                gpsDistanceMeters = 0.0,
                elapsedMillis = 0L,
                route = listOfNotNull(it.currentLocation) // La ruta empieza donde estás
            )
        }

        startLocationUpdates()
        startTimer()
        showMessage("Recorrido iniciado · ${stepSource.label}")
    }

    fun stopTracking() {
        if (!_state.value.isTracking) return
        stepTracker.stop()
        timerJob?.cancel()
        _state.update { it.copy(status = TrackingStatus.FINISHED) }
        if (!appInForeground) stopLocationUpdates()

        val s = _state.value
        val distance = formatDistance(s.mainDistanceMeters(_settings.value.strideMeters))
        showMessage("Recorrido finalizado: ${s.steps} pasos · $distance · ${formatDuration(s.elapsedMillis)}")
    }

    /** Alterna persona caminando → corriendo → moto (cambia el ícono del mapa). */
    fun cycleTravelMode() {
        _state.update { it.copy(mode = it.mode.next()) }
        showMessage("Modo: ${_state.value.mode.label}")
    }

    fun setTravelMode(mode: TravelMode) = _state.update { it.copy(mode = mode) }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(elapsedMillis = SystemClock.elapsedRealtime() - trackingStartElapsed) }
                delay(1_000)
            }
        }
    }

    // =====================================================================
    // GPS (Fused Location Provider)
    // =====================================================================

    private fun hasLocationPermission() = AppPermissions.hasLocation(getApplication())

    @SuppressLint("MissingPermission") // Se verifica con hasLocationPermission()
    private fun startLocationUpdates() {
        if (locationUpdatesActive || !hasLocationPermission()) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_MS)
            .build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        locationUpdatesActive = true

        // La última ubicación conocida llega al instante: el mapa no espera el primer "fix"
        fusedClient.lastLocation.addOnSuccessListener { it?.let(::onNewLocation) }
    }

    private fun stopLocationUpdates() {
        if (!locationUpdatesActive) return
        fusedClient.removeLocationUpdates(locationCallback)
        locationUpdatesActive = false
    }

    /** Cada lectura del GPS actualiza la posición y, si hay recorrido, la ruta (PolyLine). */
    private fun onNewLocation(location: Location) {
        val point = LatLng(location.latitude, location.longitude)

        _state.update { s ->
            var route = s.route
            var distance = s.gpsDistanceMeters

            val isFresh = location.time >= trackingStartWallClock - 5_000 // ignora lecturas viejas en caché
            val isPrecise = location.accuracy <= MAX_ACCURACY_FOR_ROUTE_M

            if (s.isTracking && isFresh && isPrecise) {
                val last = route.lastOrNull()
                if (last == null) {
                    route = route + point
                } else {
                    val meters = distanceBetween(last, point)
                    if (meters >= MIN_SEGMENT_M) { // filtra el "temblor" del GPS estando quieto
                        route = route + point
                        distance += meters
                    }
                }
            }

            s.copy(
                currentLocation = point,
                accuracyMeters = location.accuracy,
                bearing = if (location.hasBearing()) location.bearing else s.bearing,
                speedMps = if (location.hasSpeed()) location.speed else 0f,
                route = route,
                gpsDistanceMeters = distance
            )
        }
    }

    private fun distanceBetween(a: LatLng, b: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
        return result[0].toDouble()
    }

    // =====================================================================
    // Cámara y puntos de interés
    // =====================================================================

    /**
     * Crea el archivo vacío en la CACHÉ y devuelve su Uri (FileProvider) para la cámara.
     * @param title nombre opcional que el usuario escribió en el modal.
     */
    fun preparePhoto(title: String): Uri {
        val app = getApplication<Application>()
        val file = poiCache.newPhotoFile()
        pendingPhotoFile = file
        pendingPhotoTitle = title.trim()
        return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
    }

    /**
     * La cámara terminó. Si hay foto: registra coordenadas + fecha/hora, crea el punto de interés
     * (que aparece como marcador en el mapa y en el dashboard) y busca la dirección con Google.
     */
    fun onPhotoResult(success: Boolean) {
        val file = pendingPhotoFile ?: return
        pendingPhotoFile = null

        if (!success || !file.exists() || file.length() == 0L) {
            file.delete()
            showMessage("No se tomó ninguna foto")
            return
        }

        val capturedAt = System.currentTimeMillis()
        viewModelScope.launch {
            val fix = locationForPhoto()
            if (fix == null) {
                file.delete()
                showMessage("No pudimos obtener tu ubicación. Activa el GPS e inténtalo de nuevo")
                return@launch
            }

            val number = _state.value.pois.size + 1
            val poi = PointOfInterest(
                id = capturedAt,
                title = pendingPhotoTitle.ifBlank { "Punto $number" },
                photoPath = file.absolutePath,
                latitude = fix.latLng.latitude,
                longitude = fix.latLng.longitude,
                timestamp = capturedAt,
                accuracyMeters = fix.accuracy
            )
            _state.update { it.copy(pois = it.pois + poi) }
            persistPois()
            showMessage("📍 ${poi.title} guardado", actionLabel = "Ver", action = UiAction.OPEN_POIS)

            // Dirección legible con la Geocoding API de Google Maps (en segundo plano)
            geocoding.reverseGeocode(poi.latitude, poi.longitude)?.let { address ->
                _state.update { s ->
                    s.copy(pois = s.pois.map { if (it.id == poi.id) it.copy(address = address) else it })
                }
                persistPois()
            }
        }
    }

    private data class Fix(val latLng: LatLng, val accuracy: Float?)

    /** Ubicación lo más fresca posible para la foto: GPS actual → última del recorrido → última conocida. */
    @SuppressLint("MissingPermission")
    private suspend fun locationForPhoto(): Fix? {
        if (!hasLocationPermission()) return null

        val tokenSource = CancellationTokenSource()
        val fresh = try {
            withTimeoutOrNull(PHOTO_LOCATION_TIMEOUT_MS) {
                runCatching {
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token).await()
                }.getOrNull()
            }
        } finally {
            tokenSource.cancel()
        }
        if (fresh != null) return Fix(LatLng(fresh.latitude, fresh.longitude), fresh.accuracy)

        _state.value.currentLocation?.let { return Fix(it, _state.value.accuracyMeters) }

        val last = runCatching { fusedClient.lastLocation.await() }.getOrNull()
        return last?.let { Fix(LatLng(it.latitude, it.longitude), it.accuracy) }
    }

    fun deletePoi(poi: PointOfInterest) {
        poiCache.delete(poi)
        _state.update { s -> s.copy(pois = s.pois.filterNot { it.id == poi.id }) }
        persistPois()
        showMessage("Punto eliminado")
    }

    fun clearPois() {
        poiCache.clear()
        _state.update { it.copy(pois = emptyList()) }
        showMessage("Se borraron las fotos de la caché")
    }

    private fun persistPois() {
        val snapshot = _state.value.pois
        viewModelScope.launch {
            cacheMutex.withLock {
                withContext(Dispatchers.IO) { poiCache.save(snapshot) }
            }
        }
    }

    // =====================================================================
    // Mapa: "Ver en el mapa"
    // =====================================================================

    fun focusOn(latLng: LatLng) {
        _focusTarget.value = latLng
    }

    fun consumeFocus() {
        _focusTarget.value = null
    }

    // =====================================================================
    // Preferencias (pantalla "Yo")
    // =====================================================================

    fun updateUserName(name: String) = updateSettings { it.copy(userName = name) }

    fun updateStride(meters: Float) = updateSettings { it.copy(strideMeters = meters) }

    fun updateThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    private fun updateSettings(change: (UserSettings) -> UserSettings) {
        _settings.update(change)
        settingsStore.save(_settings.value)
    }

    // =====================================================================
    // Mensajes
    // =====================================================================

    fun showMessage(text: String, actionLabel: String? = null, action: UiAction? = null) {
        _messages.tryEmit(UiMessage(text, actionLabel, action))
    }

    override fun onCleared() {
        stepTracker.stop()
        stopLocationUpdates()
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val LOCATION_INTERVAL_MS = 2_000L
        const val LOCATION_FASTEST_MS = 1_000L
        const val MIN_SEGMENT_M = 3.0             // movimiento mínimo para agregar un punto a la ruta
        const val MAX_ACCURACY_FOR_ROUTE_M = 35f  // lecturas peores se muestran, pero no se suman a la ruta
        const val PHOTO_LOCATION_TIMEOUT_MS = 6_000L

        fun detectSensors(context: Context): SensorInfo {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val pm = context.packageManager
            return SensorInfo(
                stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null,
                stepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null,
                accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
                light = sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null,
                gps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
                camera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            )
        }
    }
}
