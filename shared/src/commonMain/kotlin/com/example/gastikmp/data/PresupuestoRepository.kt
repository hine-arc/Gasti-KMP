
package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase
import com.example.gastikmp.domain.Presupuesto

/**
 * GASTI KMP
 * Fase 3 - José Manuel
 *
 * Repositorio encargado de administrar
 * los presupuestos utilizando SQLDelight.
 */
class PresupuestoRepository(
    private val database: GastiDatabase
) {

    private val consultas = database.presupuestoQueries

    /**
     * CREAR PRESUPUESTO
     */
    fun crearPresupuesto(
        presupuesto: Presupuesto
    ) {
        require(presupuesto.esValido()) {
            "Los datos del presupuesto no son válidos"
        }

        val correo = presupuesto.usuarioEmail
            .trim()
            .lowercase()

        val existente = consultas
            .obtenerPresupuestoPorCategoria(
                usuario_email = correo,
                categoria_id = presupuesto.categoriaId,
                anio = presupuesto.anio.toLong(),
                mes = presupuesto.mes.toLong()
            )
            .executeAsOneOrNull()

        require(existente == null) {
            "Ya existe un presupuesto para esta categoría y mes"
        }

        consultas.insertarPresupuesto(
            id = presupuesto.id,
            usuario_email = correo,
            categoria_id = presupuesto.categoriaId,
            limite = presupuesto.limite,
            anio = presupuesto.anio.toLong(),
            mes = presupuesto.mes.toLong()
        )
    }

    /**
     * OBTENER TODOS LOS PRESUPUESTOS
     * DE UN USUARIO
     */
    fun obtenerPresupuestos(
        usuarioEmail: String
    ): List<Presupuesto> {

        return consultas
            .obtenerPresupuestosPorUsuario(
                usuario_email = usuarioEmail.trim().lowercase()
            )
            .executeAsList()
            .map { entidad ->

                Presupuesto(
                    id = entidad.id,
                    usuarioEmail = entidad.usuario_email,
                    categoriaId = entidad.categoria_id,
                    limite = entidad.limite,
                    anio = entidad.anio.toInt(),
                    mes = entidad.mes.toInt()
                )
            }
    }

    /**
     * OBTENER PRESUPUESTOS POR MES
     */
    fun obtenerPresupuestosPorMes(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): List<Presupuesto> {

        require(mes in 1..12) {
            "El mes debe estar entre 1 y 12"
        }

        return consultas
            .obtenerPresupuestosPorMes(
                usuario_email = usuarioEmail.trim().lowercase(),
                anio = anio.toLong(),
                mes = mes.toLong()
            )
            .executeAsList()
            .map { entidad ->

                Presupuesto(
                    id = entidad.id,
                    usuarioEmail = entidad.usuario_email,
                    categoriaId = entidad.categoria_id,
                    limite = entidad.limite,
                    anio = entidad.anio.toInt(),
                    mes = entidad.mes.toInt()
                )
            }
    }

    /**
     * BUSCAR PRESUPUESTO POR ID
     */
    fun obtenerPresupuestoPorId(
        id: String,
        usuarioEmail: String
    ): Presupuesto? {

        val entidad = consultas
            .obtenerPresupuestoPorId(
                id = id,
                usuario_email = usuarioEmail.trim().lowercase()
            )
            .executeAsOneOrNull()

        return entidad?.let {

            Presupuesto(
                id = it.id,
                usuarioEmail = it.usuario_email,
                categoriaId = it.categoria_id,
                limite = it.limite,
                anio = it.anio.toInt(),
                mes = it.mes.toInt()
            )
        }
    }

    /**
     * ACTUALIZAR EL LÍMITE
     */
    fun actualizarPresupuesto(
        id: String,
        usuarioEmail: String,
        nuevoLimite: Double
    ) {

        require(
            nuevoLimite.isFinite() && nuevoLimite > 0.0
        ) {
            "El límite debe ser mayor que cero"
        }

        val correo = usuarioEmail.trim().lowercase()

        require(
            obtenerPresupuestoPorId(id, correo) != null
        ) {
            "El presupuesto no existe"
        }

        consultas.actualizarPresupuesto(
            limite = nuevoLimite,
            id = id,
            usuario_email = correo
        )
    }

    /**
     * ELIMINAR PRESUPUESTO
     */
    fun eliminarPresupuesto(
        id: String,
        usuarioEmail: String
    ) {

        consultas.eliminarPresupuesto(
            id = id,
            usuario_email = usuarioEmail.trim().lowercase()
        )
    }

    /**
     * CALCULAR TOTAL PRESUPUESTADO
     * PARA UN MES
     */
    fun calcularTotalMensual(
        usuarioEmail: String,
        anio: Int,
        mes: Int
    ): Double {

        require(mes in 1..12) {
            "El mes debe estar entre 1 y 12"
        }

        return consultas
            .obtenerTotalPresupuestadoPorMes(
                usuario_email = usuarioEmail.trim().lowercase(),
                anio = anio.toLong(),
                mes = mes.toLong()
            )
            .executeAsOne()
    }

    /**
     * CONTAR PRESUPUESTOS
     */
    fun contarPresupuestos(
        usuarioEmail: String
    ): Long {

        return consultas
            .contarPresupuestosPorUsuario(
                usuario_email = usuarioEmail.trim().lowercase()
            )
            .executeAsOne()
    }
}
