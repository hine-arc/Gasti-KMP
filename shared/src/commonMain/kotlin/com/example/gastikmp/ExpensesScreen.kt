package com.example.gastikmp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.gastos.*
import com.example.gastikmp.presentation.viewmodel.GastoViewModel

@Composable
fun ExpensesScreen(database: GastiDatabase) {

    val vm = remember(database) {
        GastoViewModel(database)
    }

    val state = vm.uiState

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var mostrarCategorias by remember {
        mutableStateOf(false)
    }

    var movimientoEditar by remember {
        mutableStateOf<Transaccion?>(null)
    }

    LaunchedEffect(vm) {
        vm.cargarDatos()
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
                        "$ ${vm.obtenerTotalIngresos()}"
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
                        "$ ${vm.obtenerTotalGastos()}"
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Balance")
                Text(
                    "$ ${vm.obtenerBalance()}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

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
            "Movimientos registrados",
            style = MaterialTheme.typography.titleLarge
        )

        if (state.cargando) {

            CircularProgressIndicator()

        } else if (state.transacciones.isEmpty()) {

            Text("Todavía no tienes movimientos registrados.")

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
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                movimiento.descripcion,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text("Fecha: ${movimiento.fecha}")

                            Text("Tipo: ${movimiento.tipo}")

                            Text("Valor: $ ${movimiento.monto}")

                            Text(
                                "Categoría: ${
                                    state.categorias.firstOrNull {
                                        it.id == movimiento.categoriaId
                                    }?.nombre ?: "Sin categoría"
                                }"
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
                                        vm.eliminarTransaccion(movimiento.id)
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

    if (mostrarFormulario) {

        TransactionForm(
            categorias = state.categorias,
            initial = movimientoEditar,

            onDismiss = {
                mostrarFormulario = false
                movimientoEditar = null
            },

            onSave = { descripcion, monto, fecha, categoriaId, tipo ->

                if (movimientoEditar == null) {

                    vm.registrarTransaccion(
                        descripcion,
                        monto,
                        fecha,
                        categoriaId,
                        tipo
                    )

                } else {

                    vm.actualizarTransaccion(
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
        )
    }

    if (mostrarCategorias) {

        CategoryDialog(
            vm = vm,
            categorias = state.categorias,
            onDismiss = {
                mostrarCategorias = false
            }
        )
    }
}

@Composable
private fun TransactionForm(
    categorias: List<Categoria>,
    initial: Transaccion?,
    onDismiss: () -> Unit,
    onSave: (
        String,
        Double,
        String,
        String,
        TipoTransaccion
    ) -> ResultadoOperacion<Unit>
) {

    var descripcion by remember {
        mutableStateOf(initial?.descripcion ?: "")
    }

    var monto by remember {
        mutableStateOf(initial?.monto?.toString() ?: "")
    }

    var fecha by remember {
        mutableStateOf(initial?.fecha ?: "")
    }

    var categoriaId by remember {
        mutableStateOf(initial?.categoriaId ?: categorias.firstOrNull()?.id.orEmpty())
    }

    var tipo by remember {
        mutableStateOf(initial?.tipo ?: TipoTransaccion.GASTO)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") }
                )

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Valor") }
                )

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") }
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

                    Spacer(Modifier.width(8.dp))

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
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {

            Button(
                enabled = categorias.isNotEmpty(),
                onClick = {

                    val valor = monto
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (descripcion.isBlank()) {

                        error = "La descripción es obligatoria."

                    } else if (valor == null || valor <= 0) {

                        error = "El valor debe ser mayor que cero."

                    } else if (!Regex("\\d{4}-\\d{2}-\\d{2}").matches(fecha)) {

                        error = "Usa el formato AAAA-MM-DD."

                    } else {

                        when (
                            val resultado = onSave(
                                descripcion,
                                valor,
                                fecha,
                                categoriaId,
                                tipo
                            )
                        ) {

                            is ResultadoOperacion.Exito -> onDismiss()

                            is ResultadoOperacion.Error -> {
                                error = resultado.mensaje
                            }
                        }
                    }
                }
            ) {
                Text("Guardar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CategoryDialog(
    vm: GastoViewModel,
    categorias: List<Categoria>,
    onDismiss: () -> Unit
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Administrar categorías")
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                    },
                    label = {
                        Text("Nombre de categoría")
                    }
                )

                Button(
                    onClick = {

                        when (
                            val resultado = vm.registrarCategoria(nombre)
                        ) {

                            is ResultadoOperacion.Exito -> {
                                nombre = ""
                                mensaje = "Categoría creada correctamente."
                            }

                            is ResultadoOperacion.Error -> {
                                mensaje = resultado.mensaje
                            }
                        }
                    }
                ) {
                    Text("Agregar categoría")
                }

                categorias.forEach { categoria ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(categoria.nombre)

                        TextButton(
                            onClick = {

                                when (
                                    val resultado = vm.eliminarCategoria(categoria.id)
                                ) {

                                    is ResultadoOperacion.Exito -> {
                                        mensaje = "Categoría eliminada."
                                    }

                                    is ResultadoOperacion.Error -> {
                                        mensaje = resultado.mensaje
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
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}