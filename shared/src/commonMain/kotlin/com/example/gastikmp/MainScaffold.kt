package com.example.gastikmp.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.gastikmp.Screen
import com.example.gastikmp.HomeScreen
import com.example.gastikmp.BudgetScreen
import com.example.gastikmp.StatsScreen
import com.example.gastikmp.ProfileScreen
import com.example.gastikmp.CreditsScreen
import com.example.gastikmp.database.GastiDatabase

@Composable
fun MainScaffold(
    database: GastiDatabase,
    onLogout: () -> Unit
) {

    var selectedTab by remember {
        mutableStateOf<Screen>(Screen.Home)
    }

    var currentSubScreen by remember {
        mutableStateOf<Screen?>(null)
    }

    val activeScreen = currentSubScreen ?: selectedTab

    Scaffold(

        bottomBar = {

            if (currentSubScreen == null) {

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {

                    val screens = listOf(
                        Screen.Home to "Inicio",
                        Screen.Budget to "Presupuesto",
                        Screen.Stats to "Estadísticas",
                        Screen.Expenses to "Gastos",
                        Screen.Profile to "Perfil"
                    )

                    screens.forEach { (screen, label) ->

                        NavigationBarItem(
                            selected = selectedTab == screen,

                            onClick = {
                                selectedTab = screen
                            },

                            icon = {
                                Text(
                                    when (screen) {
                                        Screen.Home -> "🏠"
                                        Screen.Budget -> "🎯"
                                        Screen.Stats -> "📊"
                                        Screen.Expenses -> "➕"
                                        else -> "👤"
                                    }
                                )
                            },

                            label = {
                                Text(label)
                            },

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
        }

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (activeScreen) {

                Screen.Home -> {
                    HomeScreen()
                }

                Screen.Budget -> {
                    BudgetScreen()
                }

                Screen.Stats -> {
                    StatsScreen()
                }

                Screen.Expenses -> {
                    ExpensesScreen(database)
                }

                Screen.Profile -> {
                    ProfileScreen(
                        onNavigateToCredits = {
                            currentSubScreen = Screen.Credits
                        },
                        onLogout = onLogout
                    )
                }

                Screen.Credits -> {
                    CreditsScreen(
                        onBack = {
                            currentSubScreen = null
                        }
                    )
                }

                else -> {
                    HomeScreen()
                }
            }
        }
    }
}