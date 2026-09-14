// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false

    // ==================== NOVAS PLUGINS PARA SINTETIZADOR ====================
    id("com.android.kotlin-style") version "2.1.3" // plugin oficial para FluidSynth (Android 2025+)
    id("org.jetbrains.kotlin.kapt") version "2.1.3" // para KTX e dependências FluidSynth
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

group = "com.joathannes.pedsc"
version = "1.0"

android {
    namespace = "com.joathannes.pedsc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.joathannes.pedsc"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // ==================== DEPENDÊNCIAS PARA SÍNTESE ====================
    dependencies {
        implementation(platform(libs.androidx.bom))
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.compose.bom))
        implementation(libs.ui)
        implementation(libs.ui.graphics)
        implementation(libs.ui.tooling.preview)
        implementation(libs.material3)
        implementation(libs.material.icons.extended)

        // ==================== MOTOR DE SÍNTESE (FLUIDSYNTH) ====================
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.media:media:1.7.0") // para MediaPlayer e MIDI
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

        // Plugin oficial para FluidSynth (2025/2026)
        implementation("org.joska.fluidsynth:fluidsynth-android:2.5.0")
        implementation("org.joska.fluidsynth:fluidsynth-ktx:2.5.0") // wrapper Kotlin oficial
    }
}

dependencies {
    implementation(project(":wrapper"))
}

tasks.register("generateFluidSynthSdk") {
    doLast {
        println("✅ SDK do FluidSynth já configurado via plugin oficial.")
    }
}
