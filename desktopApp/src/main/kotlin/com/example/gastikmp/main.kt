
package com.example.gastikmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import com.example.gastikmp.data.DatabaseDriverFactory
import com.example.gastikmp.data.DesktopPasswordSecurity
import com.example.gastikmp.data.UsuarioRepositorio
import com.example.gastikmp.database.GastiDatabase

/**
 * GASTI KMP - PASO 10.1
 *
 * Arranque de Windows Desktop.
 *
 * Utiliza la misma lógica compartida
 * que Android:
 *
 * - SQLDelight
 * - UsuarioRepositorio
 * - PasswordSecurity
 * - AuthRepository
 * - Home, Gastos, Presupuestos y Estadísticas
 */
fun main() {

    // 1. Abrir la base de datos SQLite de Desktop.
    val driver = DatabaseDriverFactory().createDriver()

    val database = GastiDatabase(driver)

    // 2. Crear el repositorio persistente de usuarios.
    val usuarioRepositorio = UsuarioRepositorio(
        database = database
    )

    // 3. Utilizar la implementación de seguridad
    //    correspondiente a Desktop/JVM.
    val seguridad = DesktopPasswordSecurity()

    // 4. Conectar la autenticación principal.
    AuthRepository.configurarPersistencia(
        repositorio = usuarioRepositorio,
        seguridad = seguridad
    )

    // 5. Iniciar la ventana principal de Gasti.
    application {

        Window(
            onCloseRequest = ::exitApplication,
            title = "Gasti KMP"
        ) {

            App(database)
        }
    }
}
