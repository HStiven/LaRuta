package com.example.laruta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF173A66),
    onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = PoiOrange,
    onSecondary = Color.White,
    tertiary = RecordGreen,
    onTertiary = Color.White,
    error = StopRed,
    onError = Color.White,
    background = NightBackground,
    onBackground = NightOnSurface,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceVariant,
    surfaceContainerLow = NightSurface,
    outline = NightOutline,
    outlineVariant = NightOutline
)

private val LightColors = lightColorScheme(
    primary = BrandBlueDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E7FF),
    onPrimaryContainer = Color(0xFF0B2D5C),
    secondary = PoiOrange,
    onSecondary = Color.White,
    tertiary = Color(0xFF1FA65A),
    onTertiary = Color.White,
    error = Color(0xFFE5484D),
    onError = Color.White,
    background = DayBackground,
    onBackground = DayOnSurface,
    surface = DaySurface,
    onSurface = DayOnSurface,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayOnSurfaceVariant,
    surfaceContainer = DaySurface,
    surfaceContainerHigh = DaySurfaceVariant,
    surfaceContainerLow = DaySurface,
    outline = DayOutline,
    outlineVariant = DayOutline
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun LaRutaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        shapes = AppShapes,
        content = content
    )
}
