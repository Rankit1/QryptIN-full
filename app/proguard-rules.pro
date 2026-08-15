# ─────────────────────────────────────────────────────────────
#  QryptIN — proguard-rules.pro
# ─────────────────────────────────────────────────────────────

# Keep Kotlin metadata (required for reflection / coroutines)
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes SourceFile,LineNumberTable

# Kotlin serialization / coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ViewModel (keep names for SavedStateHandle key look-ups)
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
