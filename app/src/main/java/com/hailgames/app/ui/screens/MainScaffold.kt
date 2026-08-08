package com.hailgames.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.hailgames.app.ui.navigation.MainDestinations

private data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    MainTab(MainDestinations.HOME, "Início", Icons.Filled.Home),
    MainTab(MainDestinations.SETTINGS, "Ajustes", Icons.Filled.Settings)
)

@Composable
fun MainScaffold(
    sessionViewModel: SessionViewModel,
    onNavigateDetail: (String) -> Unit,
    onNavigateAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (tabs[selectedTab].route) {
            MainDestinations.HOME -> HomeScreen(
                onItemClick = onNavigateDetail,
                modifier = Modifier.paddingBottom(innerPadding.calculateBottomPadding())
            )
            MainDestinations.SETTINGS -> SettingsScreen(
                sessionViewModel = sessionViewModel,
                onNavigateAdmin = onNavigateAdmin,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
