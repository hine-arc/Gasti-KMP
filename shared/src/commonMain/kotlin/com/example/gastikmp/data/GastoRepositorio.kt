package com.example.gastikmp.data

// Importamos la base de datos generada por SQLDelight.
import com.example.gastikmp.database.GastiDatabase

// Importamos el modelo Gasto.
import com.example.gastikmp.domain.gastos.Gasto

// Importamos la clase que representa los resultados
// de las operaciones realizadas.
import com.example.gastikmp.domain.gastos.ResultadoOperacion

// Repositorio encargado de administrar los gastos.
class GastoRepositorio(
    private val database: GastiDatabase
) {

    // Accedemos a las consultas SQL generadas
    // automáticamente por SQLDelight.
    private val consultas = database.gastiDatabaseQueries


    // =====================================================
    // 1. REGISTRAR UN GASTO
    // =====================================================

    fun registrarGasto(
        gasto: Gasto
    ): ResultadoOperacion<Unit> {

        return try {

            // Ejecutamos la consulta SQL para insertar el gasto.
            consultas.insertarGasto(
                id = gasto.id,
                usuario_email = gasto.usuarioEmail,
                descripcion = gasto.descripcion,
                monto = gasto.monto,
                fecha = gasto.fecha,
                categoria_id = gasto.categoriaId
            )

            // Indicamos que el registro fue exitoso.
            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            // Devolvemos el mensaje si ocurrió algún problema.
            ResultadoOperacion.Error(
                "Error al registrar el gasto: ${e.message}"
            )
        }
    }


    // =====================================================
    // 2. CONSULTAR TODOS LOS GASTOS DE UN USUARIO
    // =====================================================

    fun obtenerGastosPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Gasto>> {

        return try {

            // Consultamos los gastos asociados al correo.
            val registros = consultas
                .obtenerGastosPorUsuario(usuario_email = usuarioEmail)
                .executeAsList()

            // Convertimos los registros de SQLDelight
            // en objetos Gasto utilizando el Mapper.
            val gastos = registros.map { registro ->

                GastoMapper.convertirGasto(
                    id = registro.id,
                    usuarioEmail = registro.usuario_email,
                    descripcion = registro.descripcion,
                    monto = registro.monto,
                    fecha = registro.fecha,
                    categoriaId = registro.categoria_id
                )
            }

            // Devolvemos la lista de gastos.
            ResultadoOperacion.Exito(gastos)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar los gastos: ${e.message}"
            )
        }
    }


    // =====================================================
    // 3. CONSULTAR UN GASTO POR SU IDENTIFICADOR
    // =====================================================

    fun obtenerGastoPorId(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Gasto?> {

        return try {

            // Buscamos el gasto utilizando su ID
            // y el correo del usuario propietario.
            val registro = consultas
                .obtenerGastoPorId(
                    id = id,
                    usuario_email = usuarioEmail
                )
                .executeAsOneOrNull()

            // Si existe, convertimos el registro en un modelo Gasto.
            val gasto = registro?.let {

                GastoMapper.convertirGasto(
                    id = it.id,
                    usuarioEmail = it.usuario_email,
                    descripcion = it.descripcion,
                    monto = it.monto,
                    fecha = it.fecha,
                    categoriaId = it.categoria_id
                )
            }

            // Devolvemos el gasto encontrado.
            // Si no existe, devolvemos null.
            ResultadoOperacion.Exito(gasto)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al buscar el gasto: ${e.message}"
            )
        }
    }


    // =====================================================
    // 4. ACTUALIZAR UN GASTO
    // =====================================================

    fun actualizarGasto(
        gasto: Gasto
    ): ResultadoOperacion<Unit> {

        return try {

            // Ejecutamos la consulta SQL de actualización.
            consultas.actualizarGasto(
                descripcion = gasto.descripcion,
                monto = gasto.monto,
                fecha = gasto.fecha,
                categoria_id = gasto.categoriaId,
                id = gasto.id,
                usuario_email = gasto.usuarioEmail
            )

            // Indicamos que la actualización fue exitosa.
            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al actualizar el gasto: ${e.message}"
            )
        }
    }


    // =====================================================
    // 5. ELIMINAR UN GASTO
    // =====================================================

    fun eliminarGasto(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            // Eliminamos el gasto utilizando su identificador
            // y verificando que pertenezca al usuario.
            consultas.eliminarGasto(
                id = id,
                usuario_email = usuarioEmail
            )

            // Indicamos que la eliminación fue exitosa.
            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar el gasto: ${e.message}"
            )
        }
    }
}