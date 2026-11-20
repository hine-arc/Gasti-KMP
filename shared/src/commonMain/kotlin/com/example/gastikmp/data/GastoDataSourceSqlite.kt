
package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

/**
 * GASTI KMP
 *
 * Adaptador de los repositorios SQLDelight
 * existentes al contrato GastoDataSource.
 *
 * Esta implementación conserva las
 * operaciones síncronas actuales.
 *
 * Android y Desktop pueden seguir
 * utilizando sus repositorios originales.
 *
 * Para WebWorkerDriver será necesaria
 * una implementación asíncrona cuando
 * se genere el esquema correspondiente.
 */
class GastoDataSourceSqlite(
    database: GastiDatabase
) : GastoDataSource {

    private val transaccionRepositorio =
        TransaccionRepositorio(database)

    private val categoriaRepositorio =
        CategoriaRepositorio(database)

    override suspend fun obtenerTransaccionesPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Transaccion>> {

        return transaccionRepositorio
            .obtenerTransaccionesPorUsuario(
                usuarioEmail
            )
    }

    override suspend fun obtenerTransaccionPorId(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Transaccion?> {

        return transaccionRepositorio
            .obtenerTransaccionPorId(
                id = id,
                usuarioEmail = usuarioEmail
            )
    }

    override suspend fun registrarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return transaccionRepositorio
            .registrarTransaccion(transaccion)
    }

    override suspend fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return transaccionRepositorio
            .actualizarTransaccion(transaccion)
    }

    override suspend fun eliminarTransaccion(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return transaccionRepositorio
            .eliminarTransaccion(
                id = id,
                usuarioEmail = usuarioEmail
            )
    }

    override suspend fun obtenerCategoriasPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Categoria>> {

        return categoriaRepositorio
            .obtenerCategoriasPorUsuario(
                usuarioEmail
            )
    }

    override suspend fun registrarCategoria(
        categoria: Categoria
    ): ResultadoOperacion<Unit> {

        return categoriaRepositorio
            .registrarCategoria(categoria)
    }

    override suspend fun eliminarCategoria(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return categoriaRepositorio
            .eliminarCategoria(
                id = id,
                usuarioEmail = usuarioEmail
            )
    }
}
