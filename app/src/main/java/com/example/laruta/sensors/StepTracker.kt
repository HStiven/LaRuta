package com.example.laruta.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.laruta.model.StepSource
import kotlin.math.sqrt

class StepTracker(
    context: Context,
    private val onSteps: (Int) -> Unit
) : SensorEventListener {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var counterBaseline = -1f // Valor del contador al iniciar (el hardware cuenta desde el último reinicio)
    private var steps = 0

    // Variables del detector por acelerómetro
    private var smoothedMagnitude = SensorManager.GRAVITY_EARTH
    private var aboveThreshold = false
    private var lastStepTimestampNs = 0L

    val hasStepCounter: Boolean get() = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    val hasStepDetector: Boolean get() = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null
    val hasAccelerometer: Boolean get() = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    fun start(): StepSource {
        stop()
        counterBaseline = -1f
        steps = 0
        aboveThreshold = false
        smoothedMagnitude = SensorManager.GRAVITY_EARTH

        // Desde Android 10 los sensores de pasos requieren ACTIVITY_RECOGNITION
        val canUseStepSensors = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                appContext, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

        val counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        return when {
            canUseStepSensors && counter != null -> {
                sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_UI)
                StepSource.STEP_COUNTER
            }
            canUseStepSensors && detector != null -> {
                sensorManager.registerListener(this, detector, SensorManager.SENSOR_DELAY_UI)
                StepSource.STEP_DETECTOR
            }
            accelerometer != null -> {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                StepSource.ACCELEROMETER
            }
            else -> StepSource.NONE
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0]
                if (counterBaseline < 0f) counterBaseline = total // primera lectura = punto de partida
                steps = (total - counterBaseline).toInt()
                onSteps(steps)
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                steps++
                onSteps(steps)
            }

            Sensor.TYPE_ACCELEROMETER -> detectStepFromAcceleration(event)
        }
    }

    private fun detectStepFromAcceleration(event: SensorEvent) {
        val (x, y, z) = event.values
        val magnitude = sqrt(x * x + y * y + z * z)
        // Filtro paso bajo para quitar ruido
        smoothedMagnitude = smoothedMagnitude * 0.75f + magnitude * 0.25f

        val gravity = SensorManager.GRAVITY_EARTH
        val enoughTimePassed = event.timestamp - lastStepTimestampNs > MIN_NS_BETWEEN_STEPS

        if (!aboveThreshold && smoothedMagnitude > gravity + PEAK_THRESHOLD && enoughTimePassed) {
            aboveThreshold = true
            lastStepTimestampNs = event.timestamp
            steps++
            onSteps(steps)
        } else if (aboveThreshold && smoothedMagnitude < gravity + RESET_THRESHOLD) {
            aboveThreshold = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val PEAK_THRESHOLD = 1.8f              // m/s² sobre la gravedad para contar un paso
        const val RESET_THRESHOLD = 0.5f             // hay que bajar de aquí antes del siguiente paso
        const val MIN_NS_BETWEEN_STEPS = 280_000_000L // 0.28 s: nadie da más de ~3.5 pasos por segundo
    }
}
