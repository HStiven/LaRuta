# LaRuta

App Android (Kotlin + Jetpack Compose) para monitorear un recorrido a pie, corriendo o en moto: cuenta pasos con el sensor de movimiento, dibuja la ruta en Google Maps, cambia a modo oscuro con el sensor de luz y guarda fotos georreferenciadas como **Puntos de Interés**.

## 1. Configurar la API key de Google Maps (obligatorio)

1. En [Google Cloud Console](https://console.cloud.google.com/) crea un proyecto y habilita:
   - **Maps SDK for Android** (el mapa, marcadores y PolyLine)
   - **Geocoding API** (convierte las coordenadas de cada foto en una dirección)
2. Crea una API key. Para la Geocoding API la key **no** puede estar restringida a "Apps de Android"; restríngela solo por API (las dos de arriba).
3. En el archivo `local.properties` (raíz del proyecto, **no se sube a Git**) agrega:

   ```
   MAPS_API_KEY=AIza...tu_key
   ```

4. *Sync Project with Gradle Files* y ejecuta.

Si el mapa sale gris: la key está vacía, mal copiada o sin "Maps SDK for Android" habilitado (revisa Logcat filtrando por `Google Maps`).

## 2. Cómo cumple cada requerimiento

| Requerimiento | Dónde está |
|---|---|
| "Iniciar recorrido" pide permisos GPS + Cámara (+ actividad física) | `ui/screens/MonitorScreen.kt` → `onStartClick()` y `util/Permissions.kt` |
| Pasos y distancia con sensor de movimiento | `sensors/StepTracker.kt` (STEP_COUNTER → STEP_DETECTOR → acelerómetro). Distancia = pasos × zancada (`model/Models.kt`) |
| Modo claro/oscuro automático con sensor de luz | `sensors/AmbientLight.kt` + `MainActivity.kt` (también cambia el estilo del mapa) |
| Ubicación actual en Google Maps | `ui/map/TrackingMap.kt` (maps-compose) + GPS en `viewmodel/TrackingViewModel.kt` |
| Botón para abrir la cámara en cualquier momento | Botón **Agregar** del riel derecho → modal → `TakePicture` |
| Foto con coordenadas, fecha/hora y marcador | `TrackingViewModel.onPhotoResult()` → `PointOfInterest` → `MarkerComposable` |
| Guardado temporal en caché + sección "Puntos de Interés" tipo dashboard | `data/PoiCache.kt` (`cacheDir/pois`) + `ui/screens/PoiDashboardScreen.kt` |
| (Opcional) PolyLine de la ruta | `TrackingMap.kt` → `Polyline(points = state.route)` |

## 3. Estructura

```
model/        Datos: TrackingState, PointOfInterest, ajustes
sensors/      StepTracker (pasos), AmbientLight (luz)
data/         PoiCache (caché), SettingsStore (preferencias), GeocodingRepository (Google Geocoding API)
viewmodel/    TrackingViewModel: GPS, cronómetro, fotos, estado compartido
ui/components Piezas del diseño, cada una con @Preview
ui/map        TrackingMap (Google Maps)
ui/screens    Monitor (Mapa/Lista), Puntos de Interés, Yo
ui/preview    PreviewData: datos falsos para los @Preview
```

Cada pantalla tiene dos versiones: `XxxRoute` (con estado, conectada al ViewModel) y `XxxScreen` (solo diseño, con `@Preview`). Para cambiar el diseño edita los `XxxScreen` y los archivos de `ui/components`, y los colores en `ui/theme/Color.kt`.

## 4. Probar en el emulador

- **Ruta GPS:** Extended controls (⋯) → *Location* → *Routes*, traza una ruta y dale *Play route*.
- **Sensor de luz:** Extended controls → *Virtual sensors* → *Additional sensors* → *Light*. Por debajo de 15 lx pasa a oscuro y por encima de 40 lx vuelve a claro.
- **Cámara:** el emulador trae una cámara virtual.
- **Pasos:** el contador de pasos no funciona bien en el emulador; pruébalo en un celular real caminando.
