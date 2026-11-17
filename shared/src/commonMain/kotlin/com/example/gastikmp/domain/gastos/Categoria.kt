package com.example.gastikmp.domain.gastos

data class Categoria(

    // Identificador único de la categoría.
    val id: String,

    // Nombre de la categoría.
    // Ejemplos: Alimentación, Transporte, Salud.
    val nombre: String,

    // Correo del usuario propietario de la categoría.
    val usuarioEmail: String
)