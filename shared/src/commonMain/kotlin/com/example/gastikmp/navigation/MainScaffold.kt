
package com.example.gastikmp.navigation

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

import com.example.gastikmp.presentation.ExpensesScreen as ExpensesDatabaseScreen

/**
 * GASTI KMP
 *
 * Navegacion principal compartida.
 *
 * Android y Desktop:
 * reciben una base de datos SQLDelight.
 *
 * Web:
 * puede proporcionar contenido financiero
 * mediante webContent, sin duplicar
 * la barra de navegacion.
 *
 * Las pantallas originales de Android
 * y Desktop permanecen sin cambios.
 */
@Composable
fun MainScaffold(
    database: GastiDatabase? = null,
    onLogout: () -> Unit,
    webContent: (@Composable (Screen) -> Unit)? = null
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
                    containerColor =
                        MaterialTheme.colorScheme.surface,
                    contentColor =
                        MaterialTheme.colorScheme.primary
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
                                        Screen.Home -> "⌂"
                                        Screen.Budget -> "◈"
                                        Screen.Stats -> "▥"
                                        Screen.Expenses -> "$"
                                        else -> "●"
                                    }
                                )
                            },

                            label = {
                                Text(label)
                            },

                            colors =
                                NavigationBarItemDefaults.colors(

                                    selectedIconColor =
                                        MaterialTheme.colorScheme.primary,

                                    unselectedIconColor =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant,

                                    selectedTextColor =
                                        MaterialTheme.colorScheme.primary,

                                    unselectedTextColor =
                                        MaterialTheme.colorScheme
                                            .onSurfaceVariant,

                                    indicatorColor =
                                        MaterialTheme.colorScheme
                                            .surfaceVariant
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

                // INICIO
                Screen.Home -> {

                    if (database != null) {

                        HomeScreen(
                            database = database,

                            onIrAGastos = {
                                selectedTab = Screen.Expenses
                            },

                            onIrAPresupuestos = {
                                selectedTab = Screen.Budget
                            }
                        )

                    } else {

                        ContenidoWeb(
                            screen = Screen.Home,
                            webContent = webContent
                        )
                    }
                }

                // PRESUPUESTOS
                Screen.Budget -> {

                    if (database != null) {

                        BudgetScreen(
                            database = database
                        )

                    } else {

                        ContenidoWeb(
                            screen = Screen.Budget,
                            webContent = webContent
                        )
                    }
                }

                // ESTADISTICAS
                Screen.Stats -> {

                    if (database != null) {

                        StatsScreen(
                            database = database
                        )

                    } else {

                        ContenidoWeb(
                            screen = Screen.Stats,
                            webContent = webContent
                        )
                    }
                }

                // GASTOS E INGRESOS
                Screen.Expenses -> {

                    if (database != null) {

                        ExpensesDatabaseScreen(
                            database = database
                        )

                    } else {

                        ContenidoWeb(
                            screen = Screen.Expenses,
                            webContent = webContent
                        )
                    }
                }

                // PERFIL ORIGINAL
                Screen.Profile -> {

                    ProfileScreen(
                        onNavigateToCredits = {
                            currentSubScreen = Screen.Credits
                        },

                        onLogout = onLogout
                    )
                }

                // CREDITOS ORIGINALES
                Screen.Credits -> {

                    CreditsScreen(
                        onBack = {
                            currentSubScreen = null
                        }
                    )
                }

                // PANTALLA POR DEFECTO
                else -> {

                    if (database != null) {

                        HomeScreen(
                            database = database,

                            onIrAGastos = {
                                selectedTab = Screen.Expenses
                            },

                            onIrAPresupuestos = {
                                selectedTab = Screen.Budget
                            }
                        )

                    } else {

                        ContenidoWeb(
                            screen = Screen.Home,
                            webContent = webContent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Permite mostrar contenido financiero Web
 * sin modificar la navegacion compartida.
 *
 * La implementacion real de las pantallas
 * financieras Web se conectara despues.
 */
@Composable
private fun ContenidoWeb(
    screen: Screen,
    webContent: (@Composable (Screen) -> Unit)?
) {

    if (webContent != null) {

        webContent(screen)

    } else {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment =
                androidx.compose.ui.Alignment.Center
        ) {

            Text(
                text = "Almacenamiento financiero Web pendiente",
                color =
                    MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
