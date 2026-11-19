
package com.example.gastikmp.data

import com.example.gastikmp.AuthRepository as MainAuthRepository
import com.example.gastikmp.domain.User

/**
 * Repositorio de autenticación compartido
 * para los módulos de datos de Gasti KMP.
 *
 * Este repositorio utiliza la misma sesión
 * del AuthRepository principal.
 *
 * De esta forma:
 *
 * - Inicio de sesión
 * - Registro
 * - Home
 * - Gastos
 * - Categorías
 * - Perfil
 *
 * consultan al mismo usuario autenticado.
 */
object AuthRepository {

    /**
     * Devuelve el usuario que inició sesión
     * mediante el repositorio principal.
     *
     * No se crea una segunda sesión.
     */
    val currentUser: User?
        get() {

            val usuario =
                MainAuthRepository.currentUser

            return usuario?.let {
                User(
                    email = it.email,
                    name = it.name
                )
            }
        }

    /**
     * Registra un usuario utilizando
     * el repositorio principal.
     */
    fun register(
        email: String,
        pass: String,
        name: String
    ): Result<User> {

        val resultado = MainAuthRepository.register(
            email = email,
            pass = pass,
            name = name
        )

        return resultado.map { usuario ->
            User(
                email = usuario.email,
                name = usuario.name
            )
        }
    }

    /**
     * Inicia sesión utilizando
     * el repositorio principal.
     */
    fun login(
        email: String,
        pass: String
    ): Result<User> {

        val resultado = MainAuthRepository.login(
            email = email,
            pass = pass
        )

        return resultado.map { usuario ->
            User(
                email = usuario.email,
                name = usuario.name
            )
        }
    }

    /**
     * Cierra la sesión desde cualquier
     * módulo de la aplicación.
     */
    fun logout() {

        MainAuthRepository.logout()
    }
}
