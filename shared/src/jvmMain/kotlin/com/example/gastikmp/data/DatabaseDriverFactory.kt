package com.example.gastikmp.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.example.gastikmp.database.GastiDatabase
import java.io.File

class DatabaseDriverFactory {

    fun createDriver(): SqlDriver {

        val archivoBD = File("gasti.db")

        val baseDatosNueva = !archivoBD.exists()

        val driver = JdbcSqliteDriver(
            "jdbc:sqlite:${archivoBD.absolutePath}"
        )

        if (baseDatosNueva) {
            GastiDatabase.Schema.create(driver)
        }

        return driver
    }
}