
package com.example.gastikmp.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

/**
 * Movimiento para el resumen Web.
 */
data class WebMovimientoHome(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoriaId: String,
    val tipo: String
)

/**
 * Presupuesto para el resumen Web.
 */
data class WebPresupuestoHome(
    val categoriaId: String,
    val limite: Double,
    val anio: Int,
    val mes: Int
)

/**
 * Información que necesita HomeScreen.
 */
data class WebDatosHome(
    val movimientos: List<WebMovimientoHome>,
    val presupuestos: List<WebPresupuestoHome>,
    val categorias: Map<String, String>
)

/**
 * GASTI KMP - WEB JAVASCRIPT
 *
 * Consultas asíncronas de la pantalla Inicio.
 *
 * Utiliza el mismo SqlDriver que Gastos.
 *
 * No abre conexiones adicionales.
 *
 * No realiza operaciones de escritura.
 */
class WebHomeDataSource(
    private val driver: SqlDriver
) {

    suspend fun cargarDatos(
        usuarioEmail: String
    ): Result<WebDatosHome> {

        return runCatching {

            val email = usuarioEmail
                .trim()
                .lowercase()

            require(email.isNotBlank()) {
                "No hay un usuario autenticado."
            }

            val movimientos = leerMovimientos(email)

            val presupuestos = leerPresupuestos(email)

            val categorias = leerCategorias(email)

            WebDatosHome(
                movimientos = movimientos,
                presupuestos = presupuestos,
                categorias = categorias
            )
        }
    }

    private suspend fun leerMovimientos(
        email: String
    ): List<WebMovimientoHome> {

        return driver.executeQuery(
            identifier = null,
            sql = """
                SELECT
                    id,
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

                    val lista =
                        mutableListOf<WebMovimientoHome>()

                    while (cursor.next().await()) {

                        lista.add(
                            WebMovimientoHome(
                                id = cursor.getString(0)!!,
                                descripcion = cursor.getString(1)!!,
                                monto = cursor.getDouble(2)!!,
                                fecha = cursor.getString(3)!!
                                    .replace("/", "-"),
                                categoriaId = cursor.getString(4)!!,
                                tipo = cursor.getString(5)!!
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

    private suspend fun leerPresupuestos(
        email: String
    ): List<WebPresupuestoHome> {

        return driver.executeQuery(
            identifier = null,
            sql = """
                SELECT
                    categoria_id,
                    limite,
                    anio,
                    mes
                FROM Presupuesto
                WHERE usuario_email = ?
            """.trimIndent(),
            mapper = { cursor ->

                QueryResult.AsyncValue {

                    val lista =
                        mutableListOf<WebPresupuestoHome>()

                    while (cursor.next().await()) {

                        lista.add(
                            WebPresupuestoHome(
                                categoriaId = cursor.getString(0)!!,
                                limite = cursor.getDouble(1)!!,
                                anio = cursor.getLong(2)!!.toInt(),
                                mes = cursor.getLong(3)!!.toInt()
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

    private suspend fun leerCategorias(
        email: String
    ): Map<String, String> {

        return driver.executeQuery(
            identifier = null,
            sql = """
                SELECT id, nombre
                FROM Categoria
                WHERE usuario_email = ?
            """.trimIndent(),
            mapper = { cursor ->

                QueryResult.AsyncValue {

                    val categorias =
                        mutableMapOf<String, String>()

                    while (cursor.next().await()) {

                        val id = cursor.getString(0)!!
                        val nombre = cursor.getString(1)!!

                        categorias[id] = nombre
                    }

                    categorias.toMap()
                }
            },
            parameters = 1,
            binders = {
                bindString(0, email)
            }
        ).await()
    }
}
