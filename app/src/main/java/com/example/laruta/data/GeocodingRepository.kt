package com.example.laruta.data

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.laruta.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class GeocodingRepository(private val context: Context) {

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            fromGoogleGeocodingApi(latitude, longitude) ?: fromAndroidGeocoder(latitude, longitude)
        }

    private fun fromGoogleGeocodingApi(latitude: Double, longitude: Double): String? {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isBlank()) return null

        val latLng = String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
        val url = URL(
            "https://maps.googleapis.com/maps/api/geocode/json" +
                "?latlng=$latLng&language=es&key=${URLEncoder.encode(apiKey, "UTF-8")}"
        )

        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val status = json.optString("status")
            if (status != "OK") {
                Log.w(TAG, "Geocoding API respondió $status: ${json.optString("error_message")}")
                return null
            }
            json.getJSONArray("results")
                .optJSONObject(0)
                ?.optString("formatted_address")
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo consultar la Geocoding API", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun fromAndroidGeocoder(latitude: Double, longitude: Double): String? = runCatching {
        if (!Geocoder.isPresent()) return null
        Geocoder(context, Locale.getDefault())
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
            ?.getAddressLine(0)
    }.getOrNull()

    private companion object {
        const val TAG = "GeocodingRepository"
    }
}
