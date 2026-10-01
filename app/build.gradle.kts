plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val fallbackKeystore = file("antigravity.keystore")
val releaseStorePath = System.getenv("STUDYTRACKER_KEYSTORE_PATH") ?: if (fallbackKeystore.exists()) fallbackKeystore.absolutePath else null
val releaseStorePassword = System.getenv("STUDYTRACKER_KEYSTORE_PASSWORD") ?: if (fallbackKeystore.exists()) "antigravity_android_key" else null
val releaseKeyAlias = System.getenv("STUDYTRACKER_KEY_ALIAS") ?: if (fallbackKeystore.exists()) "antigravity" else null
val releaseKeyPassword = System.getenv("STUDYTRACKER_KEY_PASSWORD") ?: if (fallbackKeystore.exists()) "antigravity_android_key" else null
val hasReleaseSigning = !releaseStorePath.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank() &&
    file(releaseStorePath!!).exists()

android {
    namespace = "com.studytracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.studytracker"
        minSdk = 26
        targetSdk = 36
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["appName"] = "StudyTracker"

        val defaultV2Url = System.getenv("STUDYTRACKER_V2_BASE_URL") ?: "https://studytracker-sync.osman13241429.workers.dev"
        val stagingV2Url = System.getenv("STUDYTRACKER_V2_STAGING_URL") ?: "https://studytracker-v2-staging.osman13241429.workers.dev"
        buildConfigField("String", "V2_BASE_URL", "\"$defaultV2Url\"")
        buildConfigField("String", "V2_STAGING_URL", "\"$stagingV2Url\"")
    }

    flavorDimensions += "role"
    productFlavors {
        create("child") {
            dimension = "role"
            applicationIdSuffix = ".child"
            versionNameSuffix = "-child"
            manifestPlaceholders["appName"] = "StudyTracker Öğrenci"
            buildConfigField("String", "APP_ROLE", "\"CHILD\"")
        }
        create("parent") {
            dimension = "role"
            applicationIdSuffix = ".parent"
            versionNameSuffix = "-parent"
            manifestPlaceholders["appName"] = "StudyTracker Veli"
            buildConfigField("String", "APP_ROLE", "\"PARENT\"")
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseV2Url = System.getenv("STUDYTRACKER_V2_BASE_URL") ?: "https://studytracker-sync.osman13241429.workers.dev"
            buildConfigField("String", "V2_BASE_URL", "\"$releaseV2Url\"")
        }
        debug {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
            applicationIdSuffix = ".debug"
            val debugV2Url = System.getenv("STUDYTRACKER_V2_STAGING_URL") ?: System.getenv("STUDYTRACKER_V2_BASE_URL") ?: "https://studytracker-v2-staging.osman13241429.workers.dev"
            buildConfigField("String", "V2_BASE_URL", "\"$debugV2Url\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.savedstate:savedstate-ktx:1.2.1")

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Kotlinx Serialization JSON
    implementation(libs.kotlinx.serialization.json)

    // Networking (OkHttp for Supabase REST, Realtime & Storage)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
