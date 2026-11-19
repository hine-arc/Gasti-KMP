package com.example.gastikmp.data

// Permite acceder al almacenamiento y recursos de Android.
import android.content.Context

// Controlador SQLDelight para utilizar SQLite en Android.
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

// Interfaz general de los controladores SQLDelight.
import app.cash.sqldelight.db.SqlDriver

// Base de datos generada automáticamente por SQLDelight.
import com.example.gastikmp.database.GastiDatabase

// Administra la conexión SQLite para Android.
class DatabaseDriverFactory(
    private val context: Context
) {

    // Crea y devuelve el controlador de la base de datos.
    fun createDriver(): SqlDriver {

        // Creamos la conexión con SQLite.
        // AndroidSqliteDriver crea el esquema cuando se inicializa
        // por primera vez la base de datos.
        return AndroidSqliteDriver(
            schema = GastiDatabase.Schema,
            context = context,
            name = "gasti.db"
        )
    }
}
