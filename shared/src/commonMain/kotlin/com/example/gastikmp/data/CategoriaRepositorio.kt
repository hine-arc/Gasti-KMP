
package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.ResultadoOperacion

/**
 * GASTI KMP
 *
 * Repositorio de categorías.
 *
 * Incluye categorías predeterminadas para
 * facilitar el registro de gastos e ingresos.
 *
 * Se conservan las operaciones originales:
 * - Registrar categorías
 * - Consultar categorías
 * - Eliminar categorías
 */
class CategoriaRepositorio(
    private val database: GastiDatabase
) {

    private val consultas = database.gastiDatabaseQueries

    /**
     * Categorías iniciales de Gasti.
     *
     * Se crean cuando el usuario todavía
     * no tiene ninguna categoría.
     */
    private val categoriasPredeterminadas = listOf(
        "Alimentación",
        "Transporte",
        "Vivienda",
        "Salud",
        "Educación",
        "Entretenimiento",
        "Compras",
        "Servicios",
        "Salario",
        "Trabajo",
        "Regalos",
        "Otros"
    )

    /**
     * Crear categorías iniciales para
     * un usuario sin categorías.
     */
    private fun inicializarCategorias(
        usuarioEmail: String
    ) {

        val correo = usuarioEmail.trim().lowercase()

        if (correo.isBlank()) {
            return
        }

        // Consultar categorías existentes.
        val existentes = consultas
            .obtenerCategoriasPorUsuario(
                usuario_email = correo
            )
            .executeAsList()

        // No reemplazar ni duplicar categorías
        // si el usuario ya tiene registros.
        if (existentes.isNotEmpty()) {
            return
        }

        // Guardar todas las categorías iniciales.
        database.transaction {

            categoriasPredeterminadas.forEachIndexed {
                    indice, nombre ->

                val idCategoria =
                    "default-$indice-$correo"

                consultas.insertarCategoria(
                    id = idCategoria,
                    nombre = nombre,
                    usuario_email = correo
                )
            }
        }
    }

    /**
     * Registrar una categoría personalizada.
     */
    fun registrarCategoria(
        categoria: Categoria
    ): ResultadoOperacion<Unit> {

        return try {

            val correo = categoria.usuarioEmail
                .trim()
                .lowercase()

            val nombre = categoria.nombre.trim()

            require(correo.isNotBlank()) {
                "No hay un usuario válido."
            }

            require(nombre.isNotBlank()) {
                "El nombre de la categoría es obligatorio."
            }

            // Comprobar nombres repetidos.
            val categoriasActuales = consultas
                .obtenerCategoriasPorUsuario(
                    usuario_email = correo
                )
                .executeAsList()

            val repetida = categoriasActuales.any {
                it.nombre.equals(
                    nombre,
                    ignoreCase = true
                )
            }

            require(!repetida) {
                "Esta categoría ya existe."
            }

            consultas.insertarCategoria(
                id = categoria.id,
                nombre = nombre,
                usuario_email = correo
            )

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al registrar la categoría: ${e.message}"
            )
        }
    }

    /**
     * Consultar categorías de un usuario.
     *
     * Si todavía no tiene categorías,
     * se crean las predeterminadas.
     */
    fun obtenerCategoriasPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Categoria>> {

        return try {

            val correo = usuarioEmail.trim().lowercase()

            require(correo.isNotBlank()) {
                "No hay un usuario autenticado."
            }

            // Crear categorías iniciales si hace falta.
            inicializarCategorias(correo)

            // Consultar la lista actualizada.
            val registros = consultas
                .obtenerCategoriasPorUsuario(
                    usuario_email = correo
                )
                .executeAsList()

            val categorias = registros.map { registro ->

                GastoMapper.convertirCategoria(
                    id = registro.id,
                    nombre = registro.nombre,
                    usuarioEmail = registro.usuario_email
                )
            }

            ResultadoOperacion.Exito(categorias)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar las categorías: ${e.message}"
            )
        }
    }

    /**
     * Eliminar una categoría.
     *
     * Se comprueba que pertenezca
     * al usuario indicado.
     */
    fun eliminarCategoria(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            val correo = usuarioEmail.trim().lowercase()

            consultas.eliminarCategoria(
                id = id,
                usuario_email = correo
            )

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar la categoría: ${e.message}"
            )
        }
    }
}
