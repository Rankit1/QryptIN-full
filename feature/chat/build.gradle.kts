// ─────────────────────────────────────────────────────────────
//  QryptIN — feature/chat/build.gradle.kts
// ─────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "com.qryptin.chat"
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
    // ── Core ────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Compose BOM ─────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    // ── Navigation ──────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // ── Local persistence (Room) — schema ready, wired in a later pass ──
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ── Image loading ───────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Internal: shared design system + sibling features ───
    implementation(project(":core:designsystem"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:contacts"))

    // ── Networking & WebSockets ─────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // ── Debug ───────────────────────────────────────────────
    debugImplementation(libs.androidx.ui.tooling)
}
