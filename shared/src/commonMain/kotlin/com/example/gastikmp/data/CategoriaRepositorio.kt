package com.example.gastikmp.data

// Importamos la base de datos generada por SQLDelight.
import com.example.gastikmp.database.GastiDatabase

// Importamos el modelo Categoria.
import com.example.gastikmp.domain.gastos.Categoria

// Importamos la clase para manejar los resultados.
import com.example.gastikmp.domain.gastos.ResultadoOperacion

// Repositorio encargado de administrar las categorías.
class CategoriaRepositorio(
    private val database: GastiDatabase
) {

    // Accedemos a las consultas SQL generadas por SQLDelight.
    private val consultas = database.gastiDatabaseQueries


    // =====================================================
    // 1. REGISTRAR UNA CATEGORÍA
    // =====================================================

    fun registrarCategoria(
        categoria: Categoria
    ): ResultadoOperacion<Unit> {

        return try {

            // Insertamos la categoría en la base de datos.
            consultas.insertarCategoria(
                id = categoria.id,
                nombre = categoria.nombre,
                usuario_email = categoria.usuarioEmail
            )

            // Devolvemos el resultado exitoso.
            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al registrar la categoría: ${e.message}"
            )
        }
    }


    // =====================================================
    // 2. CONSULTAR LAS CATEGORÍAS DE UN USUARIO
    // =====================================================

    fun obtenerCategoriasPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Categoria>> {

        return try {

            // Consultamos las categorías del usuario.
            val registros = consultas
                .obtenerCategoriasPorUsuario(
                    usuario_email = usuarioEmail
                )
                .executeAsList()

            // Convertimos los registros de SQLDelight
            // en objetos Categoria utilizando GastoMapper.
            val categorias = registros.map { registro ->

                GastoMapper.convertirCategoria(
                    id = registro.id,
                    nombre = registro.nombre,
                    usuarioEmail = registro.usuario_email
                )
            }

            // Devolvemos la lista de categorías.
            ResultadoOperacion.Exito(categorias)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar las categorías: ${e.message}"
            )
        }
    }


    // =====================================================
    // 3. ELIMINAR UNA CATEGORÍA
    // =====================================================

    fun eliminarCategoria(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            // Eliminamos la categoría verificando
            // que pertenezca al usuario indicado.
            consultas.eliminarCategoria(
                id = id,
                usuario_email = usuarioEmail
            )

            // Devolvemos el resultado exitoso.
            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar la categoría: ${e.message}"
            )
        }
    }
}