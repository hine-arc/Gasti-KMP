
package com.example.gastikmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

import com.example.gastikmp.data.WebSqlDelightDriverFactory

import kotlin.random.Random

/**
 * GASTI KMP
 *
 * Entrada Web JavaScript.
 *
 * Mantiene las pantallas originales
 * y ejecuta una prueba temporal de
 * persistencia de SQLite.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport {

        GastiTheme {

            PruebaPersistenciaSqlite()

            WebAuthScreen()
        }
    }
}

/**
 * Leer el identificador almacenado
 * en SQLite utilizando SQLDelight.
 */
private suspend fun leerIdentificador(
    driver: SqlDriver
): String? {

    return driver.executeQuery(
        identifier = null,

        sql = """
            SELECT identificador
            FROM gasti_prueba_persistencia
            WHERE id = 1
        """.trimIndent(),

        mapper = { cursor ->

            QueryResult.AsyncValue {

                if (cursor.next().await()) {

                    // getString devuelve String?
                    // No necesita await().
                    cursor.getString(0)

                } else {

                    null
                }
            }
        },

        parameters = 0
    ).await()
}

/**
 * Prueba temporal:
 *
 * 1. Abre SQLite.
 * 2. Crea una tabla de prueba.
 * 3. Guarda un identificador.
 * 4. Lo vuelve a leer.
 * 5. Permite comprobarlo tras F5.
 *
 * No modifica cuentas ni movimientos.
 */
@Composable
private fun PruebaPersistenciaSqlite() {

    LaunchedEffect(Unit) {

        println(
            "GASTI SQLITE: iniciando prueba..."
        )

        val conexion =
            WebSqlDelightDriverFactory
                .verificarConexion()

        if (conexion.isFailure) {

            println(
                "GASTI SQLITE: ERROR DE CONEXION - " +
                        conexion.exceptionOrNull()?.message
            )

            return@LaunchedEffect
        }

        println(
            "GASTI SQLITE: CONEXION EXITOSA"
        )

        val driver =
            WebSqlDelightDriverFactory
                .createDriver()

        try {

            driver.execute(
                identifier = null,

                sql = """
                    CREATE TABLE IF NOT EXISTS
                    gasti_prueba_persistencia (
                        id INTEGER NOT NULL PRIMARY KEY,
                        identificador TEXT NOT NULL
                    )
                """.trimIndent(),

                parameters = 0
            ).await()

            val anterior =
                leerIdentificador(driver)

            if (anterior != null) {

                println(
                    "GASTI SQLITE: DATO RECUPERADO = $anterior"
                )

            } else {

                val nuevo =
                    Random.nextLong().toString()

                driver.execute(
                    identifier = null,

                    sql = """
                        INSERT INTO gasti_prueba_persistencia (
                            id,
                            identificador
                        )
                        VALUES (?, ?)
                    """.trimIndent(),

                    parameters = 2,

                    binders = {
                        bindLong(0, 1L)
                        bindString(1, nuevo)
                    }
                ).await()

                println(
                    "GASTI SQLITE: DATO NUEVO GUARDADO = $nuevo"
                )
            }

            val valorFinal =
                leerIdentificador(driver)

            println(
                "GASTI SQLITE: LECTURA FINAL = $valorFinal"
            )

            check(valorFinal != null) {
                "No se pudo leer el identificador."
            }

            println(
                "GASTI SQLITE: PRUEBA FINALIZADA"
            )

        } catch (error: Throwable) {

            println(
                "GASTI SQLITE: ERROR - " +
                        (error.message ?: error.toString())
            )

        } finally {

            driver.close()
        }
    }
}
