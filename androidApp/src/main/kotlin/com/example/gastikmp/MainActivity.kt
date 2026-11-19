
package com.example.gastikmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.example.gastikmp.data.AndroidPasswordSecurity
import com.example.gastikmp.data.DatabaseDriverFactory
import com.example.gastikmp.data.UsuarioRepositorio
import com.example.gastikmp.database.GastiDatabase

/**
 * Actividad principal de Gasti para Android.
 *
 * Inicializa SQLDelight y conecta el repositorio
 * de usuarios con la protección de contraseñas.
 *
 * La base de datos es la misma que utilizan
 * Gastos, Presupuestos, Estadísticas y Home.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // 1. Abrir la base de datos local de Gasti.
        val driver = DatabaseDriverFactory(
            applicationContext
        ).createDriver()

        val database = GastiDatabase(driver)

        // 2. Crear el repositorio que guarda usuarios
        //    permanentemente en SQLDelight.
        val usuarioRepositorio = UsuarioRepositorio(
            database = database
        )

        // 3. Crear el servicio de protección
        //    de contraseñas de Android.
        val passwordSecurity = AndroidPasswordSecurity()

        // 4. Conectar ambos componentes al
        //    repositorio principal de autenticación.
        AuthRepository.configurarPersistencia(
            repositorio = usuarioRepositorio,
            seguridad = passwordSecurity
        )

        // 5. Mostrar Gasti con la misma base de datos.
        setContent {
            App(database)
        }
    }
}
