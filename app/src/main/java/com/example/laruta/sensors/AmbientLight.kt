package com.example.laruta.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Stable
class AmbientLightState(val isAvailable: Boolean) {
    var lux by mutableFloatStateOf(-1f)
        private set

    var targetDark by mutableStateOf<Boolean?>(null)
        private set

    /** Modo aplicado al tema (se aplica con un pequeño retraso para evitar cambios bruscos). */
    var isDark by mutableStateOf<Boolean?>(null)
        internal set

    internal fun onLux(value: Float) {
        lux = value
        val current = targetDark
        targetDark = when {
            value < DARK_BELOW_LUX -> true
            value > LIGHT_ABOVE_LUX -> false
            else -> current ?: (value < (DARK_BELOW_LUX + LIGHT_ABOVE_LUX) / 2f)
        }
    }

    companion object {
        const val DARK_BELOW_LUX = 15f
        const val LIGHT_ABOVE_LUX = 40f
        const val APPLY_DELAY_MS = 1200L
    }
}

/**
 * Registra el sensor de luz mientras la app está en primer plano (ON_RESUME → ON_PAUSE)
 * y devuelve su estado para que el tema cambie automáticamente.
 */
@Composable
fun rememberAmbientLightState(): AmbientLightState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }
    val state = remember { AmbientLightState(isAvailable = lightSensor != null) }

    DisposableEffect(lifecycleOwner, lightSensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) = state.onLux(event.values[0])
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> lightSensor?.let {
                    sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
                Lifecycle.Event.ON_PAUSE -> sensorManager.unregisterListener(listener)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(listener)
        }
    }

    // La primera lectura se aplica de inmediato; las siguientes, si se mantienen 1.2 s.
    LaunchedEffect(state.targetDark) {
        val target = state.targetDark ?: return@LaunchedEffect
        if (state.isDark != null) delay(AmbientLightState.APPLY_DELAY_MS)
        state.isDark = target
    }

    return state
}
