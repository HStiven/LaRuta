package com.example.laruta.model

/** Acción opcional de un mensaje tipo Snackbar. */
enum class UiAction { OPEN_POIS, OPEN_SETTINGS }

/** Mensaje corto que el ViewModel envía a la interfaz (se muestra como Snackbar). */
data class UiMessage(
    val text: String,
    val actionLabel: String? = null,
    val action: UiAction? = null,
    val id: Long = System.nanoTime()
)

/** Sensores y hardware disponibles en el dispositivo (se muestran en la pantalla "Yo"). */
data class SensorInfo(
    val stepCounter: Boolean = false,
    val stepDetector: Boolean = false,
    val accelerometer: Boolean = false,
    val light: Boolean = false,
    val gps: Boolean = false,
    val camera: Boolean = false
)

/** Estado actual de los permisos peligrosos que usa la app. */
data class PermissionStatus(
    val location: Boolean = false,
    val camera: Boolean = false,
    val activityRecognition: Boolean = false
) {
    val allGranted: Boolean get() = location && camera && activityRecognition
}
