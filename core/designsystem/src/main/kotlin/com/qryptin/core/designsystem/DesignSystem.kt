// ─────────────────────────────────────────────────────────────
//  com.qryptin.core.designsystem
//
//  This package is the public API of the design system module.
//
//  WHAT GOES HERE:
//  Copy (or move) these files from your existing ui/ folder
//  into this module, keeping their original package declarations
//  exactly as they are:
//
//    ui/theme/Color.kt          → keep package com.qryptin.ui.theme
//    ui/theme/ColorSchemes.kt   → keep package com.qryptin.ui.theme
//    ui/theme/Typography.kt     → keep package com.qryptin.ui.theme
//    ui/theme/Dimensions.kt     → keep package com.qryptin.ui.theme
//    ui/theme/Theme.kt          → keep package com.qryptin.ui.theme
//    ui/animations/Animations.kt
//    ui/components/GradientBackground.kt
//    ui/components/QryptCard.kt
//    ui/components/QryptSearchBar.kt
//    ui/components/QryptBottomNav.kt
//    ui/components/UiAtoms.kt
//    ui/components/ChatListItem.kt
//    ui/screens/HomeScreen.kt
//    ui/screens/home/*.kt
//
//  FONT ASSETS:
//  If using bundled Poppins (Option B from README), copy your
//  .ttf files to:
//    core/designsystem/src/main/res/font/
//
//  Feature modules import via:
//    implementation(project(":core:designsystem"))
//  and import normally:
//    import com.qryptin.ui.theme.QryptINTheme
// ─────────────────────────────────────────────────────────────
package com.qryptin.core.designsystem

// Re-export marker — no actual code needed here.
// The package simply signals to Gradle/IDE where the module boundary is.
