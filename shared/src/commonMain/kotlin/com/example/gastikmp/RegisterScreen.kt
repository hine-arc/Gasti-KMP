
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
 * Registro de usuarios con diseño compartido.
 *
 * Android y Desktop:
 * usan AuthRepository y SQLDelight.
 *
 * Web JavaScript:
 * puede proporcionar una funcion de registro
 * para usar IndexedDB y Web Crypto.
 *
 * El diseño visual es el mismo en
 * todas las plataformas.
 */
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    registrar: (
    suspend (String, String, String) -> Result<Unit>
    )? = null
) {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
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
                text = "Crea tu cuenta y toma el control",
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Registro",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    // NOMBRE COMPLETO
                    Text(
                        text = "Nombre completo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = {
                            Text("Tu nombre")
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // CORREO ELECTRONICO
                    Text(
                        text = "Correo electrónico",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
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

                    // CONTRASEÑA
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
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
                        visualTransformation =
                            if (mostrarPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        placeholder = {
                            Text("Mínimo 6 caracteres")
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // CONFIRMAR CONTRASEÑA
                    Text(
                        text = "Confirmar contraseña",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation =
                            if (mostrarPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        placeholder = {
                            Text("Repite tu contraseña")
                        },
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
                            text = "Mostrar contraseñas",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // MENSAJE DE ERROR
                    errorMessage?.let { mensaje ->

                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    // BOTON DE REGISTRO
                    Button(
                        onClick = {

                            val cleanName = name.trim()
                            val cleanEmail =
                                email.trim().lowercase()

                            when {

                                cleanName.isBlank() ||
                                        cleanEmail.isBlank() ||
                                        password.isBlank() ||
                                        confirmPassword.isBlank() -> {

                                    errorMessage =
                                        "Completa todos los campos"
                                }

                                !cleanEmail.contains("@") ||
                                        !cleanEmail
                                            .substringAfter("@")
                                            .contains(".") -> {

                                    errorMessage =
                                        "Ingresa un correo electrónico válido"
                                }

                                password.length < 6 -> {

                                    errorMessage =
                                        "La contraseña debe tener mínimo 6 caracteres"
                                }

                                password != confirmPassword -> {

                                    errorMessage =
                                        "Las contraseñas no coinciden"
                                }

                                else -> {

                                    errorMessage = null
                                    isLoading = true

                                    // Conservar una copia local de
                                    // la contraseña durante el registro.
                                    val clave = password

                                    scope.launch {

                                        try {

                                            val resultado: Result<Unit> =
                                                if (registrar != null) {

                                                    // WEB JAVASCRIPT:
                                                    // el almacenamiento
                                                    // se proporciona desde
                                                    // WebAuthScreen.
                                                    registrar(
                                                        cleanEmail,
                                                        clave,
                                                        cleanName
                                                    )

                                                } else {

                                                    // ANDROID Y DESKTOP:
                                                    // mantienen el registro
                                                    // original en SQLDelight.
                                                    withContext(
                                                        Dispatchers.Default
                                                    ) {

                                                        AuthRepository
                                                            .registrarPersistente(
                                                                email = cleanEmail,
                                                                pass = clave,
                                                                name = cleanName
                                                            )
                                                            .map { Unit }
                                                    }
                                                }

                                            if (resultado.isSuccess) {

                                                password = ""
                                                confirmPassword = ""

                                                onRegisterSuccess()

                                            } else {

                                                errorMessage =
                                                    resultado
                                                        .exceptionOrNull()
                                                        ?.message
                                                        ?: "No se pudo registrar la cuenta"
                                            }

                                        } catch (e: Exception) {

                                            errorMessage =
                                                e.message
                                                    ?: "Ocurrió un error durante el registro"

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

                            Text("Guardando cuenta...")

                        } else {

                            Text(
                                text = "Registrarse",
                                style = MaterialTheme.typography
                                    .titleMedium
                            )
                        }
                    }

                    TextButton(
                        onClick = onNavigateBack,
                        enabled = !isLoading
                    ) {

                        Text(
                            text = "¿Ya tienes cuenta? Inicia sesión",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
