
package com.example.gastikmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.gastikmp.database.GastiDatabase
import kotlin.math.abs

private val statsVerde = Color(0xFF59DDA4)
private val statsRojo = Color(0xFFFF8391)
private val statsAmarillo = Color(0xFFFFC857)
private val statsTarjeta = Color(0xFF172234)

private val statsMeses = listOf(
    "Enero", "Febrero", "Marzo", "Abril",
    "Mayo", "Junio", "Julio", "Agosto",
    "Septiembre", "Octubre", "Noviembre",
    "Diciembre"
)

private data class MovimientoStats(
    val fecha: String,
    val monto: Double,
    val categoriaId: String,
    val tipo: String
)

/**
 * ESTADISTICAS GASTI KMP
 *
 * Pantalla original compartida.
 *
 * Android y Desktop:
 * utilizan GastiDatabase directamente.
 *
 * Web:
 * recibe DatosHome desde consultas
 * asincronas de WebHomeDataSource.
 *
 * Se conserva la interfaz Compose.
 */
@Composable
fun StatsScreen(
    database: GastiDatabase? = null,
    datosWeb: DatosHome? = null
) {

    val correo = AuthRepository.currentUser
        ?.email.orEmpty()
        .trim()
        .lowercase()

    var periodo by remember {
        mutableStateOf("Mes")
    }

    var mes by remember {
        mutableIntStateOf(11)
    }

    var anio by remember {
        mutableIntStateOf(2026)
    }

    var diaReferencia by remember {
        mutableIntStateOf(19)
    }

    var mostrarPeriodo by remember {
        mutableStateOf(false)
    }

    var revision by remember {
        mutableIntStateOf(0)
    }

    // =====================================
    // DATOS FINANCIEROS
    // =====================================

    val datos = remember(
        database,
        correo,
        revision,
        datosWeb
    ) {

        runCatching {

            if (datosWeb != null) {

                val movimientos = datosWeb.movimientos.map {
                    MovimientoStats(
                        fecha = it.fecha.replace("/", "-"),
                        monto = it.monto,
                        categoriaId = it.categoriaId,
                        tipo = it.tipo
                    )
                }

                Pair(
                    movimientos,
                    datosWeb.categorias
                )

            } else if (
                database == null ||
                correo.isBlank()
            ) {

                Pair(
                    emptyList<MovimientoStats>(),
                    emptyMap<String, String>()
                )

            } else {

                val movimientos = database
                    .gastiDatabaseQueries
                    .obtenerTransaccionesPorUsuario(correo)
                    .executeAsList()
                    .map {
                        MovimientoStats(
                            fecha = it.fecha.replace("/", "-"),
                            monto = it.monto,
                            categoriaId = it.categoria_id,
                            tipo = it.tipo
                        )
                    }

                val categorias = database
                    .gastiDatabaseQueries
                    .obtenerCategoriasPorUsuario(correo)
                    .executeAsList()
                    .associate {
                        it.id to it.nombre
                    }

                Pair(movimientos, categorias)
            }
        }
    }

    val movimientos = datos.getOrNull()
        ?.first.orEmpty()

    val categorias = datos.getOrNull()
        ?.second.orEmpty()

    val errorDatos = datos.exceptionOrNull()?.message

    // =====================================
    // FILTRO POR PERIODO
    // =====================================

    val movimientosPeriodo = movimientos.filter { mov ->

        val partes = mov.fecha.split("-")

        if (partes.size != 3) {
            false
        } else {

            val a = partes[0].toIntOrNull()
            val m = partes[1].toIntOrNull()
            val d = partes[2].toIntOrNull()

            if (
                a == null ||
                m == null ||
                d == null
            ) {
                false
            } else {

                when (periodo) {

                    "Año" -> a == anio

                    "Mes" -> {
                        a == anio && m == mes
                    }

                    else -> {

                        val fechaMov = diasAbsolutosStats(
                            a, m, d
                        )

                        val referencia = diasAbsolutosStats(
                            anio, mes, diaReferencia
                        )

                        fechaMov in
                                (referencia - 6)..referencia
                    }
                }
            }
        }
    }

    val gastos = movimientosPeriodo.filter {
        it.tipo == "GASTO"
    }

    val ingresos = movimientosPeriodo.filter {
        it.tipo == "INGRESO"
    }

    val totalGastado = gastos.sumOf {
        it.monto
    }

    val totalIngresos = ingresos.sumOf {
        it.monto
    }

    // =====================================
    // PRESUPUESTOS DEL PERIODO
    // =====================================

    val resultadoPresupuestos = remember(
        database,
        correo,
        periodo,
        mes,
        anio,
        revision,
        datosWeb
    ) {

        runCatching {

            if (datosWeb != null) {

                datosWeb.presupuestos
                    .filter {
                        it.anio == anio &&
                                (
                                        periodo == "Año" ||
                                                it.mes == mes
                                        )
                    }
                    .sumOf {
                        it.limite
                    }

            } else if (
                database == null ||
                correo.isBlank()
            ) {

                0.0

            } else {

                database.presupuestoQueries
                    .obtenerPresupuestosPorUsuario(correo)
                    .executeAsList()
                    .filter {
                        it.anio.toInt() == anio &&
                                (
                                        periodo == "Año" ||
                                                it.mes.toInt() == mes
                                        )
                    }
                    .sumOf {
                        it.limite
                    }
            }
        }
    }

    val totalPresupuesto =
        resultadoPresupuestos.getOrDefault(0.0)

    val errorPresupuesto =
        resultadoPresupuestos.exceptionOrNull()?.message

    val porcentaje = if (totalPresupuesto > 0.0) {
        (
                totalGastado /
                        totalPresupuesto * 100
                ).toInt()
    } else {
        0
    }

    val progreso = if (totalPresupuesto > 0.0) {
        (
                totalGastado /
                        totalPresupuesto
                ).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    // =====================================
    // GASTOS POR CATEGORIA
    // =====================================

    val gastosCategoria = gastos
        .groupBy {
            it.categoriaId
        }
        .mapValues { entrada ->
            entrada.value.sumOf {
                it.monto
            }
        }
        .toList()
        .sortedByDescending {
            it.second
        }

    // =====================================
    // GASTOS POR DIA
    // =====================================

    val gastosPorDia = gastos
        .groupBy {
            it.fecha
        }
        .mapValues { entrada ->
            entrada.value.sumOf {
                it.monto
            }
        }
        .toList()
        .sortedBy {
            it.first
        }

    // =====================================
    // ULTIMOS CUATRO MESES
    // =====================================

    val ultimosMeses = (3 downTo 0).map { retroceso ->

        val indice =
            anio * 12 + (mes - 1) - retroceso

        val a = indice / 12
        val m = indice % 12 + 1

        val total = movimientos
            .filter {
                it.tipo == "GASTO" &&
                        it.fecha.startsWith(
                            "$a-${m.toString().padStart(2, '0')}-"
                        )
            }
            .sumOf {
                it.monto
            }

        Triple(a, m, total)
    }

    // =====================================
    // INTERFAZ ORIGINAL
    // =====================================

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
            bottom = 30.dp
        )
    ) {

        item {

            Text(
                "Estadísticas",
                style = MaterialTheme.typography
                    .headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // SEMANA / MES / AÑO

        item {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                listOf(
                    "Semana",
                    "Mes",
                    "Año"
                ).forEach { opcion ->

                    val seleccionado =
                        periodo == opcion

                    Button(
                        onClick = {
                            periodo = opcion
                        },

                        modifier = Modifier.weight(1f),

                        contentPadding = PaddingValues(
                            horizontal = 2.dp
                        ),

                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (seleccionado) {
                                    statsVerde
                                } else {
                                    statsTarjeta
                                },

                            contentColor =
                                if (seleccionado) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                        )
                    ) {
                        Text(opcion)
                    }
                }
            }
        }

        // SELECTOR DE PERIODO

        item {

            OutlinedButton(
                onClick = {
                    mostrarPeriodo = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    when (periodo) {

                        "Año" -> "Año: $anio"

                        "Semana" ->
                            "Últimos 7 días hasta " +
                                    "${diaReferencia.toString().padStart(2, '0')}/" +
                                    "${mes.toString().padStart(2, '0')}/$anio"

                        else ->
                            "${statsMeses[mes - 1]} $anio"
                    }
                )
            }
        }

        item {

            TextButton(
                onClick = {
                    revision++
                }
            ) {
                Text("Actualizar estadísticas")
            }
        }

        // ESTADOS DE CONEXION

        if (
            database == null &&
            datosWeb == null
        ) {

            item {
                Text("No hay conexión con SQLDelight.")
            }

        } else if (
            correo.isBlank() &&
            datosWeb == null
        ) {

            item {
                Text(
                    "Inicia sesión para consultar estadísticas."
                )
            }

        } else if (
            errorDatos != null ||
            errorPresupuesto != null
        ) {

            item {

                Text(
                    "Error al consultar datos: " +
                            (errorDatos ?: errorPresupuesto),

                    color = MaterialTheme.colorScheme.error
                )
            }

        } else {

            // =================================
            // RESUMEN
            // =================================

            item {

                StatsTarjeta {

                    Text(
                        "Resumen",
                        style = MaterialTheme.typography
                            .titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Total gastado: " +
                                monedaStats(totalGastado),

                        style = MaterialTheme.typography
                            .titleLarge,

                        fontWeight = FontWeight.Bold,
                        color = statsRojo
                    )

                    Text(
                        "Ingresos: " +
                                monedaStats(totalIngresos),

                        color = statsVerde
                    )

                    if (totalPresupuesto > 0.0) {

                        Text(
                            "Presupuesto: " +
                                    monedaStats(totalPresupuesto),

                            color = Color.LightGray
                        )

                        Text(
                            "Uso del presupuesto: $porcentaje%",
                            color = statsVerde
                        )

                        LinearProgressIndicator(
                            progress = { progreso },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),

                            color =
                                if (porcentaje > 100) {
                                    statsRojo
                                } else {
                                    statsVerde
                                }
                        )

                    } else {

                        Text(
                            "Sin presupuesto registrado en el período.",
                            color = Color.LightGray
                        )
                    }
                }
            }

            // =================================
            // GRAFICO POR FECHA
            // =================================

            item {

                StatsTarjeta {

                    Text(
                        if (periodo == "Año") {
                            "Gasto por mes"
                        } else {
                            "Gasto por día"
                        },

                        style = MaterialTheme.typography
                            .titleMedium,

                        fontWeight = FontWeight.Bold
                    )

                    if (gastos.isEmpty()) {

                        Text(
                            "No hay gastos en este período.",
                            color = Color.LightGray
                        )

                    } else {

                        val grupos =
                            if (periodo == "Año") {

                                (1..12).map { numeroMes ->

                                    val suma = gastos
                                        .filter {
                                            it.fecha.startsWith(
                                                "$anio-" +
                                                        numeroMes.toString()
                                                            .padStart(2, '0') +
                                                        "-"
                                            )
                                        }
                                        .sumOf {
                                            it.monto
                                        }

                                    statsMeses[numeroMes - 1]
                                        .take(3) to suma
                                }

                            } else {

                                gastosPorDia.map {
                                    it.first to it.second
                                }
                            }

                        val maximo = grupos
                            .maxOfOrNull {
                                it.second
                            }
                            ?.coerceAtLeast(1.0)
                            ?: 1.0

                        grupos.forEach {
                                (etiqueta, valor) ->

                            StatsBarra(
                                nombre = etiqueta,
                                valor = valor,
                                maximo = maximo
                            )
                        }
                    }
                }
            }

            // =================================
            // GRAFICO POR CATEGORIA
            // =================================

            item {

                StatsTarjeta {

                    Text(
                        "Por categoría",
                        style = MaterialTheme.typography
                            .titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Distribución de tus gastos",
                        style = MaterialTheme.typography
                            .bodySmall,
                        color = Color.LightGray
                    )

                    if (gastosCategoria.isEmpty()) {

                        Text(
                            "No hay gastos registrados en este período.",
                            color = Color.LightGray
                        )

                    } else {

                        val maximo = gastosCategoria
                            .maxOf {
                                it.second
                            }
                            .coerceAtLeast(1.0)

                        gastosCategoria.forEach {
                                (categoriaId, total) ->

                            StatsBarra(
                                nombre =
                                    categorias[categoriaId]
                                        ?: "Sin categoría",

                                valor = total,
                                maximo = maximo
                            )
                        }
                    }
                }
            }

            // =================================
            // COMPARACION MENSUAL
            // =================================

            item {

                StatsTarjeta {

                    Text(
                        "Comparación mensual",
                        style = MaterialTheme.typography
                            .titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Últimos 4 meses hasta " +
                                statsMeses[mes - 1] +
                                " de $anio",

                        color = Color.LightGray,
                        style = MaterialTheme.typography
                            .bodySmall
                    )

                    val maximo = ultimosMeses
                        .maxOf {
                            it.third
                        }
                        .coerceAtLeast(1.0)

                    ultimosMeses.forEach {
                            (a, m, total) ->

                        StatsBarra(
                            nombre =
                                "${statsMeses[m - 1].take(3)} $a",

                            valor = total,
                            maximo = maximo
                        )
                    }
                }
            }

            // =================================
            // ANALISIS DEL PERIODO
            // =================================

            item {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF123C32)
                    ),

                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(18.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            "💡 Insight del período",
                            color = statsVerde,
                            fontWeight = FontWeight.Bold
                        )

                        val mensaje = when {

                            gastos.isEmpty() ->
                                "Todavía no hay gastos para analizar."

                            totalPresupuesto > 0 &&
                                    totalGastado > totalPresupuesto ->
                                "Superaste el límite presupuestado."

                            totalPresupuesto > 0 &&
                                    porcentaje >= 80 ->
                                "Estás cerca del límite de tu presupuesto."

                            gastosCategoria.isNotEmpty() -> {

                                val mayor =
                                    gastosCategoria.first()

                                "Tu mayor gasto corresponde a " +
                                        (
                                                categorias[mayor.first]
                                                    ?: "Sin categoría"
                                                ) +
                                        ", con " +
                                        monedaStats(mayor.second) +
                                        "."
                            }

                            else ->
                                "Continúa registrando gastos."
                        }

                        Text(
                            mensaje,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // =====================================
    // SELECTOR DE FECHA Y PERIODO
    // =====================================

    if (mostrarPeriodo) {

        var mesTemporal by remember {
            mutableIntStateOf(mes)
        }

        var anioTemporal by remember {
            mutableIntStateOf(anio)
        }

        var diaTemporal by remember {
            mutableIntStateOf(diaReferencia)
        }

        AlertDialog(
            onDismissRequest = {
                mostrarPeriodo = false
            },

            title = {
                Text("Seleccionar período")
            },

            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                if (anioTemporal > 2000) {
                                    anioTemporal--
                                }
                            }
                        ) {
                            Text("◀")
                        }

                        Text(
                            anioTemporal.toString(),
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                if (anioTemporal < 2100) {
                                    anioTemporal++
                                }
                            }
                        ) {
                            Text("▶")
                        }
                    }

                    for (fila in 0..3) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {

                            for (col in 0..2) {

                                val numero =
                                    fila * 3 + col + 1

                                Button(
                                    onClick = {
                                        mesTemporal = numero
                                    },

                                    modifier = Modifier.weight(1f),

                                    contentPadding =
                                        PaddingValues(2.dp),

                                    colors =
                                        ButtonDefaults.buttonColors(

                                            containerColor =
                                                if (
                                                    mesTemporal == numero
                                                ) {
                                                    statsVerde
                                                } else {
                                                    statsTarjeta
                                                },

                                            contentColor =
                                                if (
                                                    mesTemporal == numero
                                                ) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                }
                                        )
                                ) {

                                    Text(
                                        statsMeses[numero - 1]
                                            .take(3)
                                    )
                                }
                            }
                        }
                    }

                    if (periodo == "Semana") {

                        Text("Día de referencia")

                        Slider(
                            value = diaTemporal.toFloat(),

                            onValueChange = {
                                diaTemporal = it.toInt()
                            },

                            valueRange = 1f..31f,
                            steps = 29
                        )

                        Text(
                            "Día seleccionado: $diaTemporal"
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        anio = anioTemporal
                        mes = mesTemporal

                        val diasMes = diasMesStats(
                            anioTemporal,
                            mesTemporal
                        )

                        diaReferencia =
                            diaTemporal.coerceIn(
                                1,
                                diasMes
                            )

                        mostrarPeriodo = false
                    }
                ) {
                    Text("Seleccionar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        mostrarPeriodo = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Tarjeta visual reutilizable.
 */
@Composable
private fun StatsTarjeta(
    contenido: @Composable ColumnScope.() -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = statsTarjeta
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp),

            content = contenido
        )
    }
}

