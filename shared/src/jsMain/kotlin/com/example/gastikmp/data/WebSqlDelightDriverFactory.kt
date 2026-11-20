
package com.example.gastikmp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

/**
 * GASTI KMP - SQLDELIGHT WEB
 *
 * Controlador SQLite para JavaScript.
 *
 * Preparación temporal del esquema Web
 * mientras adaptamos las consultas
 * generadas por SQLDelight.
 *
 * No afecta Android ni Desktop.
 */
object WebSqlDelightDriverFactory {

    fun createDriver(): SqlDriver {

        val trabajador = Worker(
            js(
                """new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""
            )
        )

        return WebWorkerDriver(trabajador)
    }

    /**
     * Crea las cinco tablas financieras
     * respetando los archivos .sq originales.
     *
     * IMPORTANTE:
     * El trabajador SQL.js actual mantiene
     * la base en memoria.
     */
    private suspend fun crearTablas(
        driver: SqlDriver
    ) {

        val sentencias = listOf(

            // USUARIOS
            """
            CREATE TABLE IF NOT EXISTS Usuario (
                email TEXT NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                password_salt TEXT NOT NULL
            )
            """.trimIndent(),

            // CATEGORÍAS
            """
            CREATE TABLE IF NOT EXISTS Categoria (
                id TEXT NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL,
                usuario_email TEXT NOT NULL
            )
            """.trimIndent(),

            // GASTOS
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

            // TRANSACCIONES
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

            // PRESUPUESTOS
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
    }

    /**
     * Prueba ejecutada desde main.kt.
     *
     * Confirma que las cinco instrucciones
     * CREATE TABLE se ejecutan sin errores.
     *
     * No comprueba persistencia.
     */
    suspend fun verificarConexion(): Result<Unit> {

        val driver = createDriver()

        return try {

            crearTablas(driver)

            println(
                "GASTI SQLITE: cinco tablas creadas correctamente"
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)

        } finally {

            driver.close()
        }
    }
}
