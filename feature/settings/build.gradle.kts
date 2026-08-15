// ─────────────────────────────────────────────────────────────
//  QryptIN — feature/settings/build.gradle.kts
// ─────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace  = "com.qryptin.settings"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)

    implementation(project(":core:designsystem"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:chat"))

    debugImplementation(libs.androidx.ui.tooling)
}
