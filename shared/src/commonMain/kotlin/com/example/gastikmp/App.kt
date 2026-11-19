
package com.example.gastikmp

import androidx.compose.runtime.*
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.navigation.MainScaffold

/**
 * Punto principal de la aplicación Gasti KMP.
 *
 * Controla qué pantalla se muestra:
 *
 * - Inicio de sesión
 * - Registro
 * - Pantalla principal
 *
 * Utiliza el AuthRepository del paquete raíz
 * para mantener una única sesión de usuario.
 */
@Composable
fun App(database: GastiDatabase) {

    GastiTheme {

        // Consultamos el usuario autenticado.
        // Este estado se actualiza automáticamente
        // cuando se inicia o cierra sesión.
        val currentUser = AuthRepository.currentUser

        // Controla si el usuario está
        // en la pantalla de registro.
        var isRegistering by remember {
            mutableStateOf(false)
        }

        when {

            // USUARIO AUTENTICADO
            currentUser != null -> {

                MainScaffold(
                    database = database,
                    onLogout = {

                        // Cerrar la sesión actual.
                        AuthRepository.logout()

                        // Regresar al inicio de sesión.
                        isRegistering = false
                    }
                )
            }

            // PANTALLA DE REGISTRO
            isRegistering -> {

                RegisterScreen(
                    onNavigateBack = {

                        // Volver al inicio de sesión.
                        isRegistering = false
                    },
                    onRegisterSuccess = {

                        // El repositorio ya establece
                        // currentUser al registrar.
                        // App mostrará automáticamente
                        // la pantalla principal.
                        isRegistering = false
                    }
                )
            }

            // PANTALLA DE INICIO DE SESIÓN
            else -> {

                LoginScreen(
                    onNavigateToRegister = {

                        // Abrir la pantalla de registro.
                        isRegistering = true
                    },
                    onLoginSuccess = {

                        // AuthRepository.login ya
                        // actualiza currentUser.
                        // Compose mostrará Home
                        // automáticamente.
                        isRegistering = false
                    }
                )
            }
        }
    }
}
