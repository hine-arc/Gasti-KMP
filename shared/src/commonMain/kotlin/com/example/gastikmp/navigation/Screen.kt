package com.example.gastikmp.navigation

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
    object Budget : Screen()
    object Stats : Screen()
    object Expenses : Screen()
    object Profile : Screen()
    object Credits : Screen()
}
