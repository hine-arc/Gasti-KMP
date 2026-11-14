package com.example.gastikmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GastiPrimary = Color(0xFF5BE396) // Verde menta de la app Gasti
val GastiBackground = Color(0xFF0D131F) // Fondo oscuro
val GastiSurface = Color(0xFF161F30) // Color de tarjetas
val GastiOnSurface = Color(0xFFFFFFFF) // Texto blanco
val GastiOnSurfaceVariant = Color(0xFF94A3B8) // Texto secundario gris

val GastiDarkColorScheme = darkColorScheme(
    primary = GastiPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF133E2B),
    onPrimaryContainer = Color(0xFFB9F6CA),
    background = GastiBackground,
    onBackground = GastiOnSurface,
    surface = GastiSurface,
    onSurface = GastiOnSurface,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = GastiOnSurfaceVariant
)

@Composable
fun GastiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GastiDarkColorScheme,
        content = content
    )
}
