package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase
import app.cash.sqldelight.db.SqlDriver

/**
 * Proveedor compartido de la base de datos Gasti.
 *
 * Recibe el controlador SQLite de cada plataforma
 * y crea una instancia de GastiDatabase.
 */
class DatabaseProvider(
    driver: SqlDriver
) {
    val database = GastiDatabase(driver)
}