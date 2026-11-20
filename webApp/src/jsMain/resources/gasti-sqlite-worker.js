
/**
 * GASTI KMP
 *
 * Trabajador SQLite Web con IndexedDB.
 *
 * Compatible con el formato de respuesta
 * del WebWorkerDriver de SQLDelight 2.3.2.
 *
 * Persistencia en fase de pruebas.
 */

importScripts("/sql-wasm.js");

const NOMBRE_DB = "gasti-sqlite";
const ALMACEN = "bases";
const CLAVE = "gasti-principal";

let sqlite = null;
let enTransaccion = false;

/**
 * Abrir IndexedDB.
 */
function abrirAlmacen() {

    return new Promise((resolve, reject) => {

        const solicitud = indexedDB.open(
            NOMBRE_DB,
            1
        );

        solicitud.onupgradeneeded = () => {

            const conexion = solicitud.result;

            if (
                !conexion.objectStoreNames.contains(
                    ALMACEN
                )
            ) {
                conexion.createObjectStore(ALMACEN);
            }
        };

        solicitud.onsuccess = () => {
            resolve(solicitud.result);
        };

        solicitud.onerror = () => {
            reject(solicitud.error);
        };
    });
}

/**
 * Recuperar SQLite desde IndexedDB.
 */
async function leerBase() {

    const conexion = await abrirAlmacen();

    try {

        return await new Promise((resolve, reject) => {

            const transaccion = conexion.transaction(
                ALMACEN,
                "readonly"
            );

            const solicitud = transaccion
                .objectStore(ALMACEN)
                .get(CLAVE);

            solicitud.onsuccess = () => {
                resolve(solicitud.result || null);
            };

            solicitud.onerror = () => {
                reject(solicitud.error);
            };
        });

    } finally {

        conexion.close();
    }
}

/**
 * Guardar la base SQLite completa.
 */
async function guardarBase() {

    const contenido = sqlite.export();

    const conexion = await abrirAlmacen();

    try {

        await new Promise((resolve, reject) => {

            const transaccion = conexion.transaction(
                ALMACEN,
                "readwrite"
            );

            transaccion.objectStore(ALMACEN)
                .put(contenido, CLAVE);

            transaccion.oncomplete = () => {
                resolve();
            };

            transaccion.onerror = () => {
                reject(transaccion.error);
            };

            transaccion.onabort = () => {
                reject(
                    transaccion.error ||
                    new Error(
                        "No se pudo guardar SQLite"
                    )
                );
            };
        });

    } finally {

        conexion.close();
    }
}

/**
 * Inicializar SQL.js.
 */
async function iniciarSQLite() {

    const SQL = await initSqlJs({
        locateFile: () => "/sql-wasm.wasm"
    });

    const guardada = await leerBase();

    if (guardada) {

        sqlite = new SQL.Database(
            new Uint8Array(guardada)
        );

        console.log(
            "GASTI SQLITE: base recuperada"
        );

    } else {

        sqlite = new SQL.Database();

        console.log(
            "GASTI SQLITE: base nueva"
        );
    }
}

/**
 * Ejecutar consultas SQL con parámetros.
 *
 * IMPORTANTE:
 *
 * SQLDelight 2.3.2 espera un objeto:
 *
 * {
 *   columns: ["nombre", "monto"],
 *   values: [
 *     ["Comida", 15000]
 *   ]
 * }
 *
 * No debemos devolver directamente
 * un array de filas.
 */
function ejecutarSQL(sql, parametros) {

    const sentencia = sqlite.prepare(sql);

    try {

        if (parametros && parametros.length > 0) {
            sentencia.bind(parametros);
        }

        const columnas = sentencia.getColumnNames();

        const filas = [];

        while (sentencia.step()) {
            filas.push(sentencia.get());
        }

        // Formato que utiliza el trabajador
        // oficial de SQLDelight.
        return {
            columns: columnas,
            values: filas
        };

    } finally {

        sentencia.free();
    }
}

/**
 * Procesar instrucciones SQLDelight.
 */
async function procesar(mensaje) {

    let resultados;

    switch (mensaje.action) {

        case "exec": {

            if (!mensaje.sql) {
                throw new Error(
                    "La instrucción SQL está vacía."
                );
            }

            resultados = ejecutarSQL(
                mensaje.sql,
                mensaje.params || []
            );

            const escritura = !/^\s*(SELECT|EXPLAIN)\b/i
                .test(mensaje.sql);

            if (
                escritura &&
                !enTransaccion
            ) {
                await guardarBase();
            }

            break;
        }

        case "begin_transaction": {

            sqlite.exec("BEGIN TRANSACTION");

            enTransaccion = true;

            resultados = [];

            break;
        }

        case "end_transaction": {

            sqlite.exec("COMMIT");

            enTransaccion = false;

            await guardarBase();

            resultados = [];

            break;
        }

        case "rollback_transaction": {

            sqlite.exec("ROLLBACK");

            enTransaccion = false;

            resultados = [];

            break;
        }

        default: {

            throw new Error(
                "Operación desconocida: " +
                mensaje.action
            );
        }
    }

    self.postMessage({
        id: mensaje.id,
        results: resultados
    });
}

/**
 * Inicializar SQL.js una sola vez.
 */
const preparado = iniciarSQLite();

/**
 * Ejecutar los mensajes en orden
 * para evitar operaciones simultáneas.
 */
let cola = Promise.resolve();

self.onmessage = (evento) => {

    const mensaje = evento.data;

    cola = cola.then(async () => {

        try {

            await preparado;

            await procesar(mensaje);

        } catch (error) {

            console.error(
                "GASTI SQLITE:",
                error
            );

            self.postMessage({
                id: mensaje.id,
                error: {
                    message:
                        error.message ||
                        "Error SQLite desconocido"
                }
            });
        }
    });
};
