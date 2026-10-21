package com.example.laruta.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta inspirada en apps de rastreo GPS (fondo azul noche + acento azul eléctrico).
// Cambia estos valores para personalizar toda la app.

// ----- Acentos (iguales en ambos modos) -----
val BrandBlue = Color(0xFF2D8CFF)      // Botones activos, ruta, ícono del usuario
val BrandBlueDeep = Color(0xFF1F6FE5)  // Primario en modo claro (más contraste sobre blanco)
val PoiOrange = Color(0xFFFF8A3D)      // Marcadores de fotos / puntos de interés
val RecordGreen = Color(0xFF2ECC71)    // Recorrido en curso
val StopRed = Color(0xFFFF5A5F)        // Detener / eliminar

// ----- Modo oscuro -----
val NightBackground = Color(0xFF12161F)
val NightSurface = Color(0xFF1B2230)
val NightSurfaceVariant = Color(0xFF242C3B)
val NightOnSurface = Color(0xFFE6EAF2)
val NightOnSurfaceVariant = Color(0xFF9AA4B5)
val NightOutline = Color(0xFF323C4E)

// ----- Modo claro -----
val DayBackground = Color(0xFFF3F5F9)
val DaySurface = Color(0xFFFFFFFF)
val DaySurfaceVariant = Color(0xFFE8ECF3)
val DayOnSurface = Color(0xFF151A23)
val DayOnSurfaceVariant = Color(0xFF5B6578)
val DayOutline = Color(0xFFD3D9E3)
