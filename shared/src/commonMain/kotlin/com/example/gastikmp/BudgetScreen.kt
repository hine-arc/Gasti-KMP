
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
import com.example.gastikmp.data.PresupuestoRepository
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.Presupuesto
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

/**
 * GASTI KMP - FASE 3
 *
 * Pantalla de presupuestos.
 *
 * Incluye:
 * - Selector visual de mes y año
 * - Creación de presupuestos
 * - Edición de límites
 * - Eliminación de presupuestos
 * - Gastos acumulados por categoría
 * - Alertas de límites
 */
@Composable
fun BudgetScreen(
    database: GastiDatabase? = null
) {

    val correo = AuthRepository.currentUser
        ?.email.orEmpty()
        .trim()
        .lowercase()

    val repositorio = remember(database) {
        database?.let {
            PresupuestoRepository(it)
        }
    }

    // Período inicial.
    // Más adelante podremos obtenerlo
    // automáticamente del reloj del sistema.
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

    val resultadoDatos = remember(
        database,
        correo,
        mesSeleccionado,
        anioSeleccionado,
        revision
    ) {

        runCatching {

            if (database == null || correo.isBlank()) {

                Triple(
                    emptyList<Presupuesto>(),
                    emptyList<Pair<String, String>>(),
                    emptyMap<String, Double>()
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

                val periodo = "$anioSeleccionado-" +
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

                Triple(
                    presupuestos,
                    categorias,
                    gastosPorCategoria
                )
            }
        }
    }

    val presupuestos = resultadoDatos
        .getOrNull()?.first.orEmpty()

    val categorias = resultadoDatos
        .getOrNull()?.second.orEmpty()

    val gastosPorCategoria = resultadoDatos
        .getOrNull()?.third.orEmpty()

    val errorDatos = resultadoDatos
        .exceptionOrNull()?.message

    val totalPresupuestado = presupuestos.sumOf {
        it.limite
    }

    val totalGastado = presupuestos.sumOf {
        gastosPorCategoria[it.categoriaId] ?: 0.0
    }

    val disponible = totalPresupuestado - totalGastado

    val porcentaje = if (totalPresupuestado > 0) {
        (totalGastado / totalPresupuestado * 100)
            .toInt()
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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

        // SELECTOR DE MES Y AÑO
        item {

            OutlinedButton(
                onClick = {
                    mostrarSelectorPeriodo = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "📅 " +
                            nombresMesesGasti[mesSeleccionado - 1] +
                            " $anioSeleccionado"
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text("Cambiar")
            }
        }

        // RESUMEN FINANCIERO
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            enabled = database != null &&
                                    correo.isNotBlank() &&
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

        if (database == null) {

            item {
                Text(
                    "No hay conexión con la base de datos."
                )
            }

        } else if (correo.isBlank()) {

            item {
                Text(
                    "Debes iniciar sesión para consultar tus presupuestos."
                )
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

        items(
            items = presupuestos,
            key = { it.id }
        ) { presupuesto ->

            val nombreCategoria = categorias
                .firstOrNull {
                    it.first == presupuesto.categoriaId
                }?.second ?: "Categoría"

            val gastado = gastosPorCategoria[
                presupuesto.categoriaId
            ] ?: 0.0

            val restante = presupuesto.limite - gastado

            val progresoCategoria = (
                    gastado / presupuesto.limite
                    ).toFloat().coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = fondoPresupuesto
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            onClick = {
                                presupuestoEditar = presupuesto
                                mensaje = ""
                                mostrarFormulario = true
                            }
                        ) {
                            Text("Editar")
                        }

                        OutlinedButton(
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
                        formatoDineroPeriodo(
                            totalPresupuestado
                        ),
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

    // VENTANA PARA ELEGIR MES Y AÑO
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

    // CREAR O EDITAR PRESUPUESTO
    if (
        mostrarFormulario &&
        repositorio != null
    ) {

        FormularioPeriodoGasti(
            categorias = categorias,
            presupuesto = presupuestoEditar,
            onCancelar = {
                mostrarFormulario = false
            },
            onGuardar = { categoriaId, limite ->

                runCatching {

                    val actual = presupuestoEditar

                    if (actual == null) {

                        repositorio.crearPresupuesto(
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

                        repositorio.actualizarPresupuesto(
                            id = actual.id,
                            usuarioEmail = correo,
                            nuevoLimite = limite
                        )
                    }

                }.onSuccess {

                    revision++
                    mostrarFormulario = false
                    mensaje = "Presupuesto guardado correctamente"

                }.onFailure {

                    mensaje = it.message ?: "Error al guardar"
                }
            }
        )
    }

    // ELIMINAR PRESUPUESTO
    presupuestoEliminar?.let { presupuesto ->

        AlertDialog(
            onDismissRequest = {
                presupuestoEliminar = null
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
                    onClick = {

                        runCatching {

                            repositorio?.eliminarPresupuesto(
                                presupuesto.id,
                                correo
                            )

                        }.onSuccess {

                            revision++
                            mensaje = "Presupuesto eliminado"

                        }.onFailure {

                            mensaje = it.message
                                ?: "Error al eliminar"
                        }

                        presupuestoEliminar = null
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
 * Selector de mes y año.
 *
 * El usuario selecciona el mes
 * pulsando un botón, sin escribir fechas.
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

                // CONTROLES DEL AÑO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
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

                // CUADRÍCULA DE LOS DOCE MESES
                for (fila in 0..3) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        for (columna in 0..2) {

                            val numeroMes = fila * 3 + columna + 1

                            val seleccionado = mes == numeroMes

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
 * Formulario para registrar o editar
 * el límite de un presupuesto.
 */
@Composable
private fun FormularioPeriodoGasti(
    categorias: List<Pair<String, String>>,
    presupuesto: Presupuesto?,
    onCancelar: () -> Unit,
    onGuardar: (String, Double) -> Unit
) {

    var categoriaId by remember(presupuesto) {
        mutableStateOf(
            presupuesto?.categoriaId
                ?: categorias.firstOrNull()
                    ?.first.orEmpty()
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
        onDismissRequest = onCancelar,
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
                        enabled = presupuesto == null &&
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
                    singleLine = true
                )

                if (categorias.isEmpty()) {

                    Text(
                        "Primero debes crear una categoría en Gastos.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {

            Button(
                enabled = categoriaId.isNotBlank() &&
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
