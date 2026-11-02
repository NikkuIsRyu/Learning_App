package tech.nikku.learningapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppDomain {
  TODAY,
  JAPANESE,
  CHINESE,
  VAULT
}

val LocalAppDomain = staticCompositionLocalOf { AppDomain.TODAY }

private val JapaneseLightColorScheme = lightColorScheme(
  primary = JapaneseSakura,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFD9E2),
  onPrimaryContainer = Color(0xFF3E001D),
  secondary = Color(0xFF74565F),
  onSecondary = Color.White,
  background = Color(0xFFFFF8F8),
  surface = Color(0xFFFFF8F8),
  onSurface = Color(0xFF201A1B)
)

private val JapaneseDarkColorScheme = darkColorScheme(
  primary = Color(0xFFFFB0C8),
  onPrimary = Color(0xFF5E1130),
  primaryContainer = Color(0xFF7B2946),
  onPrimaryContainer = Color(0xFFFFD9E2),
  secondary = Color(0xFFE3BDC6),
  onSecondary = Color(0xFF422931),
  background = Color(0xFF181113),
  surface = Color(0xFF181113),
  onSurface = Color(0xFFEBE0E1)
)

private val ChineseLightColorScheme = lightColorScheme(
  primary = ChineseVermilion,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFDAD6),
  onPrimaryContainer = Color(0xFF410002),
  secondary = Color(0xFF775653),
  onSecondary = Color.White,
  background = Color(0xFFFFF8F7),
  surface = Color(0xFFFFF8F7),
  onSurface = Color(0xFF201A19)
)

private val ChineseDarkColorScheme = darkColorScheme(
  primary = Color(0xFFFFB4AB),
  onPrimary = Color(0xFF690005),
  primaryContainer = Color(0xFF93000A),
  onPrimaryContainer = Color(0xFFFFDAD6),
  secondary = Color(0xFFE7BDB8),
  onSecondary = Color(0xFF442926),
  background = Color(0xFF181211),
  surface = Color(0xFF181211),
  onSurface = Color(0xFFEDE0DE)
)

private val VaultLightColorScheme = lightColorScheme(
  primary = VaultTeal,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFA4F2E6),
  onPrimaryContainer = Color(0xFF00201B),
  secondary = Color(0xFF4A635E),
  onSecondary = Color.White,
  background = Color(0xFFF4FAF8),
  surface = Color(0xFFF4FAF8),
  onSurface = Color(0xFF161D1B)
)

private val VaultDarkColorScheme = darkColorScheme(
  primary = Color(0xFF88D5CA),
  onPrimary = Color(0xFF003730),
  primaryContainer = Color(0xFF005047),
  onPrimaryContainer = Color(0xFFA4F2E6),
  secondary = Color(0xFFB1CCC6),
  onSecondary = Color(0xFF1C3530),
  background = Color(0xFF0E1513),
  surface = Color(0xFF0E1513),
  onSurface = Color(0xFFDEE4E1)
)

private val TodayLightColorScheme = lightColorScheme(
  primary = TodayIndigo,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFDEE0FF),
  onPrimaryContainer = Color(0xFF00105C),
  secondary = Color(0xFF5B5D72),
  onSecondary = Color.White,
  background = Color(0xFFFAF8FF),
  surface = Color(0xFFFAF8FF),
  onSurface = Color(0xFF1A1B21)
)

private val TodayDarkColorScheme = darkColorScheme(
  primary = Color(0xFFBAC3FF),
  onPrimary = Color(0xFF08218A),
  primaryContainer = Color(0xFF2339A2),
  onPrimaryContainer = Color(0xFFDEE0FF),
  secondary = Color(0xFFC3C5DD),
  onSecondary = Color(0xFF2D2F42),
  background = Color(0xFF121318),
  surface = Color(0xFF121318),
  onSurface = Color(0xFFE3E1E9)
)

fun domainColorScheme(domain: AppDomain, darkTheme: Boolean): ColorScheme {
  return when (domain) {
    AppDomain.JAPANESE -> if (darkTheme) JapaneseDarkColorScheme else JapaneseLightColorScheme
    AppDomain.CHINESE -> if (darkTheme) ChineseDarkColorScheme else ChineseLightColorScheme
    AppDomain.VAULT -> if (darkTheme) VaultDarkColorScheme else VaultLightColorScheme
    AppDomain.TODAY -> if (darkTheme) TodayDarkColorScheme else TodayLightColorScheme
  }
}

@Composable
fun DomainTheme(
  domain: AppDomain,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = domainColorScheme(domain, darkTheme)
  CompositionLocalProvider(LocalAppDomain provides domain) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      content = content
    )
  }
}
