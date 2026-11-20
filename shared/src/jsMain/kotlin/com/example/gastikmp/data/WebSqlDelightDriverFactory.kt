
package com.example.gastikmp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

/**
 * GASTI KMP
 *
 * SQLDelight Web con trabajador personalizado.
 *
 * Usa SQL.js para SQLite y prepara
 * persistencia mediante IndexedDB.
 *
 * No modifica Android ni Desktop.
 */
object WebSqlDelightDriverFactory {

    fun createDriver(): SqlDriver {

        val trabajador = Worker(
            "/gasti-sqlite-worker.js"
        )

        return WebWorkerDriver(trabajador)
    }

    /**
     * Prueba temporal de las cinco tablas.
     *
     * Se ejecuta desde main.kt y muestra
     * el resultado en la consola de Chrome.
     */
    suspend fun verificarConexion(): Result<Unit> {

        val driver = createDriver()

        return try {

            val sentencias = listOf(
                """
                CREATE TABLE IF NOT EXISTS Usuario (
                    email TEXT NOT NULL PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    password_salt TEXT NOT NULL
                )
                """.trimIndent(),

                """
                CREATE TABLE IF NOT EXISTS Categoria (
                    id TEXT NOT NULL PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    usuario_email TEXT NOT NULL
                )
                """.trimIndent(),

                """
                CREATE TABLE IF NOT EXISTS Gasto (
                    id TEXT NOT NULL PRIMARY KEY,
                    usuario_email TEXT NOT NULL,
                    descripcion TEXT NOT NULL,
                    monto REAL NOT NULL,
                    fecha TEXT NOT NULL,
                    categoria_id TEXT NOT NULL,
                    FOREIGN KEY (categoria_id)
                        REFERENCES Categoria(id)
                )
                """.trimIndent(),

                """
                CREATE TABLE IF NOT EXISTS Transaccion (
                    id TEXT NOT NULL PRIMARY KEY,
                    usuario_email TEXT NOT NULL,
                    descripcion TEXT NOT NULL,
                    monto REAL NOT NULL,
                    fecha TEXT NOT NULL,
                    categoria_id TEXT NOT NULL,
                    tipo TEXT NOT NULL
                        CHECK(tipo IN ('GASTO', 'INGRESO')),
                    FOREIGN KEY (categoria_id)
                        REFERENCES Categoria(id)
                )
                """.trimIndent(),

                """
                CREATE TABLE IF NOT EXISTS Presupuesto (
                    id TEXT NOT NULL PRIMARY KEY,
                    usuario_email TEXT NOT NULL,
                    categoria_id TEXT NOT NULL,
                    limite REAL NOT NULL
                        CHECK(limite > 0),
                    anio INTEGER NOT NULL,
                    mes INTEGER NOT NULL
                        CHECK(mes BETWEEN 1 AND 12),
                    UNIQUE(
                        usuario_email,
                        categoria_id,
                        anio,
                        mes
                    )
                )
                """.trimIndent()
            )

            for (sentencia in sentencias) {

                driver.execute(
                    identifier = null,
                    sql = sentencia,
                    parameters = 0
                ).await()
            }

            println(
                "GASTI SQLITE: cinco tablas verificadas"
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)

        } finally {

            driver.close()
        }
    }
}
