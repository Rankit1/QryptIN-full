// ─────────────────────────────────────────────────────────────
//  QryptIN — core/designsystem/build.gradle.kts
//
//  Shared design system library.
//  Both :feature:auth and :feature:contacts depend on this.
//  Your existing ui/theme/*.kt files live here.
// ─────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace  = "com.qryptin.core.designsystem"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.material3)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.material.icons.extended)

    // Google Fonts (Poppins)
    api(libs.androidx.compose.ui.text.google.fonts)

    // Coil (shared image loading)
    api(libs.coil.compose)

    // Navigation (Required for QryptBottomNav)
    api(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
