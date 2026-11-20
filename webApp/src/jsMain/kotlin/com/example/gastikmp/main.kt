
package com.example.gastikmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

import com.example.gastikmp.data.WebSqlDelightDriverFactory

/**
 * Punto de entrada de Gasti Web.
 *
 * Ejecuta una prueba de SQLite al iniciar
 * la aplicación y conserva la interfaz
 * original de inicio de sesión y navegación.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport {

        GastiTheme {

            PruebaSqliteWeb()

            WebAuthScreen()
        }
    }
}

/**
 * Prueba temporal de conexión SQLite.
 *
 * El resultado aparece en la consola
 * del navegador.
 *
 * No modifica las cuentas guardadas
 * actualmente en IndexedDB.
 */
@Composable
private fun PruebaSqliteWeb() {

    LaunchedEffect(Unit) {

        println("GASTI SQLITE: iniciando prueba...")

        try {

            val resultado =
                WebSqlDelightDriverFactory
                    .verificarConexion()

            resultado.fold(
                onSuccess = {

                    println(
                        "GASTI SQLITE: CONEXION EXITOSA"
                    )
                },

                onFailure = { error ->

                    println(
                        "GASTI SQLITE: ERROR - " +
                                (error.message ?: error.toString())
                    )
                }
            )

        } catch (error: Throwable) {

            println(
                "GASTI SQLITE: ERROR - " +
                        (error.message ?: error.toString())
            )
        }
    }
}
