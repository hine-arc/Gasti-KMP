package com.example.gastikmp.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.example.gastikmp.database.GastiDatabase

class DatabaseDriverFactory(
    private val context: Context
) {

    fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = GastiDatabase.Schema,
            context = context,
            name = "gasti.db"
        )
    }
}