/**
 * Barra horizontal basada en datos reales.
 */
@Composable
private fun StatsBarra(
    nombre: String,
    valor: Double,
    maximo: Double
) {

    val progreso = if (maximo > 0.0) {
        (
                valor / maximo
                ).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                monedaStats(valor),
                color = statsVerde,
                style = MaterialTheme.typography.bodySmall
            )
        }

        LinearProgressIndicator(
            progress = { progreso },

            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp),

            color = statsVerde,
            trackColor = Color(0xFF30394B)
        )
    }
}

/**
 * Formato de moneda colombiana.
 */
private fun monedaStats(
    valor: Double
): String {

    val entero =
        abs(valor).toLong().toString()

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

/**
 * Funciones de fecha multiplataforma.
 */
private fun bisiestoStats(
    anio: Int
): Boolean {

    return anio % 400 == 0 ||
            (
                    anio % 4 == 0 &&
                            anio % 100 != 0
                    )
}

private fun diasMesStats(
    anio: Int,
    mes: Int
): Int {

    return when (mes) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (bisiestoStats(anio)) 29 else 28
        else -> 0
    }
}

private fun diasAbsolutosStats(
    anio: Int,
    mes: Int,
    dia: Int
): Long {

    val y = (anio - 1).toLong()

    var total =
        365L * y +
                y / 4 -
                y / 100 +
                y / 400

    for (m in 1 until mes) {
        total += diasMesStats(anio, m)
    }

    return total + dia
}
