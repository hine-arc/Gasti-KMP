package com.example.gastikmp.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.russhwolf.settings.Settings
import com.example.gastikmp.domain.User

object AuthRepository {
    private val settings = Settings()
    private val registeredUsers = mutableMapOf<String, Pair<String, String>>()
    
    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        registeredUsers["jhoao@unicauca.edu.co"] = Pair("123456", "Jhoan Estudiante")
        registeredUsers["nelsonsandoval@unicauca.edu.co"] = Pair("123456", "Stiven Estudiante")
        registeredUsers["josemperafan@unicauca.edu.co"] = Pair("123456", "José Estudiante")



        val savedEmail = settings.getStringOrNull("logged_email")
        val savedName = settings.getStringOrNull("logged_name")
        if (savedEmail != null && savedName != null) {
            currentUser = User(email = savedEmail, name = savedName)
        }
    }

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

        settings.putString("logged_email", newUser.email)
        settings.putString("logged_name", newUser.name)

        return Result.success(newUser)
    }

    fun login(email: String, pass: String): Result<User> {
        val storedData = registeredUsers[email]
        if (storedData == null || storedData.first != pass) {
            return Result.failure(Exception("Correo o contraseña incorrectos"))
        }

        val loggedUser = User(email = email, name = storedData.second)
        currentUser = loggedUser

        settings.putString("logged_email", loggedUser.email)
        settings.putString("logged_name", loggedUser.name)

        return Result.success(loggedUser)
    }

    fun logout() {
        currentUser = null
        settings.remove("logged_email")
        settings.remove("logged_name")
    }
}
