package com.example.gastikmp

import androidx.compose.runtime.*
import com.example.gastikmp.data.AuthRepository
import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.navigation.MainScaffold

@Composable
fun App(database: GastiDatabase) {
    GastiTheme {
        var isLoggedIn by remember {
            mutableStateOf(AuthRepository.currentUser != null)
        }

        var isRegistering by remember {
            mutableStateOf(false)
        }

        if (isLoggedIn) {
            MainScaffold(
                database = database,
                onLogout = {
                    AuthRepository.logout()
                    isLoggedIn = false
                }
            )
        } else if (isRegistering) {
            RegisterScreen(
                onNavigateBack = {
                    isRegistering = false
                },
                onRegisterSuccess = {
                    isLoggedIn = true
                }
            )
        } else {
            LoginScreen(
                onNavigateToRegister = {
                    isRegistering = true
                },
                onLoginSuccess = {
                    isLoggedIn = true
                }
            )
        }
    }
}