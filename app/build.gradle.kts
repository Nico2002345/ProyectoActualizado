plugins {
    alias(libs.plugins.android.application)
    // Si algún día agregas clases Kotlin:
    // alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.ingsoftcalvoproy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ingsoftcalvoproy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 Habilitar soporte para íconos vectoriales
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 🔹 Puedes habilitar logs o configuración de test aquí si lo necesitas
            isMinifyEnabled = false
        }
    }

    // 🔹 Compilación con Java 17 (compatible con tu JDK)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 🔹 Habilita ViewBinding (opcional, pero útil)
    buildFeatures {
        viewBinding = true
    }

    // 🔹 Evita errores por conflictos de recursos
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0"
            )
        }
    }
}

dependencies {
    // --- 📦 Dependencias base AndroidX ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- 🧩 Componentes visuales ---
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // --- ⚙️ Ciclo de vida (para Activities y LiveData, si usas en el futuro) ---
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.6")

    // --- 🧪 Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
