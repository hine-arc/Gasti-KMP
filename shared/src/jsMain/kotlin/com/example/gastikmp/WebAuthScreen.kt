
package com.example.gastikmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gastikmp.data.WebIndexedDbStorage
import com.example.gastikmp.data.WebPasswordSecurity
import com.example.gastikmp.navigation.MainScaffold

/**
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Reutiliza el diseño de:
 * - LoginScreen
 * - RegisterScreen
 * - MainScaffold
 *
 * Las cuentas Web se guardan en IndexedDB.
 *
 * Android y Desktop mantienen SQLDelight.
 */
@Composable
fun WebAuthScreen() {

    val almacenamiento = remember {
        WebIndexedDbStorage()
    }

    val seguridad = remember {
        WebPasswordSecurity()
    }

    var mostrarRegistro by remember {
        mutableStateOf(false)
    }

    var usuarioActual by remember {
        mutableStateOf<String?>(null)
    }

    var correoActual by remember {
        mutableStateOf<String?>(null)
    }

    if (usuarioActual != null) {

        // =====================================
        // NAVEGACION ORIGINAL DE GASTI
        // =====================================

        MainScaffold(
            database = null,

            onLogout = {
                usuarioActual = null
                correoActual = null
                mostrarRegistro = false
            },

            webContent = { screen ->

                ContenidoFinancieroWebPendiente(
                    screen = screen,
                    nombre = usuarioActual ?: ""
                )
            }
        )

    } else if (mostrarRegistro) {

        // =====================================
        // REGISTRO ORIGINAL
        // =====================================

        RegisterScreen(
            onNavigateBack = {
                mostrarRegistro = false
            },

            onRegisterSuccess = {
                mostrarRegistro = false
            },

            registrar = { email, password, name ->

                runCatching {

                    val correoNormalizado =
                        email.trim().lowercase()

                    val nombreNormalizado =
                        name.trim()

                    val claveBase =
                        "usuario:$correoNormalizado"

                    require(nombreNormalizado.isNotBlank()) {
                        "El nombre es obligatorio"
                    }

                    val nombreExistente =
                        almacenamiento.leer(
                            "$claveBase:nombre"
                        )

                    require(nombreExistente == null) {
                        "El correo ya esta registrado"
                    }

                    val protegida =
                        seguridad.proteger(password)

                    // Guardar hash y sal;
                    // nunca la contrasena original.
                    almacenamiento.guardar(
                        "$claveBase:hash",
                        protegida.passwordHash
                    )

                    almacenamiento.guardar(
                        "$claveBase:salt",
                        protegida.passwordSalt
                    )

                    // Guardar el nombre al final.
                    almacenamiento.guardar(
                        "$claveBase:nombre",
                        nombreNormalizado
                    )

                    correoActual = correoNormalizado
                    usuarioActual = nombreNormalizado

                    Unit
                }
            }
        )

    } else {

        // =====================================
        // LOGIN ORIGINAL
        // =====================================

        LoginScreen(
            onNavigateToRegister = {
                mostrarRegistro = true
            },

            onLoginSuccess = {
                // El usuario ya fue establecido
                // por la autenticacion Web.
            },

            autenticar = { email, password ->

                runCatching {

                    val correoNormalizado =
                        email.trim().lowercase()

                    val claveBase =
                        "usuario:$correoNormalizado"

                    val nombreGuardado =
                        almacenamiento.leer(
                            "$claveBase:nombre"
                        )

                    val hashGuardado =
                        almacenamiento.leer(
                            "$claveBase:hash"
                        )

                    val saltGuardada =
                        almacenamiento.leer(
                            "$claveBase:salt"
                        )

                    if (
                        nombreGuardado == null ||
                        hashGuardado == null ||
                        saltGuardada == null
                    ) {
                        error(
                            "Correo o contrasena incorrectos"
                        )
                    }

                    val claveCorrecta =
                        seguridad.verificar(
                            password = password,
                            passwordHash = hashGuardado,
                            passwordSalt = saltGuardada
                        )

                    check(claveCorrecta) {
                        "Correo o contrasena incorrectos"
                    }

                    correoActual = correoNormalizado
                    usuarioActual = nombreGuardado

                    Unit
                }
            }
        )
    }
}

/**
 * Contenido provisional exclusivo para Web.
 *
 * La barra inferior y la navegacion
 * ya son las mismas de Android y Desktop.
 *
 * Las operaciones financieras reales
 * se conectaran en los siguientes pasos.
 */
@Composable
private fun ContenidoFinancieroWebPendiente(
    screen: Screen,
    nombre: String
) {

    val titulo = when (screen) {
        Screen.Home -> "Inicio"
        Screen.Budget -> "Presupuesto"
        Screen.Stats -> "Estadísticas"
        Screen.Expenses -> "Gastos e ingresos"
        else -> "Gasti"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (screen == Screen.Home) {

            Text(
                text = "¡Bienvenido, $nombre!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        Text(
            text = "Estamos conectando los datos financieros de Gasti Web.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Tus cuentas registradas permanecen en IndexedDB.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
