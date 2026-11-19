package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.TipoTransaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

/**
 * Repositorio encargado de administrar los gastos e ingresos.
 */
class TransaccionRepositorio(
    private val database: GastiDatabase
) {

    // Accedemos a las consultas generadas por SQLDelight.
    private val consultas = database.gastiDatabaseQueries

    /**
     * Registra un gasto o ingreso.
     */
    fun registrarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return try {

            require(transaccion.descripcion.isNotBlank()) {
                "La descripción es obligatoria"
            }

            require(transaccion.monto > 0) {
                "El monto debe ser mayor que cero"
            }

            require(transaccion.usuarioEmail.isNotBlank()) {
                "No se identificó al usuario"
            }

            consultas.insertarTransaccion(
                id = transaccion.id,
                usuario_email = transaccion.usuarioEmail,
                descripcion = transaccion.descripcion,
                monto = transaccion.monto,
                fecha = transaccion.fecha,
                categoria_id = transaccion.categoriaId,
                tipo = transaccion.tipo.name
            )

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al registrar la transacción: ${e.message}"
            )
        }
    }

    /**
     * Obtiene todas las transacciones de un usuario.
     */
    fun obtenerTransaccionesPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Transaccion>> {

        return try {

            val registros = consultas
                .obtenerTransaccionesPorUsuario(
                    usuario_email = usuarioEmail
                )
                .executeAsList()

            val transacciones = registros.map { registro ->

                Transaccion(
                    id = registro.id,
                    usuarioEmail = registro.usuario_email,
                    descripcion = registro.descripcion,
                    monto = registro.monto,
                    fecha = registro.fecha,
                    categoriaId = registro.categoria_id,
                    tipo = TipoTransaccion.valueOf(registro.tipo)
                )
            }

            ResultadoOperacion.Exito(transacciones)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar las transacciones: ${e.message}"
            )
        }
    }

    /**
     * Busca una transacción por su identificador y propietario.
     */
    fun obtenerTransaccionPorId(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Transaccion?> {

        return try {

            val registro = consultas
                .obtenerTransaccionPorId(
                    id = id,
                    usuario_email = usuarioEmail
                )
                .executeAsOneOrNull()

            val transaccion = registro?.let {

                Transaccion(
                    id = it.id,
                    usuarioEmail = it.usuario_email,
                    descripcion = it.descripcion,
                    monto = it.monto,
                    fecha = it.fecha,
                    categoriaId = it.categoria_id,
                    tipo = TipoTransaccion.valueOf(it.tipo)
                )
            }

            ResultadoOperacion.Exito(transaccion)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al buscar la transacción: ${e.message}"
            )
        }
    }

    /**
     * Actualiza una transacción existente.
     */
    fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return try {

            require(transaccion.descripcion.isNotBlank()) {
                "La descripción es obligatoria"
            }

            require(transaccion.monto > 0) {
                "El monto debe ser mayor que cero"
            }

            consultas.actualizarTransaccion(
                descripcion = transaccion.descripcion,
                monto = transaccion.monto,
                fecha = transaccion.fecha,
                categoria_id = transaccion.categoriaId,
                tipo = transaccion.tipo.name,
                id = transaccion.id,
                usuario_email = transaccion.usuarioEmail
            )

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al actualizar la transacción: ${e.message}"
            )
        }
    }

    /**
     * Elimina una transacción verificando su propietario.
     */
    fun eliminarTransaccion(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            consultas.eliminarTransaccion(
                id = id,
                usuario_email = usuarioEmail
            )

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar la transacción: ${e.message}"
            )
        }
    }
}