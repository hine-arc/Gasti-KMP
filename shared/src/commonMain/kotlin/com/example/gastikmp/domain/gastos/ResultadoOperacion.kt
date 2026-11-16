package com.example.gastikmp.domain.gastos

sealed class ResultadoOperacion<out T> {

    // Representa una operación realizada correctamente.
    data class Exito<T>(
        val datos: T
    ) : ResultadoOperacion<T>()

    // Representa una operación que produjo un error.
    data class Error(
        val mensaje: String
    ) : ResultadoOperacion<Nothing>()
}