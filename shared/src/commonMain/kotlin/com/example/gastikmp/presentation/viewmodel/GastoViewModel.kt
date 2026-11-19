package com.example.gastikmp.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.example.gastikmp.data.AuthRepository
import com.example.gastikmp.data.CategoriaRepositorio
import com.example.gastikmp.data.TransaccionRepositorio

import com.example.gastikmp.database.GastiDatabase

import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.TipoTransaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

import kotlin.random.Random

/**
 * Estado de la pantalla de gastos e ingresos.
 */
data class GastoUiState(
    val transacciones: List<Transaccion> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

/**
 * ViewModel encargado de administrar gastos e ingresos.
 */
class GastoViewModel(
    database: GastiDatabase
) {

    // Repositorios encargados de comunicarse con SQLite.
    private val transaccionRepositorio =
        TransaccionRepositorio(database)

    private val categoriaRepositorio =
        CategoriaRepositorio(database)

    // Estado observable por las pantallas Compose.
    var uiState by mutableStateOf(GastoUiState())
        private set

    /**
     * Obtiene el correo del usuario autenticado.
     */
    private fun obtenerUsuarioEmail(): String? {
        return AuthRepository.currentUser?.email
    }

    /**
     * Carga las transacciones y categorías del usuario.
     */
    fun cargarDatos() {

        val email = obtenerUsuarioEmail()

        if (email == null) {
            uiState = uiState.copy(
                error = "No hay un usuario autenticado."
            )
            return
        }

        uiState = uiState.copy(
            cargando = true,
            error = null
        )

        // Consultar transacciones.
        when (
            val resultado =
                transaccionRepositorio.obtenerTransaccionesPorUsuario(email)
        ) {
            is ResultadoOperacion.Exito -> {
                uiState = uiState.copy(
                    transacciones = resultado.datos
                )
            }

            is ResultadoOperacion.Error -> {
                uiState = uiState.copy(
                    error = resultado.mensaje
                )
            }
        }

        // Consultar categorías.
        when (
            val resultado =
                categoriaRepositorio.obtenerCategoriasPorUsuario(email)
        ) {
            is ResultadoOperacion.Exito -> {
                uiState = uiState.copy(
                    categorias = resultado.datos
                )
            }

            is ResultadoOperacion.Error -> {
                uiState = uiState.copy(
                    error = resultado.mensaje
                )
            }
        }

        uiState = uiState.copy(cargando = false)
    }

    /**
     * Registra un gasto o ingreso.
     */
    fun registrarTransaccion(
        descripcion: String,
        monto: Double,
        fecha: String,
        categoriaId: String,
        tipo: TipoTransaccion
    ): ResultadoOperacion<Unit> {

        val email = obtenerUsuarioEmail()
            ?: return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )

        if (descripcion.isBlank()) {
            return ResultadoOperacion.Error(
                "La descripción es obligatoria."
            )
        }

        if (monto <= 0.0) {
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
            usuarioEmail = email,
            descripcion = descripcion.trim(),
            monto = monto,
            fecha = fecha,
            categoriaId = categoriaId,
            tipo = tipo
        )

        val resultado =
            transaccionRepositorio.registrarTransaccion(transaccion)

        if (resultado is ResultadoOperacion.Exito) {
            cargarDatos()

            uiState = uiState.copy(
                mensaje = "Transacción registrada correctamente.",
                error = null
            )
        }

        return resultado
    }

    /**
     * Actualiza una transacción existente.
     */
    fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        val email = obtenerUsuarioEmail()
            ?: return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )

        if (transaccion.descripcion.isBlank()) {
            return ResultadoOperacion.Error(
                "La descripción es obligatoria."
            )
        }

        if (transaccion.monto <= 0.0) {
            return ResultadoOperacion.Error(
                "El monto debe ser mayor que cero."
            )
        }

        val transaccionActualizada = transaccion.copy(
            usuarioEmail = email,
            descripcion = transaccion.descripcion.trim()
        )

        val resultado =
            transaccionRepositorio.actualizarTransaccion(
                transaccionActualizada
            )

        if (resultado is ResultadoOperacion.Exito) {
            cargarDatos()

            uiState = uiState.copy(
                mensaje = "Transacción actualizada correctamente.",
                error = null
            )
        }

        return resultado
    }

    /**
     * Elimina una transacción.
     */
    fun eliminarTransaccion(
        id: String
    ): ResultadoOperacion<Unit> {

        val email = obtenerUsuarioEmail()
            ?: return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )

        val resultado =
            transaccionRepositorio.eliminarTransaccion(
                id = id,
                usuarioEmail = email
            )

        if (resultado is ResultadoOperacion.Exito) {
            cargarDatos()

            uiState = uiState.copy(
                mensaje = "Transacción eliminada correctamente.",
                error = null
            )
        }

        return resultado
    }

    /**
     * Registra una categoría nueva.
     */
    fun registrarCategoria(
        nombre: String
    ): ResultadoOperacion<Unit> {

        val email = obtenerUsuarioEmail()
            ?: return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )

        if (nombre.isBlank()) {
            return ResultadoOperacion.Error(
                "El nombre de la categoría es obligatorio."
            )
        }

        val categoria = Categoria(
            id = generarId(),
            nombre = nombre.trim(),
            usuarioEmail = email
        )

        val resultado =
            categoriaRepositorio.registrarCategoria(categoria)

        if (resultado is ResultadoOperacion.Exito) {
            cargarDatos()

            uiState = uiState.copy(
                mensaje = "Categoría registrada correctamente.",
                error = null
            )
        }

        return resultado
    }

    /**
     * Elimina una categoría.
     */
    fun eliminarCategoria(
        id: String
    ): ResultadoOperacion<Unit> {

        val email = obtenerUsuarioEmail()
            ?: return ResultadoOperacion.Error(
                "No hay un usuario autenticado."
            )

        // No permitir eliminar categorías que tengan transacciones.
        val categoriaEnUso = uiState.transacciones.any {
            it.categoriaId == id
        }

        if (categoriaEnUso) {
            return ResultadoOperacion.Error(
                "No puedes eliminar una categoría que tiene transacciones."
            )
        }

        val resultado =
            categoriaRepositorio.eliminarCategoria(
                id = id,
                usuarioEmail = email
            )

        if (resultado is ResultadoOperacion.Exito) {
            cargarDatos()

            uiState = uiState.copy(
                mensaje = "Categoría eliminada correctamente.",
                error = null
            )
        }

        return resultado
    }

    /**
     * Calcula el total de ingresos del usuario.
     */
    fun obtenerTotalIngresos(): Double {
        return uiState.transacciones
            .filter { it.tipo == TipoTransaccion.INGRESO }
            .sumOf { it.monto }
    }

    /**
     * Calcula el total de gastos del usuario.
     */
    fun obtenerTotalGastos(): Double {
        return uiState.transacciones
            .filter { it.tipo == TipoTransaccion.GASTO }
            .sumOf { it.monto }
    }

    /**
     * Calcula el balance entre ingresos y gastos.
     */
    fun obtenerBalance(): Double {
        return obtenerTotalIngresos() - obtenerTotalGastos()
    }

    /**
     * Genera un identificador para cada registro.
     */
    private fun generarId(): String {
        return Random.nextLong().toString()
    }

    /**
     * Limpia los mensajes mostrados en pantalla.
     */
    fun limpiarMensajes() {
        uiState = uiState.copy(
            mensaje = null,
            error = null
        )
    }
}