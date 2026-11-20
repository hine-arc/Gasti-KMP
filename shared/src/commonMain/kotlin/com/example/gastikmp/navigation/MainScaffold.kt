
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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
 * Navegacion compartida de Gasti.
 *
 * Permite que las pantallas internas
 * soliciten un cambio de pestana.
 *
 * Funciona en Android, Desktop y Web.
 */
val LocalGastiNavigate = compositionLocalOf<(Screen) -> Unit> {
    {}
}

/**
 * GASTI KMP
 *
 * Navegacion principal compartida.
 *
 * Android y Desktop:
 * utilizan GastiDatabase.
 *
 * Web:
 * utiliza webContent para mostrar
 * las pantallas financieras compartidas
 * con sus repositorios asincronos.
 *
 * Se conservan Perfil y Creditos.
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

    val activeScreen =
        currentSubScreen ?: selectedTab

    /**
     * Cambiar de pestana desde cualquier
     * pantalla dentro de MainScaffold.
     */
    val navegar: (Screen) -> Unit = { destino ->

        currentSubScreen = null
        selectedTab = destino
    }

    CompositionLocalProvider(
        LocalGastiNavigate provides navegar
    ) {

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
                                    navegar(screen)
                                },

                                icon = {

                                    Text(
                                        when (screen) {
                                            Screen.Home -> "⌂"
                                            Screen.Budget -> "◇"
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
                                            MaterialTheme.colorScheme.onSurfaceVariant,

                                        selectedTextColor =
                                            MaterialTheme.colorScheme.primary,

                                        unselectedTextColor =
                                            MaterialTheme.colorScheme.onSurfaceVariant,

                                        indicatorColor =
                                            MaterialTheme.colorScheme.surfaceVariant
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

                    // =================================
                    // PERFIL ORIGINAL
                    // =================================

                    Screen.Profile -> {

                        ProfileScreen(

                            onNavigateToCredits = {
                                currentSubScreen =
                                    Screen.Credits
                            },

                            onLogout = onLogout
                        )
                    }

                    // =================================
                    // CREDITOS ORIGINALES
                    // =================================

                    Screen.Credits -> {

                        CreditsScreen(

                            onBack = {
                                currentSubScreen = null
                            }
                        )
                    }

                    // =================================
                    // OTRAS PANTALLAS
                    // =================================

                    else -> {

                        if (webContent != null) {

                            // WEB:
                            // mantiene la misma barra
                            // de navegacion original.

                            webContent(activeScreen)

                        } else {

                            // ANDROID Y DESKTOP:
                            // mantienen las pantallas
                            // y los repositorios originales.

                            when (activeScreen) {

                                Screen.Home -> {

                                    HomeScreen(
                                        database = database,

                                        onIrAGastos = {
                                            navegar(Screen.Expenses)
                                        },

                                        onIrAPresupuestos = {
                                            navegar(Screen.Budget)
                                        }
                                    )
                                }

                                Screen.Budget -> {

                                    BudgetScreen()
                                }

                                Screen.Stats -> {

                                    StatsScreen()
                                }

                                Screen.Expenses -> {

                                    ExpensesDatabaseScreen(
                                        database = database
                                    )
                                }

                                else -> {

                                    HomeScreen(
                                        database = database,

                                        onIrAGastos = {
                                            navegar(Screen.Expenses)
                                        },

                                        onIrAPresupuestos = {
                                            navegar(Screen.Budget)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
