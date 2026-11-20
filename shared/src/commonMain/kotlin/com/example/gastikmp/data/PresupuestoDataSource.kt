
package com.example.gastikmp.data

import com.example.gastikmp.domain.Presupuesto

/**
 * GASTI KMP
 *
 * Contrato compartido para presupuestos.
 *
 * Permite utilizar la misma pantalla
 * BudgetScreen en Android, Desktop y Web.
 *
 * Android y Desktop pueden utilizar
 * sus repositorios SQLDelight actuales.
 *
 * Web utiliza consultas asincronas
 * mediante WebWorkerDriver.
 */
interface PresupuestoDataSource {

    /**
     * Consultar todos los presupuestos
     * de un usuario.
     */
    suspend fun obtenerPresupuestosPorUsuario(
        usuarioEmail: String
    ): Result<List<Presupuesto>>

    /**
     * Consultar presupuestos de un
     * periodo seleccionado.
     */
    suspend fun obtenerPresupuestosPorMes(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): Result<List<Presupuesto>>

    /**
     * Consultar categorias disponibles.
     */
    suspend fun obtenerCategorias(
        usuarioEmail: String
    ): Result<List<Pair<String, String>>>

    /**
     * Calcular gastos del periodo
     * agrupados por categoria.
     */
    suspend fun obtenerGastosPorCategoria(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): Result<Map<String, Double>>

    /**
     * Crear un presupuesto.
     */
    suspend fun crearPresupuesto(
        presupuesto: Presupuesto
    ): Result<Unit>

    /**
     * Actualizar el limite de un presupuesto.
     */
    suspend fun actualizarPresupuesto(
        id: String,
        usuarioEmail: String,
        nuevoLimite: Double
    ): Result<Unit>

    /**
     * Eliminar un presupuesto.
     */
    suspend fun eliminarPresupuesto(
        id: String,
        usuarioEmail: String
    ): Result<Unit>
}
