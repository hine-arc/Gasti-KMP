package com.example.gastikmp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gastikmp.presentation.screens.*

@Composable
fun MainScaffold(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf<Screen>(Screen.Home) }
    var currentSubScreen by remember { mutableStateOf<Screen?>(null) }

    val activeScreen = currentSubScreen ?: selectedTab

    Scaffold(
        bottomBar = {
            if (currentSubScreen == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        selected = selectedTab is Screen.Home,
                        onClick = { selectedTab = Screen.Home },
                        icon = { Text("🏠") },
                        label = { Text("Inicio") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab is Screen.Budget,
                        onClick = { selectedTab = Screen.Budget },
                        icon = { Text("🎯") },
                        label = { Text("Presupuesto") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab is Screen.Stats,
                        onClick = { selectedTab = Screen.Stats },
                        icon = { Text("📊") },
                        label = { Text("Estadísticas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab is Screen.Expenses,
                        onClick = { selectedTab = Screen.Expenses },
                        icon = { Text("➕") },
                        label = { Text("Gastos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab is Screen.Profile,
                        onClick = { selectedTab = Screen.Profile },
                        icon = { Text("👤") },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                is Screen.Home -> HomeScreen()
                is Screen.Budget -> BudgetScreen()
                is Screen.Stats -> StatsScreen()
                is Screen.Expenses -> ExpensesScreen()
                is Screen.Profile -> ProfileScreen(
                    onNavigateToCredits = { currentSubScreen = Screen.Credits },
                    onLogout = onLogout
                )
                is Screen.Credits -> CreditsScreen(
                    onBack = { currentSubScreen = null }
                )
                else -> HomeScreen()
            }
        }
    }
}
