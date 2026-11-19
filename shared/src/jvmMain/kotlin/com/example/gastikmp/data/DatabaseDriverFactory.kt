package com.example.gastikmp.data

// Controlador SQLite para aplicaciones Windows Desktop.
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

// Interfaz general de los controladores SQLDelight.
import app.cash.sqldelight.db.SqlDriver

// Base de datos generada automáticamente por SQLDelight.
import com.example.gastikmp.database.GastiDatabase

// Permite trabajar con archivos del sistema.
import java.io.File

// Administra la conexión de SQLite para Windows Desktop.
class DatabaseDriverFactory {

    // Crea y devuelve el controlador de la base de datos.
    fun createDriver(): SqlDriver {

        // Definimos el archivo donde se almacenarán los datos.
        val archivoBD = File("gasti.db")

        // Comprobamos si la base de datos todavía no existe.
        val baseDatosNueva = !archivoBD.exists()

        // Creamos la conexión con SQLite.
        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${archivoBD.absolutePath}"
        )

        // Si es la primera ejecución, creamos las tablas.
        if (baseDatosNueva) {
            GastiDatabase.Schema.create(driver)
        }

        // Devolvemos la conexión para utilizarla en los repositorios.
        return driver
    }
}