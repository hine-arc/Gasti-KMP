
package com.example.gastikmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.gastikmp.data.PresupuestoDataSource
import com.example.gastikmp.data.PresupuestoRepository
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.Presupuesto

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import kotlin.math.abs
import kotlin.random.Random

private val verdePresupuesto = Color(0xFF59DEA3)
private val amarilloPresupuesto = Color(0xFFFFC857)
private val rojoPresupuesto = Color(0xFFFF798B)
private val fondoPresupuesto = Color(0xFF1B2536)

private val nombresMesesGasti = listOf(
    "Enero", "Febrero", "Marzo",
    "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre",
    "Octubre", "Noviembre", "Diciembre"
)

private data class DatosPresupuestoPantalla(
    val presupuestos: List<Presupuesto>,
    val categorias: List<Pair<String, String>>,
    val gastosPorCategoria: Map<String, Double>
)

/**
 * GASTI KMP
 *
 * Pantalla original de presupuestos.
 *
 * Android y Desktop:
 * utilizan GastiDatabase y
 * PresupuestoRepository.
 *
 * Web:
 * utiliza PresupuestoDataSource
 * con consultas suspend.
 *
 * La interfaz Compose es compartida.
 */
@Composable
fun BudgetScreen(
    database: GastiDatabase? = null,
    dataSource: PresupuestoDataSource? = null,
    usuarioEmail: String = ""
) {

    val scope = rememberCoroutineScope()

    val correo = (
            usuarioEmail.takeIf { it.isNotBlank() }
                ?: AuthRepository.currentUser?.email.orEmpty()
            ).trim().lowercase()

    val repositorio = remember(database) {
        database?.let {
            PresupuestoRepository(it)
        }
    }

    var mesSeleccionado by remember {
        mutableIntStateOf(11)
    }

    var anioSeleccionado by remember {
        mutableIntStateOf(2026)
    }

    var mostrarSelectorPeriodo by remember {
        mutableStateOf(false)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var presupuestoEditar by remember {
        mutableStateOf<Presupuesto?>(null)
    }

    var presupuestoEliminar by remember {
        mutableStateOf<Presupuesto?>(null)
    }

    var revision by remember {
        mutableIntStateOf(0)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var procesando by remember {
        mutableStateOf(false)
    }

    // =========================================
    // ANDROID Y DESKTOP: CONSULTAS ORIGINALES
    // =========================================

    val resultadoOriginal = remember(
        database,
        correo,
        mesSeleccionado,
        anioSeleccionado,
        revision
    ) {

        runCatching {

            if (database == null || correo.isBlank()) {

                DatosPresupuestoPantalla(
                    presupuestos = emptyList(),
                    categorias = emptyList(),
                    gastosPorCategoria = emptyMap()
                )

            } else {

                val presupuestos = repositorio!!
                    .obtenerPresupuestosPorMes(
                        usuarioEmail = correo,
                        anio = anioSeleccionado,
                        mes = mesSeleccionado
                    )

                val categorias = database
                    .gastiDatabaseQueries
                    .obtenerCategoriasPorUsuario(correo)
                    .executeAsList()
                    .map {
                        it.id to it.nombre
                    }

                val movimientos = database
                    .gastiDatabaseQueries
                    .obtenerTransaccionesPorUsuario(correo)
                    .executeAsList()

                val periodo =
                    "$anioSeleccionado-" +
                            mesSeleccionado.toString()
                                .padStart(2, '0')

                val gastosPorCategoria = movimientos
                    .filter {
                        it.tipo == "GASTO" &&
                                it.fecha.replace("/", "-")
                                    .take(7) == periodo
                    }
                    .groupBy {
                        it.categoria_id
                    }
                    .mapValues { entrada ->
                        entrada.value.sumOf {
                            it.monto
                        }
                    }

                DatosPresupuestoPantalla(
                    presupuestos = presupuestos,
                    categorias = categorias,
                    gastosPorCategoria = gastosPorCategoria
                )
            }
        }
    }

    // =========================================
    // WEB: CONSULTAS ASINCRONAS
    // =========================================

    var datosWeb by remember(
        dataSource,
        correo,
        mesSeleccionado,
        anioSeleccionado
    ) {
        mutableStateOf<DatosPresupuestoPantalla?>(null)
    }

    var cargandoWeb by remember(dataSource, correo) {
        mutableStateOf(dataSource != null)
    }

    var errorWeb by remember(dataSource, correo) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(
        dataSource,
        correo,
        mesSeleccionado,
        anioSeleccionado,
        revision
    ) {

        if (dataSource == null) {
            return@LaunchedEffect
        }

        cargandoWeb = true
        errorWeb = null

        try {

            require(correo.isNotBlank()) {
                "Debes iniciar sesión."
            }

            val presupuestos = dataSource
                .obtenerPresupuestosPorMes(
                    usuarioEmail = correo,
                    anio = anioSeleccionado,
                    mes = mesSeleccionado
                )
                .getOrThrow()

            val categorias = dataSource
                .obtenerCategorias(correo)
                .getOrThrow()

            val gastos = dataSource
                .obtenerGastosPorCategoria(
                    usuarioEmail = correo,
                    anio = anioSeleccionado,
                    mes = mesSeleccionado
                )
                .getOrThrow()

            datosWeb = DatosPresupuestoPantalla(
                presupuestos = presupuestos,
                categorias = categorias,
                gastosPorCategoria = gastos
            )

            println(
                "GASTI PRESUPUESTOS WEB: datos cargados"
            )

        } catch (error: CancellationException) {

            throw error

        } catch (error: Exception) {

            errorWeb = error.message
                ?: "No se pudieron consultar los presupuestos."

            println(
                "GASTI PRESUPUESTOS WEB: ERROR - $errorWeb"
            )

        } finally {

            cargandoWeb = false
        }
    }

    val datos = if (dataSource != null) {
        datosWeb
    } else {
        resultadoOriginal.getOrNull()
    }

    val errorDatos = if (dataSource != null) {
        errorWeb
    } else {
        resultadoOriginal.exceptionOrNull()?.message
    }

    val presupuestos = datos?.presupuestos.orEmpty()

    val categorias = datos?.categorias.orEmpty()

    val gastosPorCategoria =
        datos?.gastosPorCategoria.orEmpty()

    val conexionDisponible =
        correo.isNotBlank() &&
                (dataSource != null || database != null)

    val totalPresupuestado = presupuestos.sumOf {
        it.limite
    }

    val totalGastado = presupuestos.sumOf {
        gastosPorCategoria[it.categoriaId] ?: 0.0
    }

    val disponible = totalPresupuestado - totalGastado

    val porcentaje = if (totalPresupuestado > 0) {
        (totalGastado / totalPresupuestado * 100).toInt()
    } else {
        0
    }

    val progreso = if (totalPresupuestado > 0) {
        (totalGastado / totalPresupuestado)
            .toFloat()
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    // =========================================
    // PANTALLA ORIGINAL
    // =========================================

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(horizontal = 16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp),

        contentPadding = PaddingValues(
            top = 24.dp,
            bottom = 28.dp
        )
    ) {

        item {

            Column {

                Text(
                    text = "Presupuestos",
                    style = MaterialTheme.typography
                        .headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Organiza tus límites mensuales",
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            }
        }

        // =====================================
        // SELECTOR DE PERIODO
        // =====================================

        item {

            OutlinedButton(
                onClick = {
                    mostrarSelectorPeriodo = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    "📅 " +
                            nombresMesesGasti[mesSeleccionado - 1] +
                            " $anioSeleccionado"
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text("Cambiar")
            }
        }

        // =====================================
        // RESUMEN DE PRESUPUESTOS
        // =====================================

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF302B1D)
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                "TOTAL PRESUPUESTADO",
                                color = Color.LightGray,
                                style = MaterialTheme.typography
                                    .labelSmall
                            )

                            Text(
                                formatoDineroPeriodo(
                                    totalPresupuestado
                                ),
                                color = Color.White,
                                style = MaterialTheme.typography
                                    .headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                presupuestoEditar = null
                                mensaje = ""
                                mostrarFormulario = true
                            },

                            enabled =
                                conexionDisponible &&
                                        !cargandoWeb &&
                                        !procesando &&
                                        errorDatos == null
                        ) {
                            Text("+ Nuevo")
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progreso },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp),

                        color = if (porcentaje > 100) {
                            rojoPresupuesto
                        } else {
                            amarilloPresupuesto
                        },

                        trackColor = Color.DarkGray
                    )

                    Row {

                        Text(
                            "$porcentaje% utilizado",
                            color = Color.LightGray,
                            style = MaterialTheme.typography
                                .bodySmall
                        )

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            formatoDineroPeriodo(disponible) +
                                    " restante",

                            color = if (disponible < 0) {
                                rojoPresupuesto
                            } else {
                                verdePresupuesto
                            },

                            style = MaterialTheme.typography
                                .bodySmall
                        )
                    }

                    HorizontalDivider(
                        color = Color.White.copy(
                            alpha = 0.15f
                        )
                    )

                    Row {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                "Gastado",
                                color = Color.LightGray
                            )

                            Text(
                                formatoDineroPeriodo(
                                    totalGastado
                                ),
                                color = rojoPresupuesto,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {

                            Text(
                                "Disponible",
                                color = Color.LightGray
                            )

                            Text(
                                formatoDineroPeriodo(
                                    disponible
                                ),

                                color = if (disponible < 0) {
                                    rojoPresupuesto
                                } else {
                                    verdePresupuesto
                                },

                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {

            Text(
                "Tus presupuestos registrados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // =====================================
        // ESTADOS DE CARGA
        // =====================================

        if (!conexionDisponible) {

            item {
                Text(
                    "No hay conexión con la base de datos."
                )
            }

        } else if (dataSource != null && cargandoWeb) {

            item {

                CircularProgressIndicator()

                Text("Cargando presupuestos...")
            }

        } else if (errorDatos != null) {

            item {

                Text(
                    "Error: $errorDatos",
                    color = MaterialTheme.colorScheme.error
                )
            }

        } else if (presupuestos.isEmpty()) {

            item {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = fondoPresupuesto
                    )
                ) {

                    Text(
                        "No tienes presupuestos para " +
                                nombresMesesGasti[mesSeleccionado - 1] +
                                " de $anioSeleccionado.",

                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        // =====================================
        // PRESUPUESTOS REGISTRADOS
        // =====================================

        items(
            items = presupuestos,
            key = { it.id }
        ) { presupuesto ->

            val nombreCategoria = categorias
                .firstOrNull {
                    it.first == presupuesto.categoriaId
                }?.second ?: "Categoría"

            val gastado =
                gastosPorCategoria[presupuesto.categoriaId]
                    ?: 0.0

            val restante = presupuesto.limite - gastado

            val progresoCategoria =
                (gastado / presupuesto.limite)
                    .toFloat()
                    .coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = fondoPresupuesto
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Row {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                nombreCategoria,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Mes: " +
                                        nombresMesesGasti[
                                            presupuesto.mes - 1
                                        ] +
                                        " ${presupuesto.anio}",

                                style = MaterialTheme.typography
                                    .bodySmall,

                                color = Color.LightGray
                            )
                        }

                        Text(
                            formatoDineroPeriodo(
                                presupuesto.limite
                            ),
                            color = verdePresupuesto,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progresoCategoria },
                        modifier = Modifier.fillMaxWidth(),

                        color = if (restante < 0) {
                            rojoPresupuesto
                        } else {
                            verdePresupuesto
                        }
                    )

                    Row {

                        Text(
                            "Gastado: " +
                                    formatoDineroPeriodo(gastado),

                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography
                                .bodySmall
                        )

                        Text(
                            formatoDineroPeriodo(restante) +
                                    " disponibles",

                            color = if (restante < 0) {
                                rojoPresupuesto
                            } else {
                                verdePresupuesto
                            },

                            style = MaterialTheme.typography
                                .bodySmall
                        )
                    }

                    if (
                        gastado >= presupuesto.limite * 0.8
                    ) {

                        Text(
                            if (restante < 0) {
                                "Has superado tu presupuesto"
                            } else {
                                "Atención: estás cerca del límite"
                            },

                            color = if (restante < 0) {
                                rojoPresupuesto
                            } else {
                                amarilloPresupuesto
                            }
                        )
                    }

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        OutlinedButton(
                            enabled = !procesando,

                            onClick = {
                                presupuestoEditar = presupuesto
                                mensaje = ""
                                mostrarFormulario = true
                            }
                        ) {
                            Text("Editar")
                        }

                        OutlinedButton(
                            enabled = !procesando,

                            onClick = {
                                presupuestoEliminar = presupuesto
                            }
                        ) {

                            Text(
                                "Eliminar",
                                color = rojoPresupuesto
                            )
                        }
                    }
                }
            }
        }

        item {

            HorizontalDivider()

            Text(
                text = "Suma total de presupuestos: " +
                        formatoDineroPeriodo(totalPresupuestado),

                color = verdePresupuesto,
                fontWeight = FontWeight.Bold,

                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (mensaje.isNotEmpty()) {

            item {

                Text(
                    mensaje,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // =========================================
    // ELEGIR MES Y AÑO
    // =========================================

    if (mostrarSelectorPeriodo) {

        SelectorPeriodoGasti(
            mesActual = mesSeleccionado,
            anioActual = anioSeleccionado,

            onCancelar = {
                mostrarSelectorPeriodo = false
            },

            onSeleccionar = { nuevoMes, nuevoAnio ->

                mesSeleccionado = nuevoMes
                anioSeleccionado = nuevoAnio

                mensaje = ""
                mostrarSelectorPeriodo = false
            }
        )
    }

    // =========================================
    // CREAR O EDITAR PRESUPUESTO
    // =========================================

    if (
        mostrarFormulario &&
        (dataSource != null || repositorio != null)
    ) {

        FormularioPeriodoGasti(
            categorias = categorias,
            presupuesto = presupuestoEditar,

            procesando = procesando,

            onCancelar = {
                if (!procesando) {
                    mostrarFormulario = false
                }
            },

            onGuardar = { categoriaId, limite ->

                if (!procesando) {

                    procesando = true
                    mensaje = ""

                    scope.launch {

                        try {

                            val actual = presupuestoEditar

                            val resultado: Result<Unit> =
                                if (dataSource != null) {

                                    if (actual == null) {

                                        dataSource.crearPresupuesto(
                                            Presupuesto(
                                                id = "p-${Random.nextLong()}-" +
                                                        Random.nextLong(),

                                                usuarioEmail = correo,
                                                categoriaId = categoriaId,
                                                limite = limite,

                                                anio = anioSeleccionado,
                                                mes = mesSeleccionado
                                            )
                                        )

                                    } else {

                                        dataSource.actualizarPresupuesto(
                                            id = actual.id,
                                            usuarioEmail = correo,
                                            nuevoLimite = limite
                                        )
                                    }

                                } else {

                                    runCatching {

                                        if (actual == null) {

                                            repositorio!!.crearPresupuesto(
                                                Presupuesto(
                                                    id = "p-${Random.nextLong()}-" +
                                                            Random.nextLong(),

                                                    usuarioEmail = correo,
                                                    categoriaId = categoriaId,
                                                    limite = limite,

                                                    anio = anioSeleccionado,
                                                    mes = mesSeleccionado
                                                )
                                            )

                                        } else {

                                            repositorio!!.actualizarPresupuesto(
                                                id = actual.id,
                                                usuarioEmail = correo,
                                                nuevoLimite = limite
                                            )
                                        }

                                        Unit
                                    }
                                }

                            resultado.getOrThrow()

                            revision++
                            mostrarFormulario = false
                            presupuestoEditar = null

                            mensaje =
                                "Presupuesto guardado correctamente"

                        } catch (error: CancellationException) {

                            throw error

                        } catch (error: Exception) {

                            mensaje = error.message
                                ?: "Error al guardar presupuesto"

                        } finally {

                            procesando = false
                        }
                    }
                }
            }
        )
    }

    // =========================================
    // ELIMINAR PRESUPUESTO
    // =========================================

    presupuestoEliminar?.let { presupuesto ->

        AlertDialog(
            onDismissRequest = {
                if (!procesando) {
                    presupuestoEliminar = null
                }
            },

            title = {
                Text("Eliminar presupuesto")
            },

            text = {
                Text(
                    "¿Confirmas que deseas eliminar este presupuesto?"
                )
            },

            confirmButton = {

                TextButton(
                    enabled = !procesando,

                    onClick = {

                        if (!procesando) {

                            procesando = true

                            scope.launch {

                                try {

                                    val resultado: Result<Unit> =
                                        if (dataSource != null) {

                                            dataSource.eliminarPresupuesto(
                                                presupuesto.id,
                                                correo
                                            )

                                        } else {

                                            runCatching {

                                                repositorio!!
                                                    .eliminarPresupuesto(
                                                        presupuesto.id,
                                                        correo
                                                    )

                                                Unit
                                            }
                                        }

                                    resultado.getOrThrow()

                                    revision++

                                    mensaje =
                                        "Presupuesto eliminado"

                                    presupuestoEliminar = null

                                } catch (
                                    error: CancellationException
                                ) {

                                    throw error

                                } catch (error: Exception) {

                                    mensaje = error.message
                                        ?: "Error al eliminar"

                                    presupuestoEliminar = null

                                } finally {

                                    procesando = false
                                }
                            }
                        }
                    }
                ) {

                    Text(
                        "Eliminar",
                        color = rojoPresupuesto
                    )
                }
            },

            dismissButton = {

                TextButton(
                    enabled = !procesando,

                    onClick = {
                        presupuestoEliminar = null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Selector visual de mes y año.
 */
@Composable
private fun SelectorPeriodoGasti(
    mesActual: Int,
    anioActual: Int,
    onCancelar: () -> Unit,
    onSeleccionar: (Int, Int) -> Unit
) {

    var mes by remember {
        mutableIntStateOf(mesActual)
    }

    var anio by remember {
        mutableIntStateOf(anioActual)
    }

    AlertDialog(
        onDismissRequest = onCancelar,

        title = {
            Text("Seleccionar período")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    IconButton(
                        onClick = {
                            if (anio > 2000) anio--
                        }
                    ) {
                        Text("◀")
                    }

                    Text(
                        anio.toString(),
                        style = MaterialTheme.typography
                            .titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            if (anio < 2100) anio++
                        }
                    ) {
                        Text("▶")
                    }
                }

                for (fila in 0..3) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        for (columna in 0..2) {

                            val numeroMes =
                                fila * 3 + columna + 1

                            val seleccionado =
                                mes == numeroMes

                            if (seleccionado) {

                                Button(
                                    onClick = {
                                        mes = numeroMes
                                    },

                                    modifier = Modifier.weight(1f),

                                    contentPadding = PaddingValues(
                                        horizontal = 2.dp,
                                        vertical = 4.dp
                                    )
                                ) {

                                    Text(
                                        nombresMesesGasti[
                                            numeroMes - 1
                                        ].take(3)
                                    )
                                }

                            } else {

                                OutlinedButton(
                                    onClick = {
                                        mes = numeroMes
                                    },

                                    modifier = Modifier.weight(1f),

                                    contentPadding = PaddingValues(
                                        horizontal = 2.dp,
                                        vertical = 4.dp
                                    )
                                ) {

                                    Text(
                                        nombresMesesGasti[
                                            numeroMes - 1
                                        ].take(3)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {
                    onSeleccionar(mes, anio)
                }
            ) {
                Text("Seleccionar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Formulario de creación y edición.
 *
 * Funciona con Android, Desktop y Web.
 */
@Composable
private fun FormularioPeriodoGasti(
    categorias: List<Pair<String, String>>,
    presupuesto: Presupuesto?,
    procesando: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (String, Double) -> Unit
) {

    var categoriaId by remember(presupuesto) {
        mutableStateOf(
            presupuesto?.categoriaId
                ?: categorias.firstOrNull()?.first.orEmpty()
        )
    }

    var limiteTexto by remember(presupuesto) {
        mutableStateOf(
            presupuesto?.limite
                ?.toLong()
                ?.toString()
                .orEmpty()
        )
    }

    var menuAbierto by remember {
        mutableStateOf(false)
    }

    val limite = limiteTexto.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = {
            if (!procesando) {
                onCancelar()
            }
        },

        title = {

            Text(
                if (presupuesto == null) {
                    "Nuevo presupuesto"
                } else {
                    "Editar presupuesto"
                }
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text("Categoría")

                Box {

                    OutlinedButton(
                        enabled =
                            !procesando &&
                                    presupuesto == null &&
                                    categorias.isNotEmpty(),

                        onClick = {
                            menuAbierto = true
                        }
                    ) {

                        Text(
                            categorias.firstOrNull {
                                it.first == categoriaId
                            }?.second ?: "Sin categorías"
                        )
                    }

                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = {
                            menuAbierto = false
                        }
                    ) {

                        categorias.forEach { categoria ->

                            DropdownMenuItem(
                                text = {
                                    Text(categoria.second)
                                },

                                onClick = {
                                    categoriaId = categoria.first
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limiteTexto,

                    onValueChange = {
                        limiteTexto = it
                    },

                    label = {
                        Text("Límite de gasto")
                    },

                    enabled = !procesando,
                    singleLine = true
                )

                if (categorias.isEmpty()) {

                    Text(
                        "Primero debes crear una categoría en Gastos.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (procesando) {

                    CircularProgressIndicator()

                    Text("Guardando presupuesto...")
                }
            }
        },

        confirmButton = {

            Button(
                enabled =
                    !procesando &&
                            categoriaId.isNotBlank() &&
                            limite != null &&
                            limite.isFinite() &&
                            limite > 0.0,

                onClick = {

                    if (limite != null) {

                        onGuardar(
                            categoriaId,
                            limite
                        )
                    }
                }
            ) {

                Text("Guardar")
            }
        },

        dismissButton = {

            TextButton(
                enabled = !procesando,
                onClick = onCancelar
            ) {

                Text("Cancelar")
            }
        }
    )
}

/**
 * Formato monetario en pesos colombianos.
 */
private fun formatoDineroPeriodo(
    valor: Double
): String {

    val entero = abs(valor).toLong().toString()

    val numero = entero
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return if (valor < 0) {
        "-$ $numero"
    } else {
        "$ $numero"
    }
}
