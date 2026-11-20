
package com.example.gastikmp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.gastikmp.SelectorFechaGasti
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.data.GastoDataSource
import com.example.gastikmp.domain.gastos.*
import com.example.gastikmp.presentation.viewmodel.GastoViewModel
import com.example.gastikmp.presentation.viewmodel.GastoViewModelAsync
import com.example.gastikmp.presentation.viewmodel.GastoUiState

import kotlinx.coroutines.launch

/**
 * GASTI KMP
 *
 * Pantalla original de gastos e ingresos.
 *
 * Android y Desktop:
 * utilizan GastiDatabase y GastoViewModel.
 *
 * Web:
 * podrá utilizar GastoDataSource y
 * GastoViewModelAsync.
 *
 * Se conserva la misma interfaz Compose.
 */
@Composable
fun ExpensesScreen(
    database: GastiDatabase? = null,
    dataSource: GastoDataSource? = null,
    usuarioEmail: String = ""
) {

    val vmOriginal = remember(database, dataSource) {
        if (dataSource == null && database != null) {
            GastoViewModel(database)
        } else {
            null
        }
    }

    val vmAsync = remember(dataSource, usuarioEmail) {
        if (dataSource != null) {
            GastoViewModelAsync(
                dataSource = dataSource,
                usuarioEmail = usuarioEmail
            )
        } else {
            null
        }
    }

    val state = vmAsync?.uiState
        ?: vmOriginal?.uiState
        ?: GastoUiState(
            error = "La base de datos no está conectada."
        )

    val scope = rememberCoroutineScope()

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var mostrarCategorias by remember {
        mutableStateOf(false)
    }

    var movimientoEditar by remember {
        mutableStateOf<Transaccion?>(null)
    }

    LaunchedEffect(vmOriginal, vmAsync) {
        when {
            vmAsync != null -> vmAsync.cargarDatos()
            vmOriginal != null -> vmOriginal.cargarDatos()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Gastos e ingresos",
            style = MaterialTheme.typography.headlineMedium
        )

        // RESUMEN DE INGRESOS Y GASTOS
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f)
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Ingresos")

                    Text(
                        "$ ${state.transacciones.filter {
                            it.tipo == TipoTransaccion.INGRESO
                        }.sumOf { it.monto }}"
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f)
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Gastos")

                    Text(
                        "$ ${state.transacciones.filter {
                            it.tipo == TipoTransaccion.GASTO
                        }.sumOf { it.monto }}"
                    )
                }
            }
        }

        // BALANCE
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                val ingresos = state.transacciones
                    .filter {
                        it.tipo == TipoTransaccion.INGRESO
                    }
                    .sumOf {
                        it.monto
                    }

                val gastos = state.transacciones
                    .filter {
                        it.tipo == TipoTransaccion.GASTO
                    }
                    .sumOf {
                        it.monto
                    }

                Text("Balance")

                Text(
                    "$ ${ingresos - gastos}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // BOTONES PRINCIPALES
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    movimientoEditar = null
                    mostrarFormulario = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Registrar movimiento")
            }

            OutlinedButton(
                onClick = {
                    mostrarCategorias = true
                }
            ) {
                Text("Categorías")
            }
        }

        // MENSAJES
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.mensaje?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Movimientos registrados",
            style = MaterialTheme.typography.titleLarge
        )

        // LISTADO DE MOVIMIENTOS
        if (state.cargando) {

            CircularProgressIndicator()

        } else if (state.transacciones.isEmpty()) {

            Text(
                "Todavía no tienes movimientos registrados."
            )

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    state.transacciones,
                    key = { it.id }
                ) { movimiento ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {

                            Text(
                                text = movimiento.descripcion,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Fecha: ${movimiento.fecha}"
                            )

                            Text(
                                "Tipo: ${movimiento.tipo}"
                            )

                            Text(
                                "Valor: $ ${movimiento.monto}"
                            )

                            val categoria = state.categorias
                                .firstOrNull {
                                    it.id == movimiento.categoriaId
                                }?.nombre ?: "Sin categoría"

                            Text(
                                "Categoría: $categoria"
                            )

                            Row {

                                TextButton(
                                    onClick = {
                                        movimientoEditar = movimiento
                                        mostrarFormulario = true
                                    }
                                ) {
                                    Text("Editar")
                                }

                                TextButton(
                                    onClick = {

                                        scope.launch {

                                            when {
                                                vmAsync != null -> {
                                                    vmAsync.eliminarTransaccion(
                                                        movimiento.id
                                                    )
                                                }

                                                vmOriginal != null -> {
                                                    vmOriginal.eliminarTransaccion(
                                                        movimiento.id
                                                    )
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // FORMULARIO ORIGINAL DE REGISTRO Y EDICIÓN
    if (mostrarFormulario) {

        TransactionForm(
            categorias = state.categorias,
            initial = movimientoEditar,

            onDismiss = {
                mostrarFormulario = false
                movimientoEditar = null
            },

            onSave = {
                    descripcion,
                    monto,
                    fecha,
                    categoriaId,
                    tipo ->

                when {

                    vmAsync != null -> {

                        if (movimientoEditar == null) {

                            vmAsync.registrarTransaccion(
                                descripcion,
                                monto,
                                fecha,
                                categoriaId,
                                tipo
                            )

                        } else {

                            vmAsync.actualizarTransaccion(
                                movimientoEditar!!.copy(
                                    descripcion = descripcion,
                                    monto = monto,
                                    fecha = fecha,
                                    categoriaId = categoriaId,
                                    tipo = tipo
                                )
                            )
                        }
                    }

                    vmOriginal != null -> {

                        if (movimientoEditar == null) {

                            vmOriginal.registrarTransaccion(
                                descripcion,
                                monto,
                                fecha,
                                categoriaId,
                                tipo
                            )

                        } else {

                            vmOriginal.actualizarTransaccion(
                                movimientoEditar!!.copy(
                                    descripcion = descripcion,
                                    monto = monto,
                                    fecha = fecha,
                                    categoriaId = categoriaId,
                                    tipo = tipo
                                )
                            )
                        }
                    }

                    else -> ResultadoOperacion.Error(
                        "La base de datos no está conectada."
                    )
                }
            }
        )
    }

    // ADMINISTRACIÓN DE CATEGORÍAS
    if (mostrarCategorias) {

        CategoryDialog(
            categorias = state.categorias,

            onRegistrar = { nombre ->

                when {
                    vmAsync != null ->
                        vmAsync.registrarCategoria(nombre)

                    vmOriginal != null ->
                        vmOriginal.registrarCategoria(nombre)

                    else -> ResultadoOperacion.Error(
                        "La base de datos no está conectada."
                    )
                }
            },

            onEliminar = { id ->

                when {
                    vmAsync != null ->
                        vmAsync.eliminarCategoria(id)

                    vmOriginal != null ->
                        vmOriginal.eliminarCategoria(id)

                    else -> ResultadoOperacion.Error(
                        "La base de datos no está conectada."
                    )
                }
            },

            onDismiss = {
                mostrarCategorias = false
            }
        )
    }
}

/**
 * Formulario original de gastos e ingresos.
 *
 * Ahora onSave admite una operación suspend,
 * compatible con SQLDelight Web.
 */
@Composable
private fun TransactionForm(
    categorias: List<Categoria>,
    initial: Transaccion?,
    onDismiss: () -> Unit,
    onSave: suspend (
        String,
        Double,
        String,
        String,
        TipoTransaccion
    ) -> ResultadoOperacion<Unit>
) {

    val scope = rememberCoroutineScope()

    var guardando by remember(initial) {
        mutableStateOf(false)
    }

    var descripcion by remember(initial) {
        mutableStateOf(initial?.descripcion ?: "")
    }

    var monto by remember(initial) {
        mutableStateOf(
            initial?.monto?.toString() ?: ""
        )
    }

    var fecha by remember(initial) {
        mutableStateOf(initial?.fecha ?: "")
    }

    var categoriaId by remember(initial) {
        mutableStateOf(
            initial?.categoriaId
                ?: categorias.firstOrNull()?.id.orEmpty()
        )
    }

    var tipo by remember(initial) {
        mutableStateOf(
            initial?.tipo ?: TipoTransaccion.GASTO
        )
    }

    var error by remember(initial) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!guardando) {
                onDismiss()
            }
        },

        title = {
            Text(
                if (initial == null) {
                    "Registrar movimiento"
                } else {
                    "Editar movimiento"
                }
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        error = null
                    },
                    label = {
                        Text("Descripción")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = {
                        monto = it
                        error = null
                    },
                    label = {
                        Text("Valor")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Fecha",
                    style = MaterialTheme.typography.labelMedium
                )

                SelectorFechaGasti(
                    fecha = fecha,
                    onFechaSeleccionada = { nuevaFecha ->
                        fecha = nuevaFecha
                        error = null
                    }
                )

                Row {

                    FilterChip(
                        selected = tipo == TipoTransaccion.GASTO,
                        onClick = {
                            tipo = TipoTransaccion.GASTO
                        },
                        label = {
                            Text("Gasto")
                        }
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    FilterChip(
                        selected = tipo == TipoTransaccion.INGRESO,
                        onClick = {
                            tipo = TipoTransaccion.INGRESO
                        },
                        label = {
                            Text("Ingreso")
                        }
                    )
                }

                var expanded by remember {
                    mutableStateOf(false)
                }

                Box {

                    OutlinedButton(
                        onClick = {
                            expanded = true
                        }
                    ) {

                        Text(
                            categorias.firstOrNull {
                                it.id == categoriaId
                            }?.nombre ?: "Seleccionar categoría"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        categorias.forEach { categoria ->

                            DropdownMenuItem(
                                text = {
                                    Text(categoria.nombre)
                                },
                                onClick = {
                                    categoriaId = categoria.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (categorias.isEmpty()) {
                    Text(
                        "Primero debes crear una categoría.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {

            Button(
                enabled = categorias.isNotEmpty() && !guardando,

                onClick = {

                    val valor = monto
                        .replace(",", ".")
                        .toDoubleOrNull()

                    when {

                        descripcion.isBlank() -> {
                            error = "La descripción es obligatoria."
                        }

                        valor == null ||
                                !valor.isFinite() ||
                                valor <= 0 -> {

                            error = "El valor debe ser mayor que cero."
                        }

                        !Regex("\\d{4}-\\d{2}-\\d{2}")
                            .matches(fecha) -> {

                            error = "Selecciona una fecha válida."
                        }

                        categoriaId.isBlank() -> {
                            error = "Selecciona una categoría."
                        }

                        else -> {

                            guardando = true
                            error = null

                            scope.launch {

                                try {

                                    when (
                                        val resultado = onSave(
                                            descripcion,
                                            valor,
                                            fecha,
                                            categoriaId,
                                            tipo
                                        )
                                    ) {

                                        is ResultadoOperacion.Exito -> {
                                            onDismiss()
                                        }

                                        is ResultadoOperacion.Error -> {
                                            error = resultado.mensaje
                                        }
                                    }

                                } catch (e: Exception) {

                                    error = e.message
                                        ?: "No se pudo guardar."

                                } finally {

                                    guardando = false
                                }
                            }
                        }
                    }
                }
            ) {

                Text(
                    if (guardando) {
                        "Guardando..."
                    } else {
                        "Guardar"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(
                enabled = !guardando,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Ventana original de categorías.
 *
 * Las operaciones ahora admiten
 * consultas asíncronas.
 */
@Composable
private fun CategoryDialog(
    categorias: List<Categoria>,
    onRegistrar: suspend (String) -> ResultadoOperacion<Unit>,
    onEliminar: suspend (String) -> ResultadoOperacion<Unit>,
    onDismiss: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var nombre by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    var procesando by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = {
            if (!procesando) {
                onDismiss()
            }
        },

        title = {
            Text("Administrar categorías")
        },

        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                    },
                    label = {
                        Text("Nombre de categoría")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    enabled = !procesando,
                    onClick = {

                        procesando = true

                        scope.launch {

                            try {

                                when (
                                    val resultado =
                                        onRegistrar(nombre)
                                ) {

                                    is ResultadoOperacion.Exito -> {
                                        nombre = ""
                                        mensaje =
                                            "Categoría creada correctamente."
                                    }

                                    is ResultadoOperacion.Error -> {
                                        mensaje = resultado.mensaje
                                    }
                                }

                            } catch (e: Exception) {

                                mensaje = e.message
                                    ?: "No se pudo crear la categoría."

                            } finally {

                                procesando = false
                            }
                        }
                    }
                ) {
                    Text("Agregar categoría")
                }

                categorias.forEach { categoria ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = categoria.nombre,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            enabled = !procesando,

                            onClick = {

                                procesando = true

                                scope.launch {

                                    try {

                                        when (
                                            val resultado =
                                                onEliminar(categoria.id)
                                        ) {

                                            is ResultadoOperacion.Exito -> {
                                                mensaje =
                                                    "Categoría eliminada."
                                            }

                                            is ResultadoOperacion.Error -> {
                                                mensaje = resultado.mensaje
                                            }
                                        }

                                    } catch (e: Exception) {

                                        mensaje = e.message
                                            ?: "No se pudo eliminar."

                                    } finally {

                                        procesando = false
                                    }
                                }
                            }
                        ) {
                            Text("Eliminar")
                        }
                    }
                }

                mensaje?.let {
                    Text(it)
                }
            }
        },

        confirmButton = {

            TextButton(
                enabled = !procesando,
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}
