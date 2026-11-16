
// Importamos las herramientas necesarias para configurar Kotlin Multiplatform.
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
        import org.jetbrains.kotlin.gradle.dsl.JvmTarget

        plugins {

            // Plugin principal de Kotlin Multiplatform.
            alias(libs.plugins.kotlinMultiplatform)

            // Plugin para configurar la biblioteca compartida de Android.
            alias(libs.plugins.androidMultiplatformLibrary)

            // Plugin para desarrollar interfaces con Compose Multiplatform.
            alias(libs.plugins.composeMultiplatform)

            // Plugin del compilador de Compose.
            alias(libs.plugins.composeCompiler)

            // Plugin de SQLDelight para generar código Kotlin desde archivos SQL.
            alias(libs.plugins.sqldelight)
        }


// ======================================================
// CONFIGURACIÓN DE KOTLIN MULTIPLATFORM
// ======================================================

kotlin {

    // Configuramos la plataforma JVM para Windows Desktop.
    jvm()

    // Configuramos JavaScript para ejecutar la aplicación en navegadores.
    js {
        browser()
        binaries.executable()
    }

    // Configuramos WebAssembly para navegadores compatibles.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    // Configuración de Android.
    android {

        // Nombre del espacio de nombres del módulo compartido.
        namespace = "com.example.gastikmp.shared"

        // Versión del SDK de compilación.
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        // Versión mínima de Android compatible.
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Configuración del compilador de Kotlin para Android.
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        // Permite utilizar recursos Android desde el módulo compartido.
        androidResources {
            enable = true
        }

        // Configuración de las pruebas Android.
        withHostTest {
            isIncludeAndroidResources = true
        }

        // Configuración de las pruebas en dispositivos Android.
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner =
                "androidx.test.runner.AndroidJUnitRunner"
        }
    }


    // ======================================================
    // CONFIGURACIÓN DE LOS CONJUNTOS DE CÓDIGO COMPARTIDO
    // ======================================================

    sourceSets {

        // --------------------------------------------------
        // ANDROID
        // --------------------------------------------------

        androidMain.dependencies {

            // Controlador de SQLDelight para Android.
            // Permite utilizar SQLite en dispositivos Android.
            implementation(libs.sqldelight.android.driver)

            // Herramientas para visualizar interfaces Compose.
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }


        // --------------------------------------------------
        // CÓDIGO COMPARTIDO
        // --------------------------------------------------

        commonMain.dependencies {

            // Biblioteca principal de SQLDelight.
            // Permite utilizar la base de datos desde Kotlin Multiplatform.
            implementation(libs.sqldelight.runtime)

            // Dependencias de Compose Multiplatform.
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // Dependencias para administrar los ViewModels.
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Biblioteca para almacenar configuraciones multiplataforma.
            implementation(libs.multiplatformSettings)
        }


        // --------------------------------------------------
        // PRUEBAS DEL CÓDIGO COMPARTIDO
        // --------------------------------------------------

        commonTest.dependencies {

            // Biblioteca de pruebas de Kotlin.
            implementation(libs.kotlin.test)
        }


        // --------------------------------------------------
        // WINDOWS DESKTOP / JVM
        // --------------------------------------------------

        jvmMain.dependencies {

            // Controlador SQLite para aplicaciones Desktop.
            // Permite guardar los gastos en una base de datos local.
            implementation(libs.sqldelight.sqlite.driver)
        }


        // --------------------------------------------------
        // JAVASCRIPT / WEB
        // --------------------------------------------------

        jsMain.dependencies {

            // Biblioteca para acceder a las funciones del navegador.
            implementation(libs.wrappers.browser)
        }


        // --------------------------------------------------
        // PRUEBAS DE ANDROID
        // --------------------------------------------------

        getByName("androidDeviceTest").dependencies {

            // Herramientas para ejecutar pruebas en dispositivos Android.
            implementation(libs.androidx.testRunner)
        }
    }
}


// ======================================================
// CONFIGURACIÓN DE SQLDELIGHT
// ======================================================

sqldelight {

    databases {

        // Creamos la base de datos principal de Gasti.
        create("GastiDatabase") {

            // Paquete donde SQLDelight generará las clases Kotlin.
            packageName.set("com.example.gastikmp.database")
        }
    }
}


// ======================================================
// DEPENDENCIAS ADICIONALES
// ======================================================

dependencies {

    // Herramientas de Compose para el entorno de ejecución Android.
    androidRuntimeClasspath(libs.compose.uiTooling)
}
