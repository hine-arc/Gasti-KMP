
/**
 * GASTI KMP
 *
 * Configuracion Webpack para:
 * - SQLDelight
 * - SQL.js
 * - SQLite WebAssembly
 * - Trabajador SQLite persistente
 */

const path = require("path");
const fs = require("fs");
const CopyWebpackPlugin = require("copy-webpack-plugin");

// Compatibilidad con SQL.js en navegador.

config.resolve = config.resolve || {};

config.resolve.fallback = {
    ...(config.resolve.fallback || {}),
    fs: false,
    path: false,
    crypto: false
};

// Webpack ejecuta esta configuracion desde:
//
// build/js/packages/GastiKMP-webApp/
//
// Subimos cuatro niveles para llegar
// a la raiz del proyecto Gasti-KMP.

const raizProyecto = path.resolve(
    __dirname,
    "../../../.."
);

// Ubicacion real del trabajador creado
// dentro del modulo webApp.

const trabajadorGasti = path.join(
    raizProyecto,
    "webApp",
    "src",
    "jsMain",
    "resources",
    "gasti-sqlite-worker.js"
);

// Comprobar que el archivo existe.

if (!fs.existsSync(trabajadorGasti)) {
    throw new Error(
        "No se encontro el trabajador SQLite en: " +
        trabajadorGasti
    );
}

// Copiar los archivos necesarios
// al directorio publico Web.

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: require.resolve(
                    "sql.js/dist/sql-wasm.js"
                ),
                to: "sql-wasm.js"
            },
            {
                from: require.resolve(
                    "sql.js/dist/sql-wasm.wasm"
                ),
                to: "sql-wasm.wasm"
            },
            {
                from: trabajadorGasti,
                to: "gasti-sqlite-worker.js"
            }
        ]
    })
);
