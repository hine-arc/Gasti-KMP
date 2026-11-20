
/**
 * GASTI KMP
 *
 * Configuración de SQLDelight y SQL.js
 * para Web JavaScript.
 *
 * Esta configuración:
 *
 * 1. Prepara Webpack para SQL.js.
 * 2. Copia sql-wasm.wasm al navegador.
 * 3. Conserva las demás opciones
 *    existentes de Webpack.
 *
 * No modifica Android ni Desktop.
 */

// Compatibilidad con módulos de Node.js
// que no están disponibles en el navegador.

config.resolve = config.resolve || {};

config.resolve.fallback = {
    ...(config.resolve.fallback || {}),
    fs: false,
    path: false,
    crypto: false
};

// Copiar el motor SQLite WebAssembly
// a los archivos públicos de Gasti Web.

const CopyWebpackPlugin = require("copy-webpack-plugin");

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: require.resolve(
                    "sql.js/dist/sql-wasm.wasm"
                ),
                to: "sql-wasm.wasm"
            }
        ]
    })
);
