
package com.example.gastikmp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.example.gastikmp.data.GastoDataSource

import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.TipoTransaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

import kotlin.random.Random

/**
 * GASTI KMP
 *
 * ViewModel asíncrono de gastos e ingresos.
 *
 * Utiliza el contrato GastoDataSource.
 *
 * Preparado para:
 * - SQLDelight Android
 * - SQLDelight Desktop
 * - SQLDelight Web
 *
 * No reemplaza el GastoViewModel original.
 */
class GastoViewModelAsync(
    private val dataSource: GastoDataSource,
    usuarioEmail: String
) {

    private val correoUsuario = usuarioEmail
        .trim()
        .lowercase()

    // Reutilizamos el mismo estado de Compose.
    var uiState by mutableStateOf(GastoUiState())
        private set

    /**
     * Cargar movimientos y categorías
     * mediante operaciones asíncronas.
     */
    suspend fun cargarDatos() {

        if (correoUsuario.isBlank()) {
            uiState = uiState.copy(
                cargando = false,
                error = "No hay un usuario autenticado."
            )
            return
        }

        uiState = uiState.copy(
            cargando = true,
            error = null
        )

        try {

            val resultadoTransacciones =
                dataSource.obtenerTransaccionesPorUsuario(
                    correoUsuario
                )

            val resultadoCategorias =
                dataSource.obtenerCategoriasPorUsuario(
                    correoUsuario
                )

            var mensajeError: String? = null

            when (resultadoTransacciones) {

                is ResultadoOperacion.Exito -> {
                    uiState = uiState.copy(
                        transacciones =
                            resultadoTransacciones.datos
                    )
                }

                is ResultadoOperacion.Error -> {
                    mensajeError =
                        resultadoTransacciones.mensaje
                }
            }

            when (resultadoCategorias) {

                is ResultadoOperacion.Exito -> {
                    uiState = uiState.copy(
                        categorias =
                            resultadoCategorias.datos
                    )
                }

                is ResultadoOperacion.Error -> {
                    mensajeError =
                        resultadoCategorias.mensaje
                }
            }

            uiState = uiState.copy(
                error = mensajeError
            )

        } catch (e: Exception) {

            uiState = uiState.copy(
                error = e.message
                    ?: "No se pudieron cargar los datos."
            )

        } finally {

            uiState = uiState.copy(
                cargando = false
            )
        }
    }

    /**
     * Registrar gasto o ingreso.
     */
    suspend fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        fecha: String,
        categoriaId: String,
        tipo: TipoTransaccion
    ): ResultadoOperacion<Unit> {

        if (correoUsuario.isBlank()) {
            return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )
        }

        if (descripcion.isBlank()) {
            return ResultadoOperacion.Error(
                "La descripción es obligatoria."
            )
        }

        if (!monto.isFinite() || monto <= 0.0) {
            return ResultadoOperacion.Error(
                "El monto debe ser mayor que cero."
            )
        }

        if (fecha.isBlank()) {
            return ResultadoOperacion.Error(
                "La fecha es obligatoria."
            )
        }

        if (categoriaId.isBlank()) {
            return ResultadoOperacion.Error(
                "Selecciona una categoría."
            )
        }

        val transaccion = Transaccion(
            id = generarId(),
            usuarioEmail = correoUsuario,
            descripcion = descripcion.trim(),
            monto = monto,
            fecha = fecha,
            categoriaId = categoriaId,
            tipo = tipo
        )

        return ejecutarOperacion(
            mensajeExito =
                "Transacción registrada correctamente."
        ) {

            dataSource.registrarTransaccion(
                transaccion
            )
        }
    }

    /**
     * Actualizar un movimiento.
     */
    suspend fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        if (correoUsuario.isBlank()) {
            return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )
        }

        if (transaccion.descripcion.isBlank()) {
            return ResultadoOperacion.Error(
                "La descripción es obligatoria."
            )
        }

        if (
            !transaccion.monto.isFinite() ||
            transaccion.monto <= 0.0
        ) {
            return ResultadoOperacion.Error(
                "El monto debe ser mayor que cero."
            )
        }

        val actualizada = transaccion.copy(
            usuarioEmail = correoUsuario,
            descripcion =
                transaccion.descripcion.trim()
        )

        return ejecutarOperacion(
            mensajeExito =
                "Transacción actualizada correctamente."
        ) {

            dataSource.actualizarTransaccion(
                actualizada
            )
        }
    }

    /**
     * Eliminar un movimiento del usuario.
     */
    suspend fun eliminarTransaccion(
        id: String
    ): ResultadoOperacion<Unit> {

        if (correoUsuario.isBlank()) {
            return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )
        }

        return ejecutarOperacion(
            mensajeExito =
                "Transacción eliminada correctamente."
        ) {

            dataSource.eliminarTransaccion(
                id = id,
                usuarioEmail = correoUsuario
            )
        }
    }

    /**
     * Crear una categoría.
     */
    suspend fun registrarCategoria(
        nombre: String
    ): ResultadoOperacion<Unit> {

        if (correoUsuario.isBlank()) {
            return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )
        }

        if (nombre.isBlank()) {
            return ResultadoOperacion.Error(
                "El nombre de la categoría es obligatorio."
            )
        }

        val categoria = Categoria(
            id = generarId(),
            nombre = nombre.trim(),
            usuarioEmail = correoUsuario
        )

        return ejecutarOperacion(
            mensajeExito =
                "Categoría registrada correctamente."
        ) {

            dataSource.registrarCategoria(
                categoria
            )
        }
    }

    /**
     * Eliminar una categoría.
     *
     * Evita borrar categorías usadas
     * por las transacciones cargadas.
     */
    suspend fun eliminarCategoria(
        id: String
    ): ResultadoOperacion<Unit> {

        if (correoUsuario.isBlank()) {
            return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )
        }

        val categoriaEnUso =
            uiState.transacciones.any {
                it.categoriaId == id
            }

        if (categoriaEnUso) {
            return ResultadoOperacion.Error(
                "No puedes eliminar una categoría " +
                        "que tiene transacciones."
            )
        }

        return ejecutarOperacion(
            mensajeExito =
                "Categoría eliminada correctamente."
        ) {

            dataSource.eliminarCategoria(
                id = id,
                usuarioEmail = correoUsuario
            )
        }
    }

    /**
     * Ejecuta una escritura y recarga
     * los datos cuando termina con éxito.
     */
    private suspend fun ejecutarOperacion(
        mensajeExito: String,
        operacion: suspend () ->
        ResultadoOperacion<Unit>
    ): ResultadoOperacion<Unit> {

        uiState = uiState.copy(
            mensaje = null,
            error = null
        )

        val resultado = try {
            operacion()
        } catch (e: Exception) {
            ResultadoOperacion.Error(
                e.message
                    ?: "No se pudo completar la operación."
            )
        }

        when (resultado) {

            is ResultadoOperacion.Exito -> {

                cargarDatos()

                if (uiState.error == null) {
                    uiState = uiState.copy(
                        mensaje = mensajeExito
                    )
                }
            }

            is ResultadoOperacion.Error -> {

                uiState = uiState.copy(
                    error = resultado.mensaje
                )
            }
        }

        return resultado
    }

    /**
     * Totales calculados desde
     * los movimientos cargados.
     */
    fun obtenerTotalIngresos(): Double {

        return uiState.transacciones
            .filter {
                it.tipo == TipoTransaccion.INGRESO
            }
            .sumOf {
                it.monto
            }
    }

    fun obtenerTotalGastos(): Double {

        return uiState.transacciones
            .filter {
                it.tipo == TipoTransaccion.GASTO
            }
            .sumOf {
                it.monto
            }
    }

    fun obtenerBalance(): Double {

        return obtenerTotalIngresos() -
                obtenerTotalGastos()
    }

    /**
     * Limpia mensajes de la interfaz.
     */
    fun limpiarMensajes() {

        uiState = uiState.copy(
            mensaje = null,
            error = null
        )
    }

    private fun generarId(): String {

        return Random.nextLong().toString()
    }
}
