
package com.example.gastikmp.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

import com.example.gastikmp.domain.Presupuesto

/**
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Implementacion Web del contrato
 * compartido PresupuestoDataSource.
 *
 * Utiliza WebWorkerDriver mediante
 * operaciones SQLDelight asincronas.
 *
 * Comparte el mismo driver utilizado
 * por Inicio y Gastos.
 *
 * No crea conexiones adicionales.
 */
class WebPresupuestoDataSource(
    private val driver: SqlDriver
) : PresupuestoDataSource {

    private fun normalizarCorreo(
        usuarioEmail: String
    ): String {
        return usuarioEmail.trim().lowercase()
    }

    private fun validarCorreo(
        usuarioEmail: String
    ): String {

        val email = normalizarCorreo(usuarioEmail)

        require(email.isNotBlank()) {
            "No hay un usuario autenticado."
        }

        return email
    }

    private fun validarPeriodo(
        anio: Int,
        mes: Int
    ) {

        require(anio in 2000..2100) {
            "El año no es válido."
        }

        require(mes in 1..12) {
            "El mes debe estar entre 1 y 12."
        }
    }

    /**
     * Consultar todos los presupuestos
     * del usuario autenticado.
     */
    override suspend fun obtenerPresupuestosPorUsuario(
        usuarioEmail: String
    ): Result<List<Presupuesto>> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            driver.executeQuery(
                identifier = null,

                sql = """
                    SELECT
                        id,
                        usuario_email,
                        categoria_id,
                        limite,
                        anio,
                        mes
                    FROM Presupuesto
                    WHERE usuario_email = ?
                    ORDER BY anio DESC, mes DESC
                """.trimIndent(),

                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        val lista =
                            mutableListOf<Presupuesto>()

                        while (cursor.next().await()) {

                            lista.add(
                                Presupuesto(
                                    id = cursor.getString(0)!!,
                                    usuarioEmail =
                                        cursor.getString(1)!!,
                                    categoriaId =
                                        cursor.getString(2)!!,
                                    limite = cursor.getDouble(3)!!,
                                    anio = cursor.getLong(4)!!
                                        .toInt(),
                                    mes = cursor.getLong(5)!!
                                        .toInt()
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
    }

    /**
     * Consultar presupuestos para
     * un mes y año seleccionados.
     */
    override suspend fun obtenerPresupuestosPorMes(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): Result<List<Presupuesto>> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            validarPeriodo(anio, mes)

            driver.executeQuery(
                identifier = null,

                sql = """
                    SELECT
                        id,
                        usuario_email,
                        categoria_id,
                        limite,
                        anio,
                        mes
                    FROM Presupuesto
                    WHERE usuario_email = ?
                      AND anio = ?
                      AND mes = ?
                    ORDER BY categoria_id
                """.trimIndent(),

                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        val lista =
                            mutableListOf<Presupuesto>()

                        while (cursor.next().await()) {

                            lista.add(
                                Presupuesto(
                                    id = cursor.getString(0)!!,
                                    usuarioEmail =
                                        cursor.getString(1)!!,
                                    categoriaId =
                                        cursor.getString(2)!!,
                                    limite = cursor.getDouble(3)!!,
                                    anio = cursor.getLong(4)!!
                                        .toInt(),
                                    mes = cursor.getLong(5)!!
                                        .toInt()
                                )
                            )
                        }

                        lista.toList()
                    }
                },

                parameters = 3,

                binders = {
                    bindString(0, email)
                    bindLong(1, anio.toLong())
                    bindLong(2, mes.toLong())
                }

            ).await()
        }
    }

    /**
     * Consultar categorías del usuario.
     *
     * Devuelve el identificador y nombre
     * que necesita BudgetScreen.
     */
    override suspend fun obtenerCategorias(
        usuarioEmail: String
    ): Result<List<Pair<String, String>>> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            driver.executeQuery(
                identifier = null,

                sql = """
                    SELECT
                        id,
                        nombre
                    FROM Categoria
                    WHERE usuario_email = ?
                    ORDER BY nombre
                """.trimIndent(),

                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        val lista =
                            mutableListOf<Pair<String, String>>()

                        while (cursor.next().await()) {

                            val id = cursor.getString(0)!!
                            val nombre = cursor.getString(1)!!

                            lista.add(id to nombre)
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
    }

    /**
     * Calcular los gastos por categoría
     * durante el período seleccionado.
     *
     * Solo incluye transacciones GASTO.
     */
    override suspend fun obtenerGastosPorCategoria(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): Result<Map<String, Double>> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            validarPeriodo(anio, mes)

            val periodo =
                "$anio-" + mes.toString().padStart(2, '0')

            driver.executeQuery(
                identifier = null,

                sql = """
                    SELECT
                        categoria_id,
                        SUM(monto)
                    FROM Transaccion
                    WHERE usuario_email = ?
                      AND tipo = 'GASTO'
                      AND SUBSTR(
                          REPLACE(fecha, '/', '-'),
                          1,
                          7
                      ) = ?
                    GROUP BY categoria_id
                """.trimIndent(),

                mapper = { cursor ->

                    QueryResult.AsyncValue {

                        val gastos =
                            mutableMapOf<String, Double>()

                        while (cursor.next().await()) {

                            val categoriaId =
                                cursor.getString(0)!!

                            val total =
                                cursor.getDouble(1) ?: 0.0

                            gastos[categoriaId] = total
                        }

                        gastos.toMap()
                    }
                },

                parameters = 2,

                binders = {
                    bindString(0, email)
                    bindString(1, periodo)
                }

            ).await()
        }
    }

    /**
     * Crear un presupuesto.
     *
     * La base de datos impide que un usuario
     * tenga dos presupuestos para la misma
     * categoría dentro del mismo período.
     */
    override suspend fun crearPresupuesto(
        presupuesto: Presupuesto
    ): Result<Unit> {

        return runCatching {

            val email =
                validarCorreo(presupuesto.usuarioEmail)

            validarPeriodo(
                presupuesto.anio,
                presupuesto.mes
            )

            require(presupuesto.id.isNotBlank()) {
                "El identificador es obligatorio."
            }

            require(presupuesto.categoriaId.isNotBlank()) {
                "Selecciona una categoría."
            }

            require(
                presupuesto.limite.isFinite() &&
                        presupuesto.limite > 0.0
            ) {
                "El límite debe ser mayor que cero."
            }

            driver.execute(
                identifier = null,

                sql = """
                    INSERT INTO Presupuesto (
                        id,
                        usuario_email,
                        categoria_id,
                        limite,
                        anio,
                        mes
                    )
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),

                parameters = 6,

                binders = {
                    bindString(0, presupuesto.id)
                    bindString(1, email)
                    bindString(2, presupuesto.categoriaId)
                    bindDouble(3, presupuesto.limite)
                    bindLong(
                        4,
                        presupuesto.anio.toLong()
                    )
                    bindLong(
                        5,
                        presupuesto.mes.toLong()
                    )
                }

            ).await()

            Unit
        }
    }

    /**
     * Actualizar únicamente el límite.
     *
     * No modifica el usuario, la categoría
     * ni el período del presupuesto.
     */
    override suspend fun actualizarPresupuesto(
        id: String,
        usuarioEmail: String,
        nuevoLimite: Double
    ): Result<Unit> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            require(id.isNotBlank()) {
                "El presupuesto no es válido."
            }

            require(
                nuevoLimite.isFinite() &&
                        nuevoLimite > 0.0
            ) {
                "El límite debe ser mayor que cero."
            }

            driver.execute(
                identifier = null,

                sql = """
                    UPDATE Presupuesto
                    SET limite = ?
                    WHERE id = ?
                      AND usuario_email = ?
                """.trimIndent(),

                parameters = 3,

                binders = {
                    bindDouble(0, nuevoLimite)
                    bindString(1, id)
                    bindString(2, email)
                }

            ).await()

            Unit
        }
    }

    /**
     * Eliminar un presupuesto del usuario.
     *
     * No elimina gastos ni categorías.
     */
    override suspend fun eliminarPresupuesto(
        id: String,
        usuarioEmail: String
    ): Result<Unit> {

        return runCatching {

            val email = validarCorreo(usuarioEmail)

            require(id.isNotBlank()) {
                "El presupuesto no es válido."
            }

            driver.execute(
                identifier = null,

                sql = """
                    DELETE FROM Presupuesto
                    WHERE id = ?
                      AND usuario_email = ?
                """.trimIndent(),

                parameters = 2,

                binders = {
                    bindString(0, id)
                    bindString(1, email)
                }

            ).await()

            Unit
        }
    }
}
