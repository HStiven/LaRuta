// app/build.gradle.kts (módulo :app)
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// La API key de Google Maps se lee de local.properties (archivo que NO se sube a Git).
// En local.properties agrega la línea:  MAPS_API_KEY=tu_api_key
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.example.laruta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.laruta"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Se inyecta en el AndroidManifest como ${MAPS_API_KEY}
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY")
            ?: (project.findProperty("MAPS_API_KEY") as String?)
            ?: ""
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        // También disponible en código (BuildConfig.MAPS_API_KEY) para la Geocoding API de Google
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true // Genera BuildConfig.MAPS_API_KEY
    }
}

dependencies {
    // ---------- Jetpack Compose ----------
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended") // Íconos: caminar, moto, cámara…
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---------- AndroidX ----------
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")   // collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") // viewModel()
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ---------- Google Maps + ubicación ----------
    implementation("com.google.maps.android:maps-compose:6.2.1")              // GoogleMap, Marker, Polyline en Compose
    implementation("com.google.android.gms:play-services-location:21.3.0")    // FusedLocationProviderClient (GPS)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0") // Task.await()

    // ---------- Imágenes ----------
    implementation("io.coil-kt:coil-compose:2.7.0") // Carga las fotos desde la caché
}
