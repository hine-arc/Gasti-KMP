
package com.example.gastikmp.data

/**
 * Representa los datos necesarios para verificar
 * una contraseña sin almacenar la contraseña original.
 *
 * passwordHash: resultado de la derivación.
 * passwordSalt: sal aleatoria codificada.
 */
data class PasswordProtegida(
    val passwordHash: String,
    val passwordSalt: String
)

/**
 * Contrato de seguridad para Gasti KMP.
 *
 * Las implementaciones deben utilizar:
 *
 * - PBKDF2 con HMAC-SHA256.
 * - Una sal aleatoria criptográficamente segura.
 * - Un número adecuado de iteraciones.
 * - Comparación segura contra ataques de tiempo.
 *
 * Este contrato permite utilizar implementaciones
 * compatibles con Android, Desktop, JS y WasmJS.
 */
interface PasswordSecurity {

    /**
     * Deriva un hash de una contraseña nueva.
     *
     * El resultado contiene el hash y la sal
     * que posteriormente se guardarán en SQLDelight.
     */
    suspend fun proteger(
        password: String
    ): PasswordProtegida

    /**
     * Verifica una contraseña ingresada utilizando
     * los datos almacenados en la base de datos.
     *
     * Retorna true únicamente si coincide.
     */
    suspend fun verificar(
        password: String,
        passwordHash: String,
        passwordSalt: String
    ): Boolean
}
