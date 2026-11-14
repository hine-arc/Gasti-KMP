package com.example.gastikmp

/**
 * Modelo de datos que representa a un usuario registrado en la aplicación.
 * Al estar ubicado en commonMain, puede ser utilizado tanto en Android, Desktop como Web.
 */
data class User(
    val email: String,
    val name: String,
    val token: String = "token_session"
)
