package com.example.laruta.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.laruta.model.PermissionStatus

/**
 * Permisos PELIGROSOS que usa la app. Declararlos en el Manifest no basta:
 * desde Android 6 se piden en tiempo de ejecución (al pulsar "Iniciar recorrido").
 */
object AppPermissions {

    val LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,   // GPS preciso
        Manifest.permission.ACCESS_COARSE_LOCATION  // Android 12+ exige pedirlo junto al FINE
    )

    /** Lo que se pide al pulsar "Iniciar recorrido": GPS + Cámara (+ actividad física en Android 10+). */
    val START_TRACKING: Array<String>
        get() = buildList {
            addAll(LOCATION)
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION) // Contador de pasos del hardware
            }
        }.toTypedArray()

    /** Lo mínimo para tomar una foto georreferenciada. */
    val PHOTO: Array<String> = arrayOf(Manifest.permission.CAMERA) + LOCATION

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasLocation(context: Context) = LOCATION.any { granted(context, it) }

    fun hasCamera(context: Context) = granted(context, Manifest.permission.CAMERA)

    fun hasActivityRecognition(context: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            granted(context, Manifest.permission.ACTIVITY_RECOGNITION)

    fun status(context: Context) = PermissionStatus(
        location = hasLocation(context),
        camera = hasCamera(context),
        activityRecognition = hasActivityRecognition(context)
    )
}

/** Obtiene la Activity a partir de un Context de Compose (para shouldShowRequestPermissionRationale). */
fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/** Abre la pantalla de ajustes de la app (cuando el usuario negó un permiso "para siempre"). */
fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
