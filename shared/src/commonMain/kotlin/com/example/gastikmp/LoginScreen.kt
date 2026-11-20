
package com.example.gastikmp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GASTI KMP
 *
 * Pantalla original de inicio de sesion.
 *
 * Android y Desktop:
 * utilizan AuthRepository y SQLDelight.
 *
 * Web JavaScript:
 * puede proporcionar una funcion de
 * autenticacion que utilice IndexedDB.
 *
 * El diseño es compartido entre plataformas.
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    autenticar: (suspend (String, String) -> Result<Unit>)? = null
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var mostrarPassword by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Gasti",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Organiza tu dinero, alcanza tus metas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme
                    .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // CORREO ELECTRONICO
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = {
                            Text("ejemplo@correo.com")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // CONTRASEÑA
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = {
                            Text("Tu contraseña")
                        },
                        visualTransformation =
                            if (mostrarPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = mostrarPassword,
                            onCheckedChange = {
                                mostrarPassword = it
                            },
                            enabled = !isLoading
                        )

                        Text(
                            text = "Mostrar contraseña",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // MENSAJES DE ERROR
                    errorMessage?.let { mensaje ->

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // INICIO DE SESION
                    Button(
                        onClick = {

                            val correo = email.trim().lowercase()
                            val clave = password

                            when {

                                correo.isBlank() ||
                                        clave.isBlank() -> {

                                    errorMessage =
                                        "Ingresa tu correo y contraseña"
                                }

                                else -> {

                                    isLoading = true
                                    errorMessage = null

                                    scope.launch {

                                        try {

                                            val resultado: Result<Unit> =
                                                if (autenticar != null) {

                                                    // Web:
                                                    // usa la autenticacion
                                                    // proporcionada por
                                                    // la plataforma.
                                                    autenticar(
                                                        correo,
                                                        clave
                                                    )

                                                } else {

                                                    // Android y Desktop:
                                                    // conserva el flujo
                                                    // SQLDelight original.
                                                    withContext(
                                                        Dispatchers.Default
                                                    ) {

                                                        AuthRepository
                                                            .iniciarSesionPersistente(
                                                                email = correo,
                                                                pass = clave
                                                            )
                                                            .map { Unit }
                                                    }
                                                }

                                            if (resultado.isSuccess) {

                                                password = ""

                                                onLoginSuccess()

                                            } else {

                                                errorMessage = resultado
                                                    .exceptionOrNull()
                                                    ?.message
                                                    ?: "No se pudo iniciar sesión"
                                            }

                                        } catch (e: Exception) {

                                            errorMessage = e.message
                                                ?: "Error al iniciar sesión"

                                        } finally {

                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.primary,
                            contentColor =
                                MaterialTheme.colorScheme.onPrimary
                        )
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color =
                                    MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Text("Verificando cuenta...")

                        } else {

                            Text(
                                text = "Iniciar sesión",
                                style = MaterialTheme.typography
                                    .titleMedium
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !isLoading
                    ) {

                        Text(
                            text = "¿No tienes cuenta? Regístrate",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Tus finanzas, organizadas en un solo lugar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            }
        }
    }
}
