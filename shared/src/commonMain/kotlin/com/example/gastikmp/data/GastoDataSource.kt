
package com.example.gastikmp.data

import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

/**
 * GASTI KMP
 *
 * Contrato compartido para acceder
 * a gastos, ingresos y categorías.
 *
 * Android y Desktop:
 * utilizarán SQLDelight con sus
 * controladores SQLite actuales.
 *
 * Web:
 * utilizará SQLDelight mediante
 * WebWorkerDriver y SQL.js.
 *
 * Todas las operaciones pueden
 * ejecutarse de forma asíncrona.
 */
interface GastoDataSource {

    suspend fun obtenerTransaccionesPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Transaccion>>

    suspend fun obtenerTransaccionPorId(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Transaccion?>

    suspend fun registrarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit>

    suspend fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit>

    suspend fun eliminarTransaccion(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit>

    suspend fun obtenerCategoriasPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Categoria>>

    suspend fun registrarCategoria(
        categoria: Categoria
    ): ResultadoOperacion<Unit>

    suspend fun eliminarCategoria(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit>
}
