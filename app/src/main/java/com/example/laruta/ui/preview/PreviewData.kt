package com.example.laruta.ui.preview

import com.example.laruta.model.PointOfInterest
import com.example.laruta.model.StepSource
import com.example.laruta.model.TrackingState
import com.example.laruta.model.TrackingStatus
import com.example.laruta.model.TravelMode
import com.example.laruta.model.UserSettings
import com.google.android.gms.maps.model.LatLng

object PreviewData {

    private const val NOW = 1_792_528_260_000L // 20 oct 2026, 3:31 p. m. (Bogotá)

    val pois = listOf(
        PointOfInterest(
            id = 1, title = "Mural de la 26", photoPath = "",
            latitude = 4.65312, longitude = -74.10021, timestamp = NOW - 1_800_000,
            accuracyMeters = 6f, address = "Av. El Dorado #68-51, Bogotá"
        ),
        PointOfInterest(
            id = 2, title = "Parque Salitre", photoPath = "",
            latitude = 4.65844, longitude = -74.09371, timestamp = NOW - 900_000,
            accuracyMeters = 4f, address = "Cl. 63 #59A-06, Bogotá"
        ),
        PointOfInterest(
            id = 3, title = "Punto 3", photoPath = "",
            latitude = 4.66101, longitude = -74.09012, timestamp = NOW,
            accuracyMeters = 9f, address = null
        )
    )

    private val route = listOf(
        LatLng(4.65100, -74.10300), LatLng(4.65312, -74.10021),
        LatLng(4.65600, -74.09700), LatLng(4.65844, -74.09371)
    )

    val trackingState = TrackingState(
        status = TrackingStatus.TRACKING,
        mode = TravelMode.WALK,
        steps = 4_231,
        stepSource = StepSource.STEP_COUNTER,
        gpsDistanceMeters = 3_080.0,
        elapsedMillis = 2_745_000,
        route = route,
        currentLocation = route.last(),
        speedMps = 1.4f,
        accuracyMeters = 5f,
        pois = pois
    )

    val idleState = TrackingState(currentLocation = LatLng(4.65844, -74.09371), accuracyMeters = 12f)

    val settings = UserSettings(userName = "Hernando Cortés", strideMeters = 0.75f)
}
