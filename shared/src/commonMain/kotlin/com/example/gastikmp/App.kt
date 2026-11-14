package com.example.gastikmp

import androidx.compose.runtime.*
import com.example.gastikmp.data.AuthRepository
import com.example.gastikmp.navigation.MainScaffold
import com.example.gastikmp.presentation.theme.GastiTheme
import com.example.gastikmp.presentation.screens.LoginScreen
import com.example.gastikmp.presentation.screens.RegisterScreen

@Composable
fun App() {
    GastiTheme {
        var isLoggedIn by remember { mutableStateOf(AuthRepository.currentUser != null) }
        var isRegistering by remember { mutableStateOf(false) }

        if (isLoggedIn) {
            MainScaffold(
                onLogout = {
                    AuthRepository.logout()
                    isLoggedIn = false
                }
            )
        } else {
            if (isRegistering) {
                RegisterScreen(
                    onNavigateBack = { isRegistering = false },
                    onRegisterSuccess = { isLoggedIn = true }
                )
            } else {
                LoginScreen(
                    onNavigateToRegister = { isRegistering = true },
                    onLoginSuccess = { isLoggedIn = true }
                )
            }
        }
    }
}
