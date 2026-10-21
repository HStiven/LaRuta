package com.example.laruta.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/** Pestañas de la barra inferior (como "Monitor / Alarma / YO" de la app de referencia). */
enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    MONITOR("monitor", "Monitor", Icons.Filled.Explore),
    POIS("puntos", "Puntos", Icons.Filled.PhotoLibrary),
    PROFILE("yo", "Yo", Icons.Filled.Person);

    companion object {
        fun fromRoute(route: String?): Destination = entries.firstOrNull { it.route == route } ?: MONITOR
    }
}
