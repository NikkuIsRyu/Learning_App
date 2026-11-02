package tech.nikku.learningapp.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Domain Seed Colors (Spec §10.1)
val JapaneseSakura = Color(0xFFC2185B)
val ChineseVermilion = Color(0xFFC62828)
val VaultTeal = Color(0xFF00796B)
val TodayIndigo = Color(0xFF303F9F)

// Tone Colors (Pleco-style, configurable, contrast >= 4.5:1)
// Tone 1: Flat/High (Red)
val Tone1Light = Color(0xFFE53935)
val Tone1Dark = Color(0xFFEF5350)

// Tone 2: Rising (Green)
val Tone2Light = Color(0xFF43A047)
val Tone2Dark = Color(0xFF66BB6A)

// Tone 3: Low/Dipping (Blue)
val Tone3Light = Color(0xFF1E88E5)
val Tone3Dark = Color(0xFF42A5F5)

// Tone 4: Falling (Purple)
val Tone4Light = Color(0xFF8E24AA)
val Tone4Dark = Color(0xFFAB47BC)

// Neutral Tone (Grey)
val ToneNeutralLight = Color(0xFF757575)
val ToneNeutralDark = Color(0xFFBDBDBD)

fun toneColor(tone: Int, isDark: Boolean): Color {
  return when (tone) {
    1 -> if (isDark) Tone1Dark else Tone1Light
    2 -> if (isDark) Tone2Dark else Tone2Light
    3 -> if (isDark) Tone3Dark else Tone3Light
    4 -> if (isDark) Tone4Dark else Tone4Light
    else -> if (isDark) ToneNeutralDark else ToneNeutralLight
  }
}

// Syntax Role Colors (Formula Pills - Spec §10.1)
val RoleSubjectLight = Color(0xFF1976D2)
val RoleSubjectDark = Color(0xFF64B5F6)

val RoleTopicLight = Color(0xFF3949AB)
val RoleTopicDark = Color(0xFF7986CB)

val RoleTimeLight = Color(0xFFF57F17)
val RoleTimeDark = Color(0xFFFFD54F)

val RolePlaceLight = Color(0xFF00897B)
val RolePlaceDark = Color(0xFF4DB6AC)

val RoleModalNegationLight = Color(0xFFD81B60)
val RoleModalNegationDark = Color(0xFFF06292)

val RoleVerbLight = Color(0xFFE53935)
val RoleVerbDark = Color(0xFFE57373)

val RoleObjectLight = Color(0xFF2E7D32)
val RoleObjectDark = Color(0xFF81C784)

val RoleParticleLight = Color(0xFF616161)
val RoleParticleDark = Color(0xFFB0BEC5)

val RoleMeasureLight = Color(0xFF6D4C41)
val RoleMeasureDark = Color(0xFFA1887F)

val RoleConnectorLight = Color(0xFF455A64)
val RoleConnectorDark = Color(0xFF90A4AE)
