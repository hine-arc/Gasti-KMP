
package com.example.gastikmp.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Protección de contraseñas para Android.
 *
 * Algoritmo: PBKDF2-HMAC-SHA256.
 * Iteraciones: 600000.
 * Sal aleatoria: 16 bytes.
 * Hash derivado: 32 bytes.
 *
 * No guarda contraseñas en texto plano.
 *
 * IMPORTANTE:
 * Las operaciones son costosas y deben llamarse
 * fuera del hilo principal de la interfaz.
 */
class AndroidPasswordSecurity : PasswordSecurity {

    companion object {
        private const val ITERATIONS = 600_000
        private const val SALT_LENGTH = 16
        private const val HASH_LENGTH = 32

        private const val ALGORITHM = "HmacSHA256"
        private const val HASH_PREFIX = "pbkdf2-sha256"
    }

    private val secureRandom = SecureRandom()

    /**
     * Proteger una contraseña nueva.
     */
    override suspend fun proteger(
        password: String
    ): PasswordProtegida {

        require(password.isNotEmpty()) {
            "La contraseña no puede estar vacía"
        }

        // Crear una sal criptográficamente aleatoria.
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)

        // Derivar el hash.
        val hash = deriveKey(
            password = password,
            salt = salt,
            iterations = ITERATIONS
        )

        // Guardar también el número de iteraciones
        // para permitir actualizaciones posteriores.
        val storedHash =
            "$HASH_PREFIX:$ITERATIONS:${hash.toHex()}"

        return PasswordProtegida(
            passwordHash = storedHash,
            passwordSalt = salt.toHex()
        )
    }

    /**
     * Comprobar una contraseña ingresada.
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

            val parts = passwordHash.split(":")

            if (parts.size != 3) {
                return false
            }

            if (parts[0] != HASH_PREFIX) {
                return false
            }

            val iterations =
                parts[1].toIntOrNull() ?: return false

            if (iterations < 1 ||
                iterations > ITERATIONS
            ) {
                return false
            }

            val expectedHash = parts[2].hexToBytes()
            val salt = passwordSalt.hexToBytes()

            if (expectedHash.size != HASH_LENGTH ||
                salt.size != SALT_LENGTH
            ) {
                return false
            }

            val calculatedHash = deriveKey(
                password = password,
                salt = salt,
                iterations = iterations
            )

            // Comparación resistente a diferencias
            // de tiempo entre bytes.
            MessageDigest.isEqual(
                calculatedHash,
                expectedHash
            )

        } catch (_: Exception) {
            false
        }
    }

    /**
     * Implementación PBKDF2-HMAC-SHA256.
     *
     * Se utiliza HMAC-SHA256 directamente para
     * mantener compatibilidad con Android API 24+.
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

        // PBKDF2: primer bloque, índice 1.
        val block = ByteArray(salt.size + 4)

        salt.copyInto(block)

        block[block.lastIndex] = 1

        var u = mac.doFinal(block)

        val result = u.copyOf()

        // U2 hasta U(iterations).
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
     * Convertir bytes a texto hexadecimal.
     */
    private fun ByteArray.toHex(): String {

        val digits = "0123456789abcdef"
        val result = StringBuilder(size * 2)

        for (byte in this) {

            val value = byte.toInt() and 0xFF

            result.append(digits[value ushr 4])
            result.append(digits[value and 0x0F])
        }

        return result.toString()
    }

    /**
     * Convertir texto hexadecimal a bytes.
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
