package tech.nikku.learningapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
fun LearningAppTheme(
  domain: AppDomain = AppDomain.TODAY,
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  DomainTheme(
    domain = domain,
    darkTheme = darkTheme,
    content = content
  )
}
