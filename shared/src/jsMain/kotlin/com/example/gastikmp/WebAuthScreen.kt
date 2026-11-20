
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
import com.example.gastikmp.data.WebHomeDataSource
import com.example.gastikmp.data.WebPresupuestoDataSource
import com.example.gastikmp.data.WebIndexedDbStorage
import com.example.gastikmp.data.WebPasswordSecurity
import com.example.gastikmp.data.WebSqlDelightDriverFactory

import com.example.gastikmp.navigation.LocalGastiNavigate
import com.example.gastikmp.navigation.MainScaffold
import com.example.gastikmp.presentation.ExpensesScreen

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive

/**
 * Conexion financiera de una sesion Web.
 *
 * Inicio, Gastos y Presupuestos comparten
 * el mismo controlador SQLite.
 */
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
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Reutiliza las pantallas compartidas:
 *
 * - LoginScreen
 * - RegisterScreen
 * - MainScaffold
 * - HomeScreen
 * - ExpensesScreen
 * - BudgetScreen
 *
 * La autenticacion Web permanece
 * en IndexedDB.
 *
 * Los datos financieros se almacenan
 * mediante SQLite Web.
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

    if (
        usuarioActual != null &&
        correoActual != null
    ) {

        val emailSesion = correoActual!!
        val nombreSesion = usuarioActual ?: ""

        // =====================================
        // CONTROLADOR SQLITE DE LA SESION
        // =====================================

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

            } catch (
                error: CancellationException
            ) {

                throw error

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

        // =====================================
        // REPOSITORIO COMPARTIDO DE GASTOS
        // =====================================

        val fuenteGastos = remember(sesion.driver) {

            sesion.driver?.let { driver ->
                WebGastoDataSource(driver)
            }
        }

        // =====================================
        // REPOSITORIO DE PRESUPUESTOS
        // =====================================

        val fuentePresupuestos =
            remember(sesion.driver) {

                sesion.driver?.let { driver ->
                    WebPresupuestoDataSource(driver)
                }
            }

        // =====================================
        // NAVEGACION PRINCIPAL DE GASTI
        // =====================================

        MainScaffold(
            database = null,

            onLogout = {

                usuarioActual = null
                correoActual = null
                mostrarRegistro = false
            },

            webContent = { screen ->

                val driverActual = sesion.driver

                when {

                    // =================================
                    // INICIO COMPARTIDO
                    // =================================

                    screen == Screen.Home &&
                            driverActual != null -> {

                        InicioFinancieroWeb(
                            driver = driverActual,
                            email = emailSesion,
                            nombre = nombreSesion
                        )
                    }

                    // =================================
                    // GASTOS COMPARTIDOS
                    // =================================

                    screen == Screen.Expenses &&
                            fuenteGastos != null -> {

                        ExpensesScreen(
                            database = null,
                            dataSource = fuenteGastos,
                            usuarioEmail = emailSesion
                        )
                    }

                    // =================================
                    // PRESUPUESTOS COMPARTIDOS
                    // =================================

                    screen == Screen.Budget &&
                            fuentePresupuestos != null -> {

                        BudgetScreen(
                            database = null,
                            dataSource = fuentePresupuestos,
                            usuarioEmail = emailSesion
                        )
                    }

                    // =================================
                    // PREPARACION DE SQLITE
                    // =================================

                    (
                            screen == Screen.Home ||
                                    screen == Screen.Expenses ||
                                    screen == Screen.Budget
                            ) && sesion.preparando -> {

                        PantallaCargandoWeb(
                            mensaje =
                                "Preparando los datos de Gasti..."
                        )
                    }

                    // =================================
                    // ESTADISTICAS PENDIENTES
                    // =================================

                    else -> {

                        ContenidoFinancieroWebPendiente(
                            screen = screen,
                            nombre = nombreSesion,
                            preparando = sesion.preparando,
                            conexionPreparada =
                                driverActual != null,
                            mensajeError = sesion.mensajeError
                        )
                    }
                }
            }
        )

    } else if (mostrarRegistro) {

        // =====================================
        // REGISTRO WEB ORIGINAL
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

                    require(
                        nombreNormalizado.isNotBlank()
                    ) {
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

                    // Guardamos hash y sal;
                    // nunca la contrasena original.

                    almacenamiento.guardar(
                        "$claveBase:hash",
                        protegida.passwordHash
                    )

                    almacenamiento.guardar(
                        "$claveBase:salt",
                        protegida.passwordSalt
                    )

                    // Guardamos el nombre al final.

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
        // LOGIN WEB ORIGINAL
        // =====================================

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
 * Consulta el resumen financiero Web.
 *
 * Reutiliza HomeScreen original.
 *
 * Conecta las acciones rapidas
 * de Inicio con MainScaffold.
 */
@Composable
private fun InicioFinancieroWeb(
    driver: SqlDriver,
    email: String,
    nombre: String
) {

    val navegar = LocalGastiNavigate.current

    var datos by remember(driver, email) {
        mutableStateOf<DatosHome?>(null)
    }

    var cargando by remember(driver, email) {
        mutableStateOf(true)
    }

    var mensajeError by remember(driver, email) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(driver, email) {

        cargando = true
        mensajeError = null

        try {

            println(
                "GASTI HOME WEB: consultando datos financieros..."
            )

            val repositorio =
                WebHomeDataSource(driver)

            val resultado =
                repositorio.cargarDatos(email)
                    .getOrThrow()

            if (isActive) {

                datos = DatosHome(

                    movimientos =
                        resultado.movimientos.map {

                            MovimientoHome(
                                id = it.id,
                                descripcion = it.descripcion,
                                monto = it.monto,
                                fecha = it.fecha,
                                categoriaId = it.categoriaId,
                                tipo = it.tipo
                            )
                        },

                    presupuestos =
                        resultado.presupuestos.map {

                            PresupuestoHome(
                                categoriaId = it.categoriaId,
                                limite = it.limite,
                                anio = it.anio,
                                mes = it.mes
                            )
                        },

                    categorias = resultado.categorias
                )

                println(
                    "GASTI HOME WEB: datos financieros cargados"
                )
            }

        } catch (
            error: CancellationException
        ) {

            throw error

        } catch (error: Exception) {

            mensajeError =
                error.message
                    ?: "No se pudo consultar el resumen."

            println(
                "GASTI HOME WEB: ERROR - " +
                        mensajeError
            )

        } finally {

            cargando = false
        }
    }

    when {

        cargando -> {

            PantallaCargandoWeb(
                mensaje =
                    "Cargando tu resumen financiero..."
            )
        }

        mensajeError != null -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                verticalArrangement =
                    Arrangement.Center,

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "No se pudo cargar Inicio",
                    style =
                        MaterialTheme.typography.titleLarge,
                    color =
                        MaterialTheme.colorScheme.error
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = mensajeError
                        ?: "Error desconocido.",

                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            }
        }

        datos != null -> {

            HomeScreen(
                database = null,
                datosWeb = datos,
                nombreWeb = nombre,

                onIrAGastos = {
                    navegar(Screen.Expenses)
                },

                onIrAPresupuestos = {
                    navegar(Screen.Budget)
                }
            )
        }

        else -> {

            PantallaCargandoWeb(
                mensaje =
                    "Esperando datos financieros..."
            )
        }
    }
}

/**
 * Pantalla temporal de carga.
 */
@Composable
private fun PantallaCargandoWeb(
    mensaje: String
) {

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

        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Pantallas pendientes.
 *
 * Actualmente queda Estadisticas
 * por conectar con SQLite Web.
 */
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
            style =
                MaterialTheme.typography.headlineMedium,
            color =
                MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (screen == Screen.Home) {

            Text(
                text = "¡Bienvenido, $nombre!",
                style =
                    MaterialTheme.typography.titleLarge,
                color =
                    MaterialTheme.colorScheme.onBackground
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
                "Esta pantalla financiera estará disponible próximamente."

            else ->
                "La conexión financiera no está disponible."
        }

        Text(
            text = mensaje,
            style =
                MaterialTheme.typography.bodyMedium,

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
            text =
                "Tus cuentas registradas permanecen en IndexedDB.",

            style = MaterialTheme.typography.bodySmall,

            color = MaterialTheme.colorScheme
                .onSurfaceVariant
        )
    }
}
