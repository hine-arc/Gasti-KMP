package com.example.gastikmp.domain.gastos

/**
 * Tipos de transacciones disponibles en Gasti.
 */
enum class TipoTransaccion {
    GASTO,
    INGRESO
}

/**
 * Modelo que representa un gasto o ingreso.
 *
 * Cada transacción pertenece a un usuario y tiene
 * una categoría asociada.
 */
data class Transaccion(
    val id: String,
    val usuarioEmail: String,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoriaId: String,
    val tipo: TipoTransaccion
)