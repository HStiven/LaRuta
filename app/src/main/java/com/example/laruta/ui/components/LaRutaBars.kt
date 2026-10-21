package com.example.laruta.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.laruta.navigation.Destination
import com.example.laruta.ui.theme.LaRutaTheme

/** Pestañas superiores de la pantalla Monitor (como "Lista | Mapa" de la referencia). */
enum class MonitorTab(val label: String) { LIST("Lista"), MAP("Mapa") }

/** Logo circular de la app (arriba a la izquierda). */
@Composable
fun AppLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Color(0xFF7B5CFF)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

/**
 * Barra superior de Monitor: logo · nombre del usuario · acción (tipo de mapa),
 * y debajo las pestañas Lista / Mapa.
 */
@Composable
fun MonitorTopBar(
    title: String,
    selectedTab: MonitorTab,
    onTabSelected: (MonitorTab) -> Unit,
    actionIcon: ImageVector,
    actionDescription: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLogoBadge()
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onTitleClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon, contentDescription = actionDescription, modifier = Modifier.size(26.dp))
                }
            }
            MonitorTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }
    }
}

/** Pestañas con subrayado azul bajo la seleccionada. */
@Composable
fun MonitorTabs(
    selectedTab: MonitorTab,
    onTabSelected: (MonitorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        MonitorTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = color
                    )
                    if (tab == MonitorTab.LIST) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = color)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                )
            }
        }
    }
}

/** Barra superior simple para las demás pantallas */
@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLogoBadge()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            actions()
        }
    }
}

/** Barra inferior: Monitor · Puntos (con contador de fotos) · Yo. */
@Composable
fun LaRutaBottomBar(
    current: Destination,
    poiCount: Int,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Destination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == current,
                onClick = { onSelect(destination) },
                icon = {
                    if (destination == Destination.POIS && poiCount > 0) {
                        BadgedBox(badge = { Badge { Text("$poiCount") } }) {
                            Icon(destination.icon, contentDescription = destination.label)
                        }
                    } else {
                        Icon(destination.icon, contentDescription = destination.label)
                    }
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}


@Preview(name = "Top bar · oscuro", showBackground = true)
@Composable
private fun MonitorTopBarDarkPreview() {
    LaRutaTheme(darkTheme = true) {
        MonitorTopBar(
            title = "Nombre usuario",
            selectedTab = MonitorTab.MAP,
            onTabSelected = {},
            actionIcon = Icons.Filled.Layers,
            actionDescription = "Tipo de mapa",
            onActionClick = {}
        )
    }
}

@Preview(name = "Top bar · claro", showBackground = true)
@Composable
private fun MonitorTopBarLightPreview() {
    LaRutaTheme(darkTheme = false) {
        MonitorTopBar(
            title = "Nombre usuario",
            selectedTab = MonitorTab.LIST,
            onTabSelected = {},
            actionIcon = Icons.Filled.Layers,
            actionDescription = "Tipo de mapa",
            onActionClick = {}
        )
    }
}

@Preview(name = "Top bar simple", showBackground = true)
@Composable
private fun ScreenTopBarPreview() {
    LaRutaTheme(darkTheme = true) {
        ScreenTopBar(title = "Puntos de Interés", subtitle = "3 fotos en el recorrido")
    }
}

@Preview(name = "Bottom bar", showBackground = true)
@Composable
private fun LaRutaBottomBarPreview() {
    LaRutaTheme(darkTheme = true) {
        LaRutaBottomBar(current = Destination.MONITOR, poiCount = 3, onSelect = {})
    }
}
