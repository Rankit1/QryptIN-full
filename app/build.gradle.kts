// ─────────────────────────────────────────────────────────────
//  QryptIN — app/build.gradle.kts
//  Application module — wires all features together
// ─────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services) 
}

android {
    namespace   = "com.qryptin"
    compileSdk  = 35

    defaultConfig {
        applicationId  = "com.qryptin"
        minSdk         = 26
        targetSdk      = 35
        versionCode    = 1
        versionName    = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            //applicationIdSuffix = ".debug"
            isDebuggable        = true
        }
    }

    buildFeatures {
        compose      = true
        buildConfig  = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ── Feature modules ──────────────────────────────────────
    implementation(project(":feature:auth"))
    implementation(project(":feature:contacts"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:calls"))
    implementation(project(":feature:settings"))

    // ── Shared design system ─────────────────────────────────
    implementation(project(":core:designsystem"))

    // ── Room (needed to access ChatDatabase in MainActivity) ──
    //implementation(libs.androidx.room.runtime)

    // ── Compose BOM ──────────────────────────────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ── Compose ──────────────────────────────────────────────
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material.icons.extended)

    // ── Navigation ───────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Lifecycle ────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Activity ─────────────────────────────────────────────
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    // ── Splash screen ────────────────────────────────────────
    implementation(libs.androidx.core.splashscreen)

    // ── Debug ────────────────────────────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Firebase ─────────────────────────────────────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // ── Retrofit (for backend API calls) ─────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // WebSockets (STOMP) - Using a light wrapper for OkHttp
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

}
