
package com.example.gastikmp.data

/**
 * Datos financieros compartidos para
 * la pantalla original de Inicio de Gasti.
 *
 * Android y Desktop obtienen estos datos
 * mediante SQLDelight.
 *
 * Web JavaScript los obtendra mediante
 * IndexedDB.
 *
 * Esta estructura no modifica la base
 * de datos de ninguna plataforma.
 */

data class MovimientoHomeDato(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val fecha: String,
    val categoriaId: String,
    val tipo: String
)

data class PresupuestoHomeDato(
    val categoriaId: String,
    val limite: Double,
    val anio: Int,
    val mes: Int
)

data class ResumenHomeDato(
    val movimientos: List<MovimientoHomeDato> = emptyList(),
    val presupuestos: List<PresupuestoHomeDato> = emptyList(),
    val categorias: Map<String, String> = emptyMap()
)
