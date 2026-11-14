package com.example.gastikmp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings

/**
 * Repositorio compartido para gestionar la autenticación de usuarios y la persistencia de sesión utilizando Multiplatform Settings.
 */
object AuthRepository {
    private val settings = Settings()
    
    // Almacenamiento simulado de usuarios: Map<Email, Pair<Password, Name>>
    private val registeredUsers = mutableMapOf<String, Pair<String, String>>()

    // Estado observable de la sesión actual del usuario
    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        // Precargamos un usuario por defecto para pruebas rápidas
        registeredUsers["jhoao@unicauca.edu.co"] = Pair("123456", "Jhoan Estudiante")

        // Recuperar sesión persistida al iniciar la app
        val savedEmail = settings.getStringOrNull("logged_email")
        val savedName = settings.getStringOrNull("logged_name")
        if (savedEmail != null && savedName != null) {
            currentUser = User(email = savedEmail, name = savedName)
        }
    }

    /**
     * Registra un nuevo usuario en la aplicación y persiste la sesión.
     */
    fun register(email: String, pass: String, name: String): Result<User> {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }
        if (registeredUsers.containsKey(email)) {
            return Result.failure(Exception("El correo ya se encuentra registrado"))
        }

        registeredUsers[email] = Pair(pass, name)
        val newUser = User(email = email, name = name)
        currentUser = newUser

        // Persistir sesión
        settings.putString("logged_email", newUser.email)
        settings.putString("logged_name", newUser.name)

        return Result.success(newUser)
    }

    /**
     * Inicia sesión validando el correo y la contraseña, y persiste la sesión.
     */
    fun login(email: String, pass: String): Result<User> {
        val storedData = registeredUsers[email]
        if (storedData == null || storedData.first != pass) {
            return Result.failure(Exception("Correo o contraseña incorrectos"))
        }

        val loggedUser = User(email = email, name = storedData.second)
        currentUser = loggedUser

        // Persistir sesión
        settings.putString("logged_email", loggedUser.email)
        settings.putString("logged_name", loggedUser.name)

        return Result.success(loggedUser)
    }

    /**
     * Cierra la sesión actual del usuario y borra la persistencia.
     */
    fun logout() {
        currentUser = null
        settings.remove("logged_email")
        settings.remove("logged_name")
    }
}
