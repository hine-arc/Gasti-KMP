
package com.example.gastikmp.data

import com.example.gastikmp.database.GastiDatabase

/**
 * Representa una cuenta almacenada en SQLDelight.
 *
 * La contraseña no se almacena directamente.
 * Se conservan el hash y la sal utilizados
 * para verificar las credenciales.
 */
data class CuentaGuardada(
    val email: String,
    val nombre: String,
    val passwordHash: String,
    val passwordSalt: String
)

/**
 * Repositorio de usuarios de Gasti KMP.
 *
 * Permite consultar y guardar usuarios
 * en la base de datos SQLDelight.
 *
 * Funciona con el código compartido KMP.
 */
class UsuarioRepositorio(
    private val database: GastiDatabase
) {

    // Consultas generadas automáticamente
    // por SQLDelight desde GastiDatabase.sq.
    private val consultas =
        database.gastiDatabaseQueries

    /**
     * Buscar un usuario por correo electrónico.
     *
     * Devuelve null si no está registrado.
     */
    fun buscarPorCorreo(
        email: String
    ): CuentaGuardada? {

        val correoNormalizado =
            email.trim().lowercase()

        val usuario = consultas
            .obtenerUsuarioPorEmail(correoNormalizado)
            .executeAsOneOrNull()

        return usuario?.let {

            CuentaGuardada(
                email = it.email,
                nombre = it.nombre,
                passwordHash = it.password_hash,
                passwordSalt = it.password_salt
            )
        }
    }

    /**
     * Comprobar si un correo ya está registrado.
     */
    fun existeCorreo(
        email: String
    ): Boolean {

        return buscarPorCorreo(email) != null
    }

    /**
     * Guardar un usuario nuevo.
     *
     * Recibe el hash y la sal ya calculados.
     * No recibe ni almacena la contraseña original.
     */
    fun guardarUsuario(
        email: String,
        nombre: String,
        passwordHash: String,
        passwordSalt: String
    ) {

        val correoNormalizado =
            email.trim().lowercase()

        require(correoNormalizado.isNotBlank()) {
            "El correo es obligatorio"
        }

        require(nombre.isNotBlank()) {
            "El nombre es obligatorio"
        }

        require(passwordHash.isNotBlank()) {
            "El hash de contraseña es obligatorio"
        }

        require(passwordSalt.isNotBlank()) {
            "La sal de contraseña es obligatoria"
        }

        require(!existeCorreo(correoNormalizado)) {
            "El correo ya está registrado"
        }

        consultas.insertarUsuario(
            email = correoNormalizado,
            nombre = nombre.trim(),
            password_hash = passwordHash,
            password_salt = passwordSalt
        )
    }

    /**
     * Actualizar el nombre de una cuenta.
     */
    fun actualizarNombre(
        email: String,
        nuevoNombre: String
    ) {

        require(nuevoNombre.isNotBlank()) {
            "El nombre no puede estar vacío"
        }

        consultas.actualizarNombreUsuario(
            nombre = nuevoNombre.trim(),
            email = email.trim().lowercase()
        )
    }
}
