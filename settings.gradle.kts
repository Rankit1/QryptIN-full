// ─────────────────────────────────────────────────────────────
//  QryptIN — settings.gradle.kts
// ─────────────────────────────────────────────────────────────
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "2.0.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "QryptIN"

// ── Modules ──────────────────────────────────────────────────
include(":app")
include(":core:designsystem")
include(":feature:auth")
include(":feature:contacts")
include(":feature:chat")
include(":feature:calls")
include(":feature:settings")
