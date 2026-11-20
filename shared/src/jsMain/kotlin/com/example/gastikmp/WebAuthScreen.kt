
package com.example.gastikmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import app.cash.sqldelight.db.SqlDriver

import com.example.gastikmp.data.WebGastoDataSource
import com.example.gastikmp.data.WebIndexedDbStorage
import com.example.gastikmp.data.WebPasswordSecurity
import com.example.gastikmp.data.WebSqlDelightDriverFactory
import com.example.gastikmp.navigation.MainScaffold
import com.example.gastikmp.presentation.ExpensesScreen

import kotlinx.coroutines.isActive

private class SesionFinancieraWeb {

    var driver by mutableStateOf<SqlDriver?>(null)

    var mensajeError by mutableStateOf<String?>(null)

    var preparando by mutableStateOf(true)

    fun cerrar() {
        val anterior = driver
        driver = null

        if (anterior != null) {
            runCatching {
                anterior.close()
            }
        }
    }
}

/**
 * Gasti Web:
 * autenticacion original y conexion financiera
 * compartida durante la sesion.
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

    if (usuarioActual != null && correoActual != null) {

        val emailSesion = correoActual!!

        val sesion = remember(emailSesion) {
            SesionFinancieraWeb()
        }

        LaunchedEffect(sesion) {

            sesion.preparando = true
            sesion.mensajeError = null

            try {

                println(
                    "GASTI WEB: verificando SQLite financiero..."
                )

                WebSqlDelightDriverFactory
                    .verificarConexion()
                    .getOrThrow()

                if (isActive) {

                    sesion.driver =
                        WebSqlDelightDriverFactory.createDriver()

                    println(
                        "GASTI WEB: conexion financiera preparada"
                    )
                }

            } catch (error: Exception) {

                sesion.mensajeError =
                    error.message
                        ?: "No se pudo preparar SQLite"

                println(
                    "GASTI WEB: ERROR SQLITE - " +
                            sesion.mensajeError
                )

            } finally {
                sesion.preparando = false
            }
        }

        DisposableEffect(sesion) {
            onDispose {
                sesion.cerrar()
                println(
                    "GASTI WEB: conexion financiera cerrada"
                )
            }
        }

        val fuenteGastos = remember(sesion.driver) {
            sesion.driver?.let { driver ->
                WebGastoDataSource(driver)
            }
        }

        MainScaffold(
            database = null,

            onLogout = {
                usuarioActual = null
                correoActual = null
                mostrarRegistro = false
            },

            webContent = { screen ->

                when {

                    screen == Screen.Expenses &&
                            fuenteGastos != null -> {

                        ExpensesScreen(
                            database = null,
                            dataSource = fuenteGastos,
                            usuarioEmail = emailSesion
                        )
                    }

                    screen == Screen.Expenses &&
                            sesion.preparando -> {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            CircularProgressIndicator()

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Text("Preparando Gastos e ingresos...")
                        }
                    }

                    else -> {

                        ContenidoFinancieroWebPendiente(
                            screen = screen,
                            nombre = usuarioActual ?: "",
                            preparando = sesion.preparando,
                            conexionPreparada = sesion.driver != null,
                            mensajeError = sesion.mensajeError
                        )
                    }
                }
            }
        )

    } else if (mostrarRegistro) {

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
                        almacenamiento.leer("$claveBase:nombre")

                    require(nombreExistente == null) {
                        "El correo ya esta registrado"
                    }

                    val protegida =
                        seguridad.proteger(password)

                    almacenamiento.guardar(
                        "$claveBase:hash",
                        protegida.passwordHash
                    )

                    almacenamiento.guardar(
                        "$claveBase:salt",
                        protegida.passwordSalt
                    )

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

        LoginScreen(
            onNavigateToRegister = {
                mostrarRegistro = true
            },

            onLoginSuccess = {
                // Usuario establecido al autenticar.
            },

            autenticar = { email, password ->

                runCatching {

                    val correoNormalizado =
                        email.trim().lowercase()

                    val claveBase =
                        "usuario:$correoNormalizado"

                    val nombreGuardado =
                        almacenamiento.leer("$claveBase:nombre")

                    val hashGuardado =
                        almacenamiento.leer("$claveBase:hash")

                    val saltGuardada =
                        almacenamiento.leer("$claveBase:salt")

                    if (
                        nombreGuardado == null ||
                        hashGuardado == null ||
                        saltGuardada == null
                    ) {
                        error("Correo o contrasena incorrectos")
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

@Composable
private fun ContenidoFinancieroWebPendiente(
    screen: Screen,
    nombre: String,
    preparando: Boolean,
    conexionPreparada: Boolean,
    mensajeError: String?
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
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        val mensaje = when {
            mensajeError != null ->
                "Error de conexión SQLite: $mensajeError"

            preparando ->
                "Preparando la conexión SQLite de Gasti Web..."

            conexionPreparada ->
                "Conexión SQLite financiera preparada."

            else ->
                "La conexión financiera no está disponible."
        }

        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = if (mensajeError != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
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
