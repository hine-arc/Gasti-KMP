
package com.example.gastikmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.navigation.MainScaffold

/**
 * GASTI KMP
 *
 * Punto principal de la aplicación.
 *
 * Android y Desktop proporcionan una
 * instancia de GastiDatabase.
 *
 * Web podrá iniciar mientras se prepara
 * su controlador de almacenamiento.
 *
 * IMPORTANTE:
 * Si no hay base de datos, no se permite
 * iniciar sesión ni registrar movimientos,
 * para evitar guardar información ficticia.
 */
@Composable
fun App(
    database: GastiDatabase? = null
) {

    GastiTheme {

        if (database == null) {

            // Pantalla temporal y explícita para Web.
            // No simula persistencia ni usuarios.
            WebPendienteDeBaseDeDatos()

        } else {

            // Solo se ejecuta cuando existe
            // una base de datos real.
            AppConBaseDeDatos(database)
        }
    }
}

/**
 * Aplicación completa con SQLDelight.
 *
 * Conserva el comportamiento actual
 * de Android y Windows Desktop.
 */
@Composable
private fun AppConBaseDeDatos(
    database: GastiDatabase
) {

    val currentUser = AuthRepository.currentUser

    var isRegistering by remember {
        mutableStateOf(false)
    }

    when {

        // USUARIO AUTENTICADO
        currentUser != null -> {

            MainScaffold(
                database = database,
                onLogout = {

                    AuthRepository.logout()

                    isRegistering = false
                }
            )
        }

        // REGISTRO DE USUARIO
        isRegistering -> {

            RegisterScreen(
                onNavigateBack = {
                    isRegistering = false
                },
                onRegisterSuccess = {
                    isRegistering = false
                }
            )
        }

        // INICIO DE SESIÓN
        else -> {

            LoginScreen(
                onNavigateToRegister = {
                    isRegistering = true
                },
                onLoginSuccess = {
                    isRegistering = false
                }
            )
        }
    }
}

/**
 * Pantalla informativa para Web.
 *
 * Se muestra hasta que configuremos
 * una base de datos persistente del navegador.
 */
@Composable
private fun WebPendienteDeBaseDeDatos() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Gasti",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Versión Web",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Almacenamiento Web pendiente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "La aplicación Web ya puede iniciar, " +
                            "pero todavía debemos conectar SQLDelight " +
                            "con una base de datos compatible con el navegador.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "El registro de usuarios, los gastos " +
                            "y los presupuestos estarán disponibles " +
                            "cuando terminemos esa integración.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
