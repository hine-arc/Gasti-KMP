
package com.example.gastikmp.domain

/**
 * Modelo de presupuesto de Gasti KMP.
 *
 * Representa el límite de dinero que un usuario
 * establece para una categoría durante un mes.
 */
data class Presupuesto(

    // Identificador único del presupuesto.
    val id: String,

    // Correo del usuario propietario.
    val usuarioEmail: String,

    // Identificador de la categoría.
    val categoriaId: String,

    // Cantidad máxima que puede gastar.
    val limite: Double,

    // Año del presupuesto. Ejemplo: 2026.
    val anio: Int,

    // Mes del presupuesto, entre 1 y 12.
    val mes: Int
) {

    /**
     * Calcula cuánto dinero queda disponible.
     */
    fun calcularDisponible(
        gastosAcumulados: Double
    ): Double {
        return limite - gastosAcumulados
    }

    /**
     * Calcula el porcentaje utilizado.
     */
    fun calcularPorcentaje(
        gastosAcumulados: Double
    ): Double {

        if (limite <= 0.0) {
            return 0.0
        }

        return (gastosAcumulados / limite) * 100.0
    }

    /**
     * Indica si se superó el presupuesto.
     */
    fun estaExcedido(
        gastosAcumulados: Double
    ): Boolean {
        return gastosAcumulados > limite
    }

    /**
     * Indica si el gasto alcanzó o superó
     * el 80% del presupuesto.
     */
    fun mostrarAlerta(
        gastosAcumulados: Double
    ): Boolean {
        return calcularPorcentaje(
            gastosAcumulados
        ) >= 80.0
    }

    /**
     * Valida los datos básicos del presupuesto.
     */
    fun esValido(): Boolean {
        return id.isNotBlank() &&
                usuarioEmail.isNotBlank() &&
                categoriaId.isNotBlank() &&
                limite.isFinite() &&
                limite > 0.0 &&
                anio in 2000..2100 &&
                mes in 1..12
    }
}
