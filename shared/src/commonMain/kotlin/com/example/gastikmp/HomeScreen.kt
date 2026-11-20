
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gastikmp.database.GastiDatabase
import kotlin.math.abs

// Colores del diseño Gasti.
private val homeVerde = Color(0xFF63DFA6)
private val homeRojo = Color(0xFFFF8D9C)
private val homeAzul = Color(0xFFA8BDF4)
private val homeTarjeta = Color(0xFF1C293A)

private val mesesHome = listOf(
    "Enero", "Febrero", "Marzo", "Abril",
    "Mayo", "Junio", "Julio", "Agosto",
    "Septiembre", "Octubre", "Noviembre",
    "Diciembre"
)

data class MovimientoHome(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoriaId: String,
    val tipo: String
)

data class PresupuestoHome(
    val categoriaId: String,
    val limite: Double,
    val anio: Int,
    val mes: Int
)

data class DatosHome(
    val movimientos: List<MovimientoHome>,
    val presupuestos: List<PresupuestoHome>,
    val categorias: Map<String, String>
)

/**
 * GASTI KMP
 *
 * Pantalla Inicio compartida.
 *
 * Android y Desktop:
 * consultan GastiDatabase directamente.
 *
 * Web:
 * puede recibir DatosHome desde
 * consultas SQLDelight asincronas.
 *
 * Se conserva el diseño original.
 */
@Composable
fun HomeScreen(
    database: GastiDatabase? = null,
    onIrAGastos: () -> Unit = {},
    onIrAPresupuestos: () -> Unit = {},
    datosWeb: DatosHome? = null,
    nombreWeb: String? = null
) {

    val usuario = AuthRepository.currentUser

    val correo = usuario?.email
        .orEmpty()
        .trim()
        .lowercase()

    val nombreUsuario = nombreWeb
        ?.takeIf { it.isNotBlank() }
        ?: usuario?.name
            ?.takeIf { it.isNotBlank() }
        ?: "Usuario"

    // Periodo inicial del proyecto.
    var mes by remember {
        mutableIntStateOf(11)
    }

    var anio by remember {
        mutableIntStateOf(2026)
    }

    var revision by remember {
        mutableIntStateOf(0)
    }

    // Android/Desktop consultan SQLDelight.
    // Web utiliza los datos proporcionados.
    val resultado = remember(
        database,
        correo,
        revision,
        datosWeb
    ) {

        runCatching {

            if (datosWeb != null) {

                datosWeb

            } else if (
                database == null ||
                correo.isBlank()
            ) {

                DatosHome(
                    movimientos = emptyList(),
                    presupuestos = emptyList(),
                    categorias = emptyMap()
                )

            } else {

                val movimientos = database
                    .gastiDatabaseQueries
                    .obtenerTransaccionesPorUsuario(correo)
                    .executeAsList()
                    .map { registro ->

                        MovimientoHome(
                            id = registro.id,
                            descripcion = registro.descripcion,
                            monto = registro.monto,
                            fecha = registro.fecha.replace("/", "-"),
                            categoriaId = registro.categoria_id,
                            tipo = registro.tipo
                        )
                    }

                val presupuestos = database
                    .presupuestoQueries
                    .obtenerPresupuestosPorUsuario(correo)
                    .executeAsList()
                    .map { registro ->

                        PresupuestoHome(
                            categoriaId = registro.categoria_id,
                            limite = registro.limite,
                            anio = registro.anio.toInt(),
                            mes = registro.mes.toInt()
                        )
                    }

                val categorias = database
                    .gastiDatabaseQueries
                    .obtenerCategoriasPorUsuario(correo)
                    .executeAsList()
                    .associate {
                        it.id to it.nombre
                    }

                DatosHome(
                    movimientos = movimientos,
                    presupuestos = presupuestos,
                    categorias = categorias
                )
            }
        }
    }

    val datos = resultado.getOrNull()

    val error = resultado.exceptionOrNull()?.message

    val periodo = "$anio-" +
            mes.toString().padStart(2, '0') +
            "-"

    // Presupuestos del mes seleccionado.
    val presupuestosMes = datos
        ?.presupuestos
        .orEmpty()
        .filter {
            it.anio == anio && it.mes == mes
        }

    // Movimientos del mismo mes.
    val movimientosMes = datos
        ?.movimientos
        .orEmpty()
        .filter {
            it.fecha.startsWith(periodo)
        }

    val gastosMes = movimientosMes.filter {
        it.tipo == "GASTO"
    }

    val ingresosMes = movimientosMes.filter {
        it.tipo == "INGRESO"
    }

    val totalGastos = gastosMes.sumOf {
        it.monto
    }

    val totalIngresos = ingresosMes.sumOf {
        it.monto
    }

    val balance = totalIngresos - totalGastos

    val totalPresupuestado = presupuestosMes.sumOf {
        it.limite
    }

    val categoriasConPresupuesto = presupuestosMes
        .map { it.categoriaId }
        .toSet()

    val gastosPresupuestados = gastosMes
        .filter {
            it.categoriaId in categoriasConPresupuesto
        }
        .sumOf {
            it.monto
        }

    val disponible = totalPresupuestado -
            gastosPresupuestados

    val porcentaje = if (totalPresupuestado > 0) {
        (
                gastosPresupuestados /
                        totalPresupuestado * 100
                ).toInt()
    } else {
        0
    }

    val progreso = if (totalPresupuestado > 0) {
        (
                gastosPresupuestados /
                        totalPresupuestado
                ).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    // Cinco gastos mas recientes del usuario.
    val recientes = datos
        ?.movimientos
        .orEmpty()
        .filter {
            it.tipo == "GASTO"
        }
        .sortedByDescending {
            it.fecha
        }
        .take(5)

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

        // ENCABEZADO
        item {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Text(
                    text = "Gasti",
                    style = MaterialTheme.typography
                        .headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = homeVerde
                )

                Text(
                    text = "Hola, $nombreUsuario 👋",
                    style = MaterialTheme.typography
                        .titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Este es tu resumen financiero",
                    style = MaterialTheme.typography
                        .bodyMedium,
                    color = MaterialTheme.colorScheme
                        .onSurfaceVariant
                )
            }
        }

        // CAMBIAR MES Y ANO
        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                IconButton(
                    onClick = {

                        if (mes == 1) {
                            mes = 12
                            anio--
                        } else {
                            mes--
                        }
                    }
                ) {
                    Text("◀")
                }

                Text(
                    text = "${mesesHome[mes - 1]} $anio",
                    style = MaterialTheme.typography
                        .titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {

                        if (mes == 12) {
                            mes = 1
                            anio++
                        } else {
                            mes++
                        }
                    }
                ) {
                    Text("▶")
                }
            }
        }

        if (database == null && datosWeb == null) {

            item {
                Text(
                    "La base de datos todavía no está conectada.",
                    color = MaterialTheme.colorScheme.error
                )
            }

        } else if (correo.isBlank() && datosWeb == null) {

            item {
                Text(
                    "Inicia sesión para consultar tus finanzas."
                )
            }

        } else if (error != null) {

            item {

                Text(
                    "No se pudieron consultar los datos: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

        } else {

            // TARJETA PRINCIPAL DEL PRESUPUESTO
            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = homeTarjeta
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(13.dp)
                    ) {

                        Text(
                            "DISPONIBLE EN PRESUPUESTO",
                            color = Color.LightGray,
                            style = MaterialTheme.typography
                                .labelSmall
                        )

                        Text(
                            if (presupuestosMes.isEmpty()) {
                                "Sin presupuesto"
                            } else {
                                dineroHome(disponible)
                            },
                            style = MaterialTheme.typography
                                .headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (disponible < 0) {
                                homeRojo
                            } else {
                                homeVerde
                            }
                        )

                        Text(
                            if (presupuestosMes.isEmpty()) {
                                "Crea un presupuesto para comenzar."
                            } else {
                                "$porcentaje% de los límites utilizados"
                            },
                            color = Color.LightGray,
                            style = MaterialTheme.typography
                                .bodySmall
                        )

                        LinearProgressIndicator(
                            progress = { progreso },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp),
                            color = if (disponible < 0) {
                                homeRojo
                            } else {
                                homeVerde
                            },
                            trackColor = Color(0xFF344155)
                        )

                        HorizontalDivider(
                            color = Color.White.copy(
                                alpha = 0.12f
                            )
                        )

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            HomeDato(
                                titulo = "Suma presupuestos",
                                valor = dineroHome(
                                    totalPresupuestado
                                ),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            HomeDato(
                                titulo = "Gastos presupuestados",
                                valor = dineroHome(
                                    gastosPresupuestados
                                ),
                                color = homeRojo,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // RESUMEN DE MOVIMIENTOS
            item {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = homeTarjeta
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp)
                        ) {

                            Text(
                                "Ingresos",
                                color = Color.LightGray,
                                style = MaterialTheme.typography
                                    .bodySmall
                            )

                            Text(
                                dineroHome(totalIngresos),
                                color = homeVerde,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = homeTarjeta
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(5.dp)
                        ) {

                            Text(
                                "Gastos totales",
                                color = Color.LightGray,
                                style = MaterialTheme.typography
                                    .bodySmall
                            )

                            Text(
                                dineroHome(totalGastos),
                                color = homeRojo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = homeTarjeta
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            "Balance de movimientos",
                            style = MaterialTheme.typography
                                .bodyMedium
                        )

                        Text(
                            dineroHome(balance),
                            color = if (balance < 0) {
                                homeRojo
                            } else {
                                homeVerde
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ACCIONES RAPIDAS
            item {

                Text(
                    "Acciones rápidas",
                    style = MaterialTheme.typography
                        .titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Button(
                        onClick = onIrAGastos,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 6.dp,
                            vertical = 14.dp
                        )
                    ) {
                        Text("+ Registrar gasto")
                    }

                    OutlinedButton(
                        onClick = onIrAPresupuestos,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = 6.dp,
                            vertical = 14.dp
                        )
                    ) {
                        Text("Presupuestos")
                    }
                }
            }

            // GASTOS RECIENTES
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        "Gastos recientes",
                        style = MaterialTheme.typography
                            .titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = onIrAGastos
                    ) {
                        Text("Ver todos")
                    }
                }
            }

            if (recientes.isEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = homeTarjeta
                        )
                    ) {

                        Text(
                            "Todavía no tienes gastos registrados.",
                            modifier = Modifier.padding(18.dp),
                            color = Color.LightGray
                        )
                    }
                }

            } else {

                items(
                    items = recientes,
                    key = { it.id }
                ) { movimiento ->

                    val categoria = datos?.categorias
                        ?.get(movimiento.categoriaId)
                        ?: "Sin categoría"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = homeTarjeta
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement =
                                    Arrangement.spacedBy(3.dp)
                            ) {

                                Text(
                                    categoria,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    movimiento.descripcion,
                                    style = MaterialTheme.typography
                                        .bodySmall,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    movimiento.fecha,
                                    style = MaterialTheme.typography
                                        .labelSmall,
                                    color = Color.LightGray
                                )
                            }

                            Text(
                                "-${dineroHome(movimiento.monto)}",
                                color = homeRojo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ACTUALIZAR RESUMEN
            item {

                TextButton(
                    onClick = {
                        revision++
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar resumen")
                }
            }
        }
    }
}

/**
 * Componente pequeno para importes.
 */
@Composable
private fun HomeDato(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        Text(
            titulo,
            color = Color.LightGray,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            valor,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formato monetario en pesos colombianos.
 */
private fun dineroHome(valor: Double): String {

    val entero = abs(valor).toLong().toString()

    val numero = entero.reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return if (valor < 0) {
        "-$ $numero"
    } else {
        "$ $numero"
    }
}
