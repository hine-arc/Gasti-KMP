package com.example.gastikmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.gastikmp.data.DatabaseDriverFactory
import com.example.gastikmp.database.GastiDatabase

fun main() = application {

    val driver = DatabaseDriverFactory().createDriver()

    val database = GastiDatabase(driver)

    Window(
        onCloseRequest = ::exitApplication,
        title = "GastiKMP"
    ) {
        App(database)
    }
}