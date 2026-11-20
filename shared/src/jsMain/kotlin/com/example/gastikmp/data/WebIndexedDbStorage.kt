
package com.example.gastikmp.data

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Almacenamiento persistente de Gasti Web.
 *
 * Plataforma: Kotlin/JS.
 * Motor: IndexedDB del navegador.
 *
 * Guarda pares clave/valor de forma persistente.
 *
 * No modifica las bases de datos SQLDelight
 * de Android ni Desktop.
 */
class WebIndexedDbStorage {

    companion object {
        private const val DATABASE_NAME = "gasti-web"
        private const val DATABASE_VERSION = 1
        private const val STORE_NAME = "registros"
    }

    /**
     * Abrir la base de datos del navegador.
     *
     * Si no existe, IndexedDB la crea
     * automáticamente junto con su almacén.
     */
    private suspend fun abrirBase(): dynamic =
        suspendCoroutine { continuation ->

            try {
                val indexedDb: dynamic =
                    js("globalThis.indexedDB")

                check(indexedDb != null) {
                    "IndexedDB no está disponible en este navegador"
                }

                val solicitud = indexedDb.open(
                    DATABASE_NAME,
                    DATABASE_VERSION
                )

                solicitud.onupgradeneeded = { _: dynamic ->

                    val base = solicitud.result

                    if (!base.objectStoreNames.contains(STORE_NAME)) {
                        base.createObjectStore(STORE_NAME)
                    }
                }

                solicitud.onsuccess = { _: dynamic ->
                    continuation.resume(solicitud.result)
                }

                solicitud.onerror = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "No se pudo abrir IndexedDB"
                        )
                    )
                }

            } catch (error: Throwable) {
                continuation.resumeWithException(error)
            }
        }

    /**
     * Guardar un valor.
     *
     * Si la clave ya existe, reemplaza su valor.
     */
    suspend fun guardar(
        clave: String,
        valor: String
    ) {
        require(clave.isNotBlank()) {
            "La clave no puede estar vacía"
        }

        val base = abrirBase()

        try {
            suspendCoroutine<Unit> { continuation ->

                val transaccion =
                    base.transaction(STORE_NAME, "readwrite")

                val almacen =
                    transaccion.objectStore(STORE_NAME)

                transaccion.oncomplete = { _: dynamic ->
                    continuation.resume(Unit)
                }

                transaccion.onerror = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "No se pudo guardar la información"
                        )
                    )
                }

                transaccion.onabort = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "Se canceló el guardado"
                        )
                    )
                }

                almacen.put(valor, clave)
            }

        } finally {
            base.close()
        }
    }

    /**
     * Leer el valor asociado a una clave.
     *
     * Devuelve null si no existe.
     */
    suspend fun leer(
        clave: String
    ): String? {

        require(clave.isNotBlank()) {
            "La clave no puede estar vacía"
        }

        val base = abrirBase()

        return try {
            suspendCoroutine { continuation ->

                val transaccion =
                    base.transaction(STORE_NAME, "readonly")

                val almacen =
                    transaccion.objectStore(STORE_NAME)

                val solicitud = almacen.get(clave)

                solicitud.onsuccess = { _: dynamic ->

                    val resultado: dynamic =
                        solicitud.result

                    if (resultado == null) {
                        continuation.resume(null)
                    } else {
                        continuation.resume(resultado as String)
                    }
                }

                solicitud.onerror = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "No se pudo leer la información"
                        )
                    )
                }
            }

        } finally {
            base.close()
        }
    }

    /**
     * Eliminar el valor asociado a una clave.
     */
    suspend fun eliminar(
        clave: String
    ) {
        require(clave.isNotBlank()) {
            "La clave no puede estar vacía"
        }

        val base = abrirBase()

        try {
            suspendCoroutine<Unit> { continuation ->

                val transaccion =
                    base.transaction(STORE_NAME, "readwrite")

                val almacen =
                    transaccion.objectStore(STORE_NAME)

                transaccion.oncomplete = { _: dynamic ->
                    continuation.resume(Unit)
                }

                transaccion.onerror = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "No se pudo eliminar la información"
                        )
                    )
                }

                transaccion.onabort = { _: dynamic ->
                    continuation.resumeWithException(
                        IllegalStateException(
                            "Se canceló la eliminación"
                        )
                    )
                }

                almacen.delete(clave)
            }

        } finally {
            base.close()
        }
    }
}
