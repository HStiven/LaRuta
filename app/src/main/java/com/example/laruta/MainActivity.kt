package com.example.laruta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.laruta.model.ThemeMode
import com.example.laruta.sensors.rememberAmbientLightState
import com.example.laruta.ui.LaRutaApp
import com.example.laruta.ui.theme.LaRutaTheme
import com.example.laruta.viewmodel.TrackingViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: TrackingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val lightState = rememberAmbientLightState() // registra el sensor TYPE_LIGHT
            val systemDark = isSystemInDarkTheme()

            val darkTheme = when (settings.themeMode) {
                ThemeMode.AUTO -> lightState.isDark ?: systemDark // sin sensor/lectura: sigue al sistema
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            DisposableEffect(darkTheme) {
                val transparent = android.graphics.Color.TRANSPARENT
                val style = if (darkTheme) SystemBarStyle.dark(transparent)
                else SystemBarStyle.light(transparent, transparent)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }

            LaRutaTheme(darkTheme = darkTheme) {
                LaRutaApp(viewModel = viewModel, lightState = lightState, isDarkTheme = darkTheme)
            }
        }
    }
}
