
package com.example.gastikmp.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Protección de contraseñas para Windows Desktop.
 *
 * Utiliza PBKDF2 con HMAC-SHA256.
 *
 * Mantiene el mismo formato de almacenamiento
 * que AndroidPasswordSecurity.
 *
 * No almacena contraseñas originales.
 */
class DesktopPasswordSecurity : PasswordSecurity {

    companion object {

        private const val ITERATIONS = 600_000

        private const val SALT_LENGTH = 16

        private const val HASH_LENGTH = 32

        private const val ALGORITHM = "HmacSHA256"

        private const val HASH_PREFIX = "pbkdf2-sha256"
    }

    private val secureRandom = SecureRandom()

    /**
     * Protege una contraseña nueva.
     *
     * Genera una sal aleatoria y deriva
     * el hash mediante PBKDF2-HMAC-SHA256.
     */
    override suspend fun proteger(
        password: String
    ): PasswordProtegida {

        require(password.isNotEmpty()) {
            "La contraseña no puede estar vacía"
        }

        // Generar una sal aleatoria.
        val salt = ByteArray(SALT_LENGTH)

        secureRandom.nextBytes(salt)

        // Derivar el hash.
        val hash = deriveKey(
            password = password,
            salt = salt,
            iterations = ITERATIONS
        )

        // Guardar algoritmo, iteraciones y hash.
        val storedHash =
            "$HASH_PREFIX:$ITERATIONS:${hash.toHex()}"

        return PasswordProtegida(
            passwordHash = storedHash,
            passwordSalt = salt.toHex()
        )
    }

    /**
     * Verifica una contraseña ingresada.
     *
     * Devuelve true si coincide con
     * el hash almacenado.
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

            // Separar algoritmo, iteraciones y hash.
            val parts = passwordHash.split(":")

            if (parts.size != 3) {
                return false
            }

            // Comprobar algoritmo.
            if (parts[0] != HASH_PREFIX) {
                return false
            }

            // Obtener iteraciones.
            val iterations =
                parts[1].toIntOrNull() ?: return false

            if (
                iterations < 1 ||
                iterations > ITERATIONS
            ) {
                return false
            }

            // Convertir valores guardados a bytes.
            val expectedHash =
                parts[2].hexToBytes()

            val salt =
                passwordSalt.hexToBytes()

            // Validar tamaños.
            if (
                expectedHash.size != HASH_LENGTH ||
                salt.size != SALT_LENGTH
            ) {
                return false
            }

            // Derivar nuevamente el hash.
            val calculatedHash = deriveKey(
                password = password,
                salt = salt,
                iterations = iterations
            )

            // Comparación de tiempo constante.
            MessageDigest.isEqual(
                calculatedHash,
                expectedHash
            )

        } catch (_: Exception) {

            false
        }
    }

    /**
     * Implementación de PBKDF2-HMAC-SHA256.
     *
     * Utiliza los mismos bytes UTF-8
     * y el mismo algoritmo que Android.
     */
    private fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int
    ): ByteArray {

        val passwordBytes =
            password.encodeToByteArray()

        val mac = Mac.getInstance(ALGORITHM)

        mac.init(
            SecretKeySpec(
                passwordBytes,
                ALGORITHM
            )
        )

        // Primer bloque PBKDF2.
        val block = ByteArray(salt.size + 4)

        salt.copyInto(block)

        block[block.lastIndex] = 1

        // Primera iteración.
        var u = mac.doFinal(block)

        val result = u.copyOf()

        // Iteraciones restantes.
        for (iteration in 2..iterations) {

            u = mac.doFinal(u)

            for (index in result.indices) {

                result[index] = (
                        result[index].toInt() xor
                                u[index].toInt()
                        ).toByte()
            }
        }

        return result.copyOf(HASH_LENGTH)
    }

    /**
     * Convierte bytes a hexadecimal.
     */
    private fun ByteArray.toHex(): String {

        val digits = "0123456789abcdef"

        val result = StringBuilder(size * 2)

        for (byte in this) {

            val value = byte.toInt() and 0xFF

            result.append(
                digits[value ushr 4]
            )

            result.append(
                digits[value and 0x0F]
            )
        }

        return result.toString()
    }

    /**
     * Convierte hexadecimal a bytes.
     */
    private fun String.hexToBytes(): ByteArray {

        require(length % 2 == 0) {
            "Hexadecimal inválido"
        }

        return ByteArray(length / 2) { index ->

            val first =
                this[index * 2].digitToInt(16)

            val second =
                this[index * 2 + 1].digitToInt(16)

            ((first shl 4) or second).toByte()
        }
    }
}
