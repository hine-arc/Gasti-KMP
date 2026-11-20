
package com.example.gastikmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/**
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Punto de entrada principal.
 *
 * Utiliza las pantallas compartidas
 * de inicio de sesión, registro
 * y navegación.
 *
 * Las pruebas temporales de SQLite
 * ya fueron completadas.
 *
 * La conexión financiera se realizará
 * mediante un controlador compartido,
 * evitando trabajadores simultáneos.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport {

        GastiTheme {

            WebAuthScreen()
        }
    }
}
