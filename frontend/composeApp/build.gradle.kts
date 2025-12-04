import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.2.20"

}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                sourceMaps = false
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {

                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.compose.animation:animation")
            implementation("io.insert-koin:koin-core:4.1.0")
            implementation("io.insert-koin:koin-compose:4.1.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
            implementation("network.chaintech:qr-kit:3.1.3")
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
            implementation("io.ktor:ktor-client-core:3.3.2")
            implementation("io.ktor:ktor-client-cio:3.3.2")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.2")
            implementation("io.ktor:ktor-client-logging:3.3.2")

            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

            implementation("io.github.vinceglb:filekit-core:0.12.0")
            implementation("io.github.vinceglb:filekit-dialogs:0.12.0")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.12.0")
            implementation("io.github.vinceglb:filekit-coil:0.12.0")
            implementation(libs.kotlinx.datetime)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-java:3.3.2")
        }
        wasmJsMain.dependencies{
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            implementation("io.ktor:ktor-client-js:3.3.2")

        }
        androidMain.dependencies{
            implementation("androidx.activity:activity-compose:1.8.2")
            implementation("io.ktor:ktor-client-android:3.3.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
        }
        iosMain.dependencies{
            implementation("io.ktor:ktor-client-darwin:3.3.2")
        }
    }
}

android {
    namespace = "com.campusface"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.campusface"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


compose.desktop {
    application {
        mainClass = "com.campusface.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.campusface"
            packageVersion = "1.0.0"
        }
    }
}
