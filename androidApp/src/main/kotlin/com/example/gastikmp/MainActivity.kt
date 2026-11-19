package com.example.gastikmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gastikmp.data.DatabaseDriverFactory
import com.example.gastikmp.database.GastiDatabase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val driver = DatabaseDriverFactory(
            applicationContext
        ).createDriver()

        val database = GastiDatabase(driver)

        setContent {
            App(database)
        }
    }
}