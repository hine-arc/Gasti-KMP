package com.example.gastikmp.domain.gastos

data class Gasto(

    // Identificador único del gasto.
    val id: String,

    // Correo del usuario que registró el gasto.
    val usuarioEmail: String,

    // Descripción del gasto.
    // Ejemplo: Compra de mercado.
    val descripcion: String,

    // Valor monetario del gasto.
    // Ejemplo: 25000.0 representa $25.000.
    val monto: Double,

    // Fecha en la que se realizó el gasto.
    // Se manejará utilizando el formato YYYY-MM-DD.
    val fecha: String,

    // Identificador de la categoría asociada al gasto.
    val categoriaId: String
)