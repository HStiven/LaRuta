package com.example.laruta.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Formato colombiano: coma decimal, fechas en español. */
private val esCo: Locale = Locale.Builder().setLanguage("es").setRegion("CO").build()

/** 850 → "850 m" · 1250 → "1,25 km" */
fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.roundToInt()} m"
    else String.format(esCo, "%.2f km", meters / 1000.0)

/** 754000 ms → "00:12:34" */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

/** Metros por segundo → "4,2 km/h" */
fun formatSpeed(metersPerSecond: Float): String =
    String.format(esCo, "%.1f km/h", metersPerSecond * 3.6f)

/** Velocidad media del recorrido en km/h. */
fun formatAverageSpeed(meters: Double, millis: Long): String {
    if (millis < 1000) return formatSpeed(0f)
    return formatSpeed((meters / (millis / 1000.0)).toFloat())
}

/** 12345 → "12.345" */
fun formatSteps(steps: Int): String = NumberFormat.getIntegerInstance(esCo).format(steps)

/** "20 oct 2026 · 03:31 p. m." */
fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy · hh:mm a", esCo).format(Date(timestamp))

/** "20/10/2026" */
fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", esCo).format(Date(timestamp))

/** "15:31:08" */
fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm:ss", esCo).format(Date(timestamp))

/** "4.65331, -74.08360" (punto decimal: es el formato que entienden los mapas) */
fun formatCoordinates(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.5f, %.5f", latitude, longitude)

/** "Juan Pérez" → "JP" */
fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
