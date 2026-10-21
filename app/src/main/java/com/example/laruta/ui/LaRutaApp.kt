package com.example.laruta.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.laruta.model.UiAction
import com.example.laruta.navigation.Destination
import com.example.laruta.sensors.AmbientLightState
import com.example.laruta.ui.components.LaRutaBottomBar
import com.example.laruta.ui.screens.MonitorRoute
import com.example.laruta.ui.screens.PoiDashboardRoute
import com.example.laruta.ui.screens.ProfileRoute
import com.example.laruta.util.openAppSettings
import com.example.laruta.viewmodel.TrackingViewModel

private fun NavHostController.navigateTo(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun LaRutaApp(
    viewModel: TrackingViewModel,
    lightState: AmbientLightState,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.fromRoute(backStackEntry?.destination?.route)

    // Avisa al ViewModel si la app pasa a segundo plano (para apagar el GPS si no hay recorrido)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onAppForeground()
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackground()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Mensajes del ViewModel → Snackbar
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                when (message.action) {
                    UiAction.OPEN_POIS -> navController.navigateTo(Destination.POIS)
                    UiAction.OPEN_SETTINGS -> context.openAppSettings()
                    null -> Unit
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            LaRutaBottomBar(
                current = current,
                poiCount = state.pois.size,
                onSelect = { navController.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.MONITOR.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.MONITOR.route) {
                MonitorRoute(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onOpenPois = { navController.navigateTo(Destination.POIS) }
                )
            }
            composable(Destination.POIS.route) {
                PoiDashboardRoute(
                    viewModel = viewModel,
                    onShowOnMap = { navController.navigateTo(Destination.MONITOR) }
                )
            }
            composable(Destination.PROFILE.route) {
                ProfileRoute(viewModel = viewModel, lightState = lightState, isDarkTheme = isDarkTheme)
            }
        }
    }
}
