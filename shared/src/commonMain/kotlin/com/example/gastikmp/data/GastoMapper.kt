package com.example.gastikmp.data

// Importamos los modelos de gastos y categorías.
import com.example.gastikmp.domain.gastos.Gasto
import com.example.gastikmp.domain.gastos.Categoria

// Esta clase se encarga de convertir los registros de SQLDelight
// en objetos que nuestra aplicación Kotlin pueda utilizar.
object GastoMapper {

    // =====================================================
    // CONVERTIR REGISTRO DE BASE DE DATOS A MODELO GASTO
    // =====================================================

    fun convertirGasto(
        id: String,
        usuarioEmail: String,
        descripcion: String,
        monto: Double,
        fecha: String,
        categoriaId: String
    ): Gasto {

        // Construimos un objeto Gasto utilizando los datos
        // recuperados de la base de datos.
        return Gasto(
            id = id,
            usuarioEmail = usuarioEmail,
            descripcion = descripcion,
            monto = monto,
            fecha = fecha,
            categoriaId = categoriaId
        )
    }

    // =====================================================
    // CONVERTIR REGISTRO DE BASE DE DATOS A MODELO CATEGORIA
    // =====================================================

    fun convertirCategoria(
        id: String,
        nombre: String,
        usuarioEmail: String
    ): Categoria {

        // Construimos un objeto Categoria con los datos
        // recuperados de SQLDelight.
        return Categoria(
            id = id,
            nombre = nombre,
            usuarioEmail = usuarioEmail
        )
    }
}