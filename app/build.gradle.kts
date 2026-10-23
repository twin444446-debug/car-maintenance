plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Добавляем плагин для Compose (версия должна совпадать с версией Kotlin)
    id("org.jetbrains.kotlin.plugin.compose") version "1.9.22" 
}

android {
    namespace = "com.example.carmaintenance"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carmaintenance"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Включаем поддержку Compose
    buildFeatures {
        compose = true
    }
}

// Блок с библиотеками (зависимостями)
dependencies {
    // Основные библиотеки для Compose (UI, анимации, Material Design)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM (управляет версиями всех Compose-библиотек)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Библиотека для загрузки красивых картинок (машин)
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // Для инструментов предпросмотра (не обязательно, но полезно)
    debugImplementation("androidx.compose.ui:ui-tooling")
}
