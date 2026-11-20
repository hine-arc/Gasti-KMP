
package com.example.gastikmp.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

import com.example.gastikmp.domain.gastos.Categoria
import com.example.gastikmp.domain.gastos.Transaccion
import com.example.gastikmp.domain.gastos.TipoTransaccion
import com.example.gastikmp.domain.gastos.ResultadoOperacion

/**
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Acceso a gastos y categorias mediante
 * SQLDelight WebWorkerDriver.
 *
 * Utiliza operaciones SQL asincronas.
 *
 * No modifica Android ni Desktop.
 *
 * Debe recibir un unico driver compartido
 * por las pantallas financieras Web.
 */
class WebGastoDataSource(
    private val driver: SqlDriver
) : GastoDataSource {

    private val categoriasIniciales = listOf(
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

    private fun normalizarCorreo(
        email: String
    ): String {
        return email.trim().lowercase()
    }

    /**
     * Leer movimientos.
     */
    override suspend fun obtenerTransaccionesPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Transaccion>> {

        return try {

            val email = normalizarCorreo(usuarioEmail)

            val movimientos = driver.executeQuery(
                identifier = null,
                sql = """
                    SELECT
                        id,
                        usuario_email,
                        descripcion,
                        monto,
                        fecha,
                        categoria_id,
                        tipo
                    FROM Transaccion
                    WHERE usuario_email = ?
                    ORDER BY fecha DESC
                """.trimIndent(),
                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        val lista = mutableListOf<Transaccion>()

                        while (cursor.next().await()) {

                            lista.add(
                                Transaccion(
                                    id = cursor.getString(0)!!,
                                    usuarioEmail = cursor.getString(1)!!,
                                    descripcion = cursor.getString(2)!!,
                                    monto = cursor.getDouble(3)!!,
                                    fecha = cursor.getString(4)!!,
                                    categoriaId = cursor.getString(5)!!,
                                    tipo = TipoTransaccion.valueOf(
                                        cursor.getString(6)!!
                                    )
                                )
                            )
                        }

                        lista.toList()
                    }
                },
                parameters = 1,
                binders = {
                    bindString(0, email)
                }
            ).await()

            ResultadoOperacion.Exito(movimientos)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar transacciones: ${e.message}"
            )
        }
    }

    /**
     * Buscar movimiento por ID y usuario.
     */
    override suspend fun obtenerTransaccionPorId(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Transaccion?> {

        return try {

            val email = normalizarCorreo(usuarioEmail)

            val movimiento = driver.executeQuery(
                identifier = null,
                sql = """
                    SELECT
                        id,
                        usuario_email,
                        descripcion,
                        monto,
                        fecha,
                        categoria_id,
                        tipo
                    FROM Transaccion
                    WHERE id = ?
                    AND usuario_email = ?
                """.trimIndent(),
                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        if (cursor.next().await()) {

                            Transaccion(
                                id = cursor.getString(0)!!,
                                usuarioEmail = cursor.getString(1)!!,
                                descripcion = cursor.getString(2)!!,
                                monto = cursor.getDouble(3)!!,
                                fecha = cursor.getString(4)!!,
                                categoriaId = cursor.getString(5)!!,
                                tipo = TipoTransaccion.valueOf(
                                    cursor.getString(6)!!
                                )
                            )

                        } else {
                            null
                        }
                    }
                },
                parameters = 2,
                binders = {
                    bindString(0, id)
                    bindString(1, email)
                }
            ).await()

            ResultadoOperacion.Exito(movimiento)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al buscar transacción: ${e.message}"
            )
        }
    }

    /**
     * Registrar gasto o ingreso.
     */
    override suspend fun registrarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return try {

            require(transaccion.descripcion.isNotBlank()) {
                "La descripción es obligatoria."
            }

            require(
                transaccion.monto.isFinite() &&
                        transaccion.monto > 0
            ) {
                "El monto debe ser mayor que cero."
            }

            driver.execute(
                identifier = null,
                sql = """
                    INSERT INTO Transaccion (
                        id,
                        usuario_email,
                        descripcion,
                        monto,
                        fecha,
                        categoria_id,
                        tipo
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                parameters = 7,
                binders = {
                    bindString(0, transaccion.id)
                    bindString(
                        1,
                        normalizarCorreo(
                            transaccion.usuarioEmail
                        )
                    )
                    bindString(2, transaccion.descripcion)
                    bindDouble(3, transaccion.monto)
                    bindString(4, transaccion.fecha)
                    bindString(5, transaccion.categoriaId)
                    bindString(6, transaccion.tipo.name)
                }
            ).await()

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al registrar transacción: ${e.message}"
            )
        }
    }

    /**
     * Actualizar movimiento.
     */
    override suspend fun actualizarTransaccion(
        transaccion: Transaccion
    ): ResultadoOperacion<Unit> {

        return try {

            require(transaccion.descripcion.isNotBlank()) {
                "La descripción es obligatoria."
            }

            require(
                transaccion.monto.isFinite() &&
                        transaccion.monto > 0
            ) {
                "El monto debe ser mayor que cero."
            }

            driver.execute(
                identifier = null,
                sql = """
                    UPDATE Transaccion
                    SET
                        descripcion = ?,
                        monto = ?,
                        fecha = ?,
                        categoria_id = ?,
                        tipo = ?
                    WHERE id = ?
                    AND usuario_email = ?
                """.trimIndent(),
                parameters = 7,
                binders = {
                    bindString(0, transaccion.descripcion)
                    bindDouble(1, transaccion.monto)
                    bindString(2, transaccion.fecha)
                    bindString(3, transaccion.categoriaId)
                    bindString(4, transaccion.tipo.name)
                    bindString(5, transaccion.id)
                    bindString(
                        6,
                        normalizarCorreo(
                            transaccion.usuarioEmail
                        )
                    )
                }
            ).await()

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al actualizar transacción: ${e.message}"
            )
        }
    }

    /**
     * Eliminar movimiento.
     */
    override suspend fun eliminarTransaccion(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            driver.execute(
                identifier = null,
                sql = """
                    DELETE FROM Transaccion
                    WHERE id = ?
                    AND usuario_email = ?
                """.trimIndent(),
                parameters = 2,
                binders = {
                    bindString(0, id)
                    bindString(
                        1,
                        normalizarCorreo(usuarioEmail)
                    )
                }
            ).await()

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar transacción: ${e.message}"
            )
        }
    }

    /**
     * Leer categorías de un usuario.
     */
    private suspend fun leerCategorias(
        email: String
    ): List<Categoria> {

        return driver.executeQuery(
            identifier = null,
            sql = """
                SELECT id, nombre, usuario_email
                FROM Categoria
                WHERE usuario_email = ?
            """.trimIndent(),
            mapper = { cursor ->

                QueryResult.AsyncValue {

                    val lista = mutableListOf<Categoria>()

                    while (cursor.next().await()) {

                        lista.add(
                            Categoria(
                                id = cursor.getString(0)!!,
                                nombre = cursor.getString(1)!!,
                                usuarioEmail = cursor.getString(2)!!
                            )
                        )
                    }

                    lista.toList()
                }
            },
            parameters = 1,
            binders = {
                bindString(0, email)
            }
        ).await()
    }

    /**
     * Crear categorías predeterminadas
     * cuando el usuario no tiene ninguna.
     */
    private suspend fun asegurarCategorias(
        email: String
    ) {

        if (leerCategorias(email).isNotEmpty()) {
            return
        }

        categoriasIniciales.forEachIndexed { indice, nombre ->

            driver.execute(
                identifier = null,
                sql = """
                    INSERT OR IGNORE INTO Categoria (
                        id,
                        nombre,
                        usuario_email
                    )
                    VALUES (?, ?, ?)
                """.trimIndent(),
                parameters = 3,
                binders = {
                    bindString(0, "default-$indice-$email")
                    bindString(1, nombre)
                    bindString(2, email)
                }
            ).await()
        }
    }

    /**
     * Obtener categorías.
     */
    override suspend fun obtenerCategoriasPorUsuario(
        usuarioEmail: String
    ): ResultadoOperacion<List<Categoria>> {

        return try {

            val email = normalizarCorreo(usuarioEmail)

            require(email.isNotBlank()) {
                "No hay un usuario autenticado."
            }

            asegurarCategorias(email)

            ResultadoOperacion.Exito(
                leerCategorias(email)
            )

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al consultar categorías: ${e.message}"
            )
        }
    }

    /**
     * Registrar categoría personalizada.
     */
    override suspend fun registrarCategoria(
        categoria: Categoria
    ): ResultadoOperacion<Unit> {

        return try {

            val email = normalizarCorreo(
                categoria.usuarioEmail
            )

            val nombre = categoria.nombre.trim()

            require(email.isNotBlank()) {
                "No hay un usuario autenticado."
            }

            require(nombre.isNotBlank()) {
                "El nombre es obligatorio."
            }

            val repetida = leerCategorias(email).any {
                it.nombre.equals(
                    nombre,
                    ignoreCase = true
                )
            }

            require(!repetida) {
                "Esta categoría ya existe."
            }

            driver.execute(
                identifier = null,
                sql = """
                    INSERT INTO Categoria (
                        id,
                        nombre,
                        usuario_email
                    )
                    VALUES (?, ?, ?)
                """.trimIndent(),
                parameters = 3,
                binders = {
                    bindString(0, categoria.id)
                    bindString(1, nombre)
                    bindString(2, email)
                }
            ).await()

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al registrar categoría: ${e.message}"
            )
        }
    }

    /**
     * Eliminar categoría de un usuario.
     */
    override suspend fun eliminarCategoria(
        id: String,
        usuarioEmail: String
    ): ResultadoOperacion<Unit> {

        return try {

            val email = normalizarCorreo(usuarioEmail)

            driver.execute(
                identifier = null,
                sql = """
                    DELETE FROM Categoria
                    WHERE id = ?
                    AND usuario_email = ?
                    AND NOT EXISTS (
                        SELECT 1
                        FROM Transaccion
                        WHERE categoria_id = ?
                        AND usuario_email = ?
                    )
                """.trimIndent(),
                parameters = 4,
                binders = {
                    bindString(0, id)
                    bindString(1, email)
                    bindString(2, id)
                    bindString(3, email)
                }
            ).await()

            ResultadoOperacion.Exito(Unit)

        } catch (e: Exception) {

            ResultadoOperacion.Error(
                "Error al eliminar categoría: ${e.message}"
            )
        }
    }
}
