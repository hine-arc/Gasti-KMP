
package com.example.gastikmp.data

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Seguridad de contraseñas para Gasti Web JS.
 *
 * Algoritmo compatible con Android:
 * PBKDF2-HMAC-SHA256
 * 600000 iteraciones
 * Sal: 16 bytes
 * Hash: 32 bytes
 *
 * Usa Web Crypto del navegador.
 */
class WebPasswordSecurity : PasswordSecurity {

    companion object {
        private const val ITERATIONS = 600_000
        private const val SALT_LENGTH = 16
        private const val HASH_LENGTH = 32
        private const val PREFIX = "pbkdf2-sha256"
        private const val HEX = "0123456789abcdef"
    }

    /**
     * Obtener Web Crypto.
     */
    private fun obtenerCrypto(): dynamic {
        val crypto: dynamic = js("globalThis.crypto")

        check(crypto != null && crypto.subtle != null) {
            "Web Crypto no disponible. Usa HTTPS o localhost."
        }

        return crypto
    }

    /**
     * Convertir una promesa de JavaScript
     * en una suspensión de Kotlin.
     */
    private suspend fun esperar(
        promesa: dynamic
    ): dynamic = suspendCoroutine { continuation ->

        promesa.then(
            { resultado: dynamic ->
                continuation.resume(resultado)
            },
            { error: dynamic ->
                continuation.resumeWithException(
                    IllegalStateException(
                        "Error de Web Crypto: " +
                                error.toString()
                    )
                )
            }
        )
    }

    /**
     * Convertir bytes JavaScript a hexadecimal.
     */
    private fun bytesAHex(
        bytes: dynamic
    ): String {
        val resultado = StringBuilder()
        val longitud = bytes.length as Int

        for (i in 0 until longitud) {
            val valor = bytes[i] as Int

            resultado.append(HEX[valor ushr 4])
            resultado.append(HEX[valor and 15])
        }

        return resultado.toString()
    }

    /**
     * Convertir hexadecimal a Uint8Array.
     */
    private fun hexABytes(
        texto: String
    ): dynamic {
        require(texto.length % 2 == 0) {
            "Hexadecimal invalido"
        }

        val bytes: dynamic =
            js("new Uint8Array(16)")

        require(texto.length == SALT_LENGTH * 2) {
            "La sal debe contener 16 bytes"
        }

        for (i in 0 until SALT_LENGTH) {
            val primero =
                texto[i * 2].digitToInt(16)

            val segundo =
                texto[i * 2 + 1].digitToInt(16)

            bytes[i] = (primero shl 4) or segundo
        }

        return bytes
    }

    /**
     * Derivar una clave mediante PBKDF2.
     */
    private suspend fun derivarHash(
        password: String,
        saltHex: String,
        iterations: Int
    ): String {

        val crypto = obtenerCrypto()

        val encoder: dynamic =
            js("new TextEncoder()")

        val passwordBytes =
            encoder.encode(password)

        val usos = arrayOf("deriveBits")

        val clave = esperar(
            crypto.subtle.importKey(
                "raw",
                passwordBytes,
                "PBKDF2",
                false,
                usos
            )
        )

        val parametros: dynamic = js("({})")

        parametros.name = "PBKDF2"
        parametros.salt = hexABytes(saltHex)
        parametros.iterations = iterations
        parametros.hash = "SHA-256"

        val resultado = esperar(
            crypto.subtle.deriveBits(
                parametros,
                clave,
                HASH_LENGTH * 8
            )
        )

        val convertirBuffer: dynamic =
            js("(buffer) => new Uint8Array(buffer)")

        val bytes = convertirBuffer(resultado)

        return bytesAHex(bytes)
    }

    /**
     * Proteger una contraseña nueva.
     */
    override suspend fun proteger(
        password: String
    ): PasswordProtegida {

        require(password.isNotEmpty()) {
            "La contraseña no puede estar vacia"
        }

        val crypto = obtenerCrypto()

        val salt: dynamic =
            js("new Uint8Array(16)")

        crypto.getRandomValues(salt)

        val saltHex = bytesAHex(salt)

        val hashHex = derivarHash(
            password = password,
            saltHex = saltHex,
            iterations = ITERATIONS
        )

        check(hashHex.length == HASH_LENGTH * 2) {
            "Longitud incorrecta del hash"
        }

        return PasswordProtegida(
            passwordHash = "$PREFIX:$ITERATIONS:$hashHex",
            passwordSalt = saltHex
        )
    }

    /**
     * Verificar una contraseña almacenada.
     */
    override suspend fun verificar(
        password: String,
        passwordHash: String,
        passwordSalt: String
    ): Boolean {

        if (password.isEmpty()) {
            return false
        }

        return try {
            val partes = passwordHash.split(":")

            if (partes.size != 3) {
                return false
            }

            if (partes[0] != PREFIX) {
                return false
            }

            val iterations =
                partes[1].toIntOrNull() ?: return false

            if (iterations !in 1..ITERATIONS) {
                return false
            }

            val hashEsperado = partes[2].lowercase()

            if (
                hashEsperado.length != HASH_LENGTH * 2 ||
                passwordSalt.length != SALT_LENGTH * 2
            ) {
                return false
            }

            val hashCalculado = derivarHash(
                password = password,
                saltHex = passwordSalt,
                iterations = iterations
            )

            var diferencia = 0

            for (i in hashEsperado.indices) {
                diferencia = diferencia or (
                        hashEsperado[i].code xor
                                hashCalculado[i].code
                        )
            }

            diferencia == 0

        } catch (_: Exception) {
            false
        }
    }
}
