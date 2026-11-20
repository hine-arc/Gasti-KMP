
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

kotlin {

    // WINDOWS DESKTOP
    jvm()

    // WEB JAVASCRIPT
    js {
        browser()
        binaries.executable()
    }

    // WEBASSEMBLY
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    // ANDROID
    android {
        namespace = "com.example.gastikmp.shared"

        compileSdk =
            libs.versions.android.compileSdk.get().toInt()

        minSdk =
            libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner =
                "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {

        // ANDROID
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }

        // CODIGO COMPARTIDO
        commonMain.dependencies {

            // SQLDelight
            implementation(libs.sqldelight.runtime)

            // Funciones para consultas asincronas.
            // Necesarias para el futuro controlador Web.
            implementation(
                "app.cash.sqldelight:async-extensions:2.3.2"
            )

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // Lifecycle
            implementation(
                libs.androidx.lifecycle.viewmodelCompose
            )

            implementation(
                libs.androidx.lifecycle.runtimeCompose
            )

            // Configuracion multiplataforma
            implementation(libs.multiplatformSettings)
        }

        // PRUEBAS COMPARTIDAS
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // WINDOWS DESKTOP
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }

        // WEB JAVASCRIPT
        jsMain.dependencies {

            implementation(libs.wrappers.browser)

            // Controlador SQLDelight Web.
            implementation(
                "app.cash.sqldelight:web-worker-driver:2.3.2"
            )

            // Trabajador oficial SQL.js.
            implementation(
                npm(
                    "@cashapp/sqldelight-sqljs-worker",
                    "2.3.2"
                )
            )

            // Motor SQLite del navegador.
            implementation(
                npm("sql.js", "1.8.0")
            )

            // Copia el archivo WebAssembly de SQL.js.
            implementation(
                devNpm("copy-webpack-plugin", "9.1.0")
            )
        }

        // WEBASSEMBLY
        wasmJsMain.dependencies {
            implementation(
                "app.cash.sqldelight:web-worker-driver:2.3.2"
            )
        }

        // PRUEBAS ANDROID
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

// SQLDELIGHT
sqldelight {
    databases {
        create("GastiDatabase") {

            packageName.set(
                "com.example.gastikmp.database"
            )

            // IMPORTANTE:
            // Todavia no activar generateAsync.
            //
            // Primero adaptaremos las consultas
            // y repositorios compartidos.
        }
    }
}

// HERRAMIENTAS ANDROID
dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
