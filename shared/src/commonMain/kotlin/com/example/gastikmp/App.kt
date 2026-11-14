package com.example.gastikmp

import androidx.compose.runtime.*

@Composable
fun App() {
    GastiTheme {
        // Estado de autenticación reactivo
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
