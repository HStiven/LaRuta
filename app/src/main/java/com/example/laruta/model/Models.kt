package com.example.laruta.model

import com.google.android.gms.maps.model.LatLng

/** Medio con el que se hace el recorrido: cambia el ícono del marcador en el mapa. */
enum class TravelMode(val label: String) {
    WALK("Caminar"),
    RUN("Correr"),
    MOTO("Moto");

    fun next(): TravelMode = entries[(ordinal + 1) % entries.size]
}

enum class TrackingStatus { IDLE, TRACKING, FINISHED }

enum class ThemeMode(val label: String) {
    AUTO("Sensor de luz"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class StepSource(val label: String) {
    NONE("Sin sensor disponible"),
    STEP_COUNTER("Contador de pasos (TYPE_STEP_COUNTER)"),
    STEP_DETECTOR("Detector de pasos (TYPE_STEP_DETECTOR)"),
    ACCELEROMETER("Acelerómetro (TYPE_ACCELEROMETER)")
}

data class PointOfInterest(
    val id: Long,
    val title: String,
    val photoPath: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracyMeters: Float?,
    val address: String? = null
) {
    val latLng: LatLng get() = LatLng(latitude, longitude)
}

/** Todo lo que la interfaz necesita saber del recorrido actual. */
data class TrackingState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val mode: TravelMode = TravelMode.WALK,
    val steps: Int = 0,
    val stepSource: StepSource = StepSource.NONE,
    val gpsDistanceMeters: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val route: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val bearing: Float = 0f,
    val speedMps: Float = 0f,
    val accuracyMeters: Float? = null,
    val pois: List<PointOfInterest> = emptyList()
) {
    val isTracking: Boolean get() = status == TrackingStatus.TRACKING

    /** Distancia calculada con el sensor de movimiento: pasos × longitud de zancada. */
    fun sensorDistanceMeters(strideMeters: Float): Double {
        val factor = if (mode == TravelMode.RUN) 1.35 else 1.0 // Al correr la zancada es más larga
        return steps * strideMeters * factor
    }

    /** En moto los pasos no tienen sentido: se usa la distancia del GPS. */
    fun mainDistanceMeters(strideMeters: Float): Double =
        if (mode == TravelMode.MOTO) gpsDistanceMeters else sensorDistanceMeters(strideMeters)
}

/** Preferencias del usuario (pantalla Perfil). */
data class UserSettings(
    val userName: String = "Explorador",
    val strideMeters: Float = 0.75f,
    val themeMode: ThemeMode = ThemeMode.AUTO
)
