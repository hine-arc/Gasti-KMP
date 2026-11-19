
package com.example.gastikmp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.gastikmp.data.PasswordSecurity
import com.example.gastikmp.data.UsuarioRepositorio
import com.russhwolf.settings.Settings

/**
 * Autenticación principal de Gasti KMP.
 *
 * PASO 9:
 * Prepara el registro e inicio de sesión
 * persistentes mediante SQLDelight.
 *
 * Las funciones antiguas se conservan
 * temporalmente hasta conectar los
 * formularios de acceso.
 */
object AuthRepository {

    private val settings = Settings()

    // Almacenamiento anterior.
    // Se eliminará después de actualizar
    // LoginScreen y RegisterScreen.
    private val registeredUsers =
        mutableMapOf<String, Pair<String, String>>()

    // Componentes para autenticación permanente.
    private var usuarioRepositorio: UsuarioRepositorio? = null
    private var passwordSecurity: PasswordSecurity? = null

    // La sesión actual sigue siendo observable
    // por las pantallas Compose de Gasti.
    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        settings.remove("logged_email")
        settings.remove("logged_name")
    }

    /**
     * Conectar el almacenamiento permanente.
     *
     * Se llamará desde el arranque de Gasti
     * cuando tengamos la implementación de
     * PasswordSecurity correspondiente.
     */
    fun configurarPersistencia(
        repositorio: UsuarioRepositorio,
        seguridad: PasswordSecurity
    ) {
        usuarioRepositorio = repositorio
        passwordSecurity = seguridad
    }

    /**
     * Registro permanente.
     *
     * Utiliza un hash de contraseña y una
     * sal criptográfica, sin guardar la
     * contraseña original en SQLDelight.
     */
    suspend fun registrarPersistente(
        email: String,
        pass: String,
        name: String
    ): Result<User> {

        return runCatching {

            val correo = email.trim().lowercase()
            val nombre = name.trim()

            require(correo.isNotBlank()) {
                "El correo es obligatorio"
            }

            require(nombre.isNotBlank()) {
                "El nombre es obligatorio"
            }

            require(
                correo.contains("@") &&
                        correo.substringAfter("@").contains(".")
            ) {
                "Ingresa un correo electrónico válido"
            }

            require(pass.length >= 6) {
                "La contraseña debe tener mínimo 6 caracteres"
            }

            val repositorio = requireNotNull(
                usuarioRepositorio
            ) {
                "La base de datos de usuarios no está configurada"
            }

            val seguridad = requireNotNull(
                passwordSecurity
            ) {
                "La seguridad de contraseñas no está configurada"
            }

            require(!repositorio.existeCorreo(correo)) {
                "El correo ya se encuentra registrado"
            }

            // Calcular hash y sal mediante
            // la implementación de la plataforma.
            val protegida = seguridad.proteger(pass)

            // Guardar la cuenta en SQLDelight.
            repositorio.guardarUsuario(
                email = correo,
                nombre = nombre,
                passwordHash = protegida.passwordHash,
                passwordSalt = protegida.passwordSalt
            )

            val nuevoUsuario = User(
                email = correo,
                name = nombre
            )

            currentUser = nuevoUsuario

            nuevoUsuario
        }
    }

    /**
     * Inicio de sesión permanente.
     *
     * Recupera la cuenta desde SQLDelight
     * y verifica la contraseña protegida.
     */
    suspend fun iniciarSesionPersistente(
        email: String,
        pass: String
    ): Result<User> {

        return runCatching {

            val correo = email.trim().lowercase()

            require(
                correo.isNotBlank() && pass.isNotBlank()
            ) {
                "Ingresa tu correo y contraseña"
            }

            val repositorio = requireNotNull(
                usuarioRepositorio
            ) {
                "La base de datos de usuarios no está configurada"
            }

            val seguridad = requireNotNull(
                passwordSecurity
            ) {
                "La seguridad de contraseñas no está configurada"
            }

            val cuenta = repositorio.buscarPorCorreo(correo)

            // Usamos un mensaje general para
            // credenciales incorrectas.
            if (cuenta == null) {
                error("Correo o contraseña incorrectos")
            }

            val contraseñaCorrecta = seguridad.verificar(
                password = pass,
                passwordHash = cuenta.passwordHash,
                passwordSalt = cuenta.passwordSalt
            )

            if (!contraseñaCorrecta) {
                error("Correo o contraseña incorrectos")
            }

            val usuario = User(
                email = cuenta.email,
                name = cuenta.nombre
            )

            currentUser = usuario

            usuario
        }
    }

    /**
     * Registro anterior, temporal.
     *
     * Se conserva para no romper los
     * formularios existentes durante
     * esta transición.
     */
    @Deprecated(
        "Usar registrarPersistente después de conectar el formulario"
    )
    fun register(
        email: String,
        pass: String,
        name: String
    ): Result<User> {

        val correo = email.trim().lowercase()
        val nombre = name.trim()

        if (
            correo.isBlank() ||
            pass.isBlank() ||
            nombre.isBlank()
        ) {
            return Result.failure(
                Exception("Todos los campos son obligatorios")
            )
        }

        if (
            !correo.contains("@") ||
            !correo.substringAfter("@").contains(".")
        ) {
            return Result.failure(
                Exception("Ingresa un correo electrónico válido")
            )
        }

        if (pass.length < 6) {
            return Result.failure(
                Exception(
                    "La contraseña debe tener mínimo 6 caracteres"
                )
            )
        }

        if (registeredUsers.containsKey(correo)) {
            return Result.failure(
                Exception("El correo ya se encuentra registrado")
            )
        }

        registeredUsers[correo] = Pair(pass, nombre)

        val usuario = User(
            email = correo,
            name = nombre
        )

        currentUser = usuario

        return Result.success(usuario)
    }

    /**
     * Inicio de sesión anterior, temporal.
     */
    @Deprecated(
        "Usar iniciarSesionPersistente después de conectar el formulario"
    )
    fun login(
        email: String,
        pass: String
    ): Result<User> {

        val correo = email.trim().lowercase()

        if (correo.isBlank() || pass.isBlank()) {
            return Result.failure(
                Exception("Ingresa tu correo y contraseña")
            )
        }

        val guardado = registeredUsers[correo]

        if (
            guardado == null ||
            guardado.first != pass
        ) {
            return Result.failure(
                Exception("Correo o contraseña incorrectos")
            )
        }

        val usuario = User(
            email = correo,
            name = guardado.second
        )

        currentUser = usuario

        return Result.success(usuario)
    }

    /**
     * Cerrar sesión sin borrar la cuenta
     * ni sus datos financieros.
     */
    fun logout() {

        currentUser = null

        settings.remove("logged_email")
        settings.remove("logged_name")
    }
}
