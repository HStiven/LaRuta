package com.example.laruta.data

import android.content.Context
import com.example.laruta.model.PointOfInterest
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Almacenamiento TEMPORAL en caché (context.cacheDir).
 *
 * - Las fotos se guardan como JPG en cacheDir/pois/
 * - La información asociada (coordenadas, fecha, nombre) en cacheDir/pois/pois.json
 *
 * Android puede borrar la caché si necesita espacio: por eso es "temporal",
 * tal como pide el enunciado.
 */
class PoiCache(context: Context) {

    private val directory = File(context.cacheDir, "pois").apply { mkdirs() }
    private val indexFile = File(directory, "pois.json")

    /** Archivo vacío donde la app de cámara escribirá la próxima foto. */
    fun newPhotoFile(): File {
        directory.mkdirs()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return File(directory, "POI_$stamp.jpg")
    }

    fun load(): List<PointOfInterest> = runCatching {
        if (!indexFile.exists()) return emptyList()
        val array = JSONArray(indexFile.readText())
        (0 until array.length())
            .map { array.getJSONObject(it).toPoi() }
            .filter { File(it.photoPath).exists() } // por si Android limpió la caché
    }.getOrDefault(emptyList())

    fun save(pois: List<PointOfInterest>) {
        runCatching {
            directory.mkdirs()
            val array = JSONArray()
            pois.forEach { array.put(it.toJson()) }
            indexFile.writeText(array.toString())
        }
    }

    fun delete(poi: PointOfInterest) {
        File(poi.photoPath).delete()
    }

    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
    }

    private fun PointOfInterest.toJson() = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("photoPath", photoPath)
        put("latitude", latitude)
        put("longitude", longitude)
        put("timestamp", timestamp)
        accuracyMeters?.let { put("accuracy", it.toDouble()) }
        address?.let { put("address", it) }
    }

    private fun JSONObject.toPoi() = PointOfInterest(
        id = getLong("id"),
        title = getString("title"),
        photoPath = getString("photoPath"),
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
        timestamp = getLong("timestamp"),
        accuracyMeters = if (has("accuracy")) getDouble("accuracy").toFloat() else null,
        address = if (has("address") && !isNull("address")) getString("address") else null
    )
}
