package tech.nikku.learningapp.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import tech.nikku.learningapp.R
import tech.nikku.learningapp.core.designsystem.theme.AppDomain

enum class TopLevelDestination(
  val route: Any,
  val icon: ImageVector,
  @StringRes val labelRes: Int,
  val domain: AppDomain,
  val contentDescription: String
) {
  TODAY(
    route = TodayRoute,
    icon = Icons.Default.DateRange,
    labelRes = R.string.nav_today,
    domain = AppDomain.TODAY,
    contentDescription = "Today reviews and daily dashboard"
  ),
  JAPANESE(
    route = JapaneseRoute,
    icon = Icons.Default.Star,
    labelRes = R.string.nav_japanese,
    domain = AppDomain.JAPANESE,
    contentDescription = "Japanese curriculum"
  ),
  CHINESE(
    route = ChineseRoute,
    icon = Icons.Default.Create,
    labelRes = R.string.nav_chinese,
    domain = AppDomain.CHINESE,
    contentDescription = "Chinese companion"
  ),
  VAULT(
    route = VaultRoute,
    icon = Icons.Default.Info,
    labelRes = R.string.nav_vault,
    domain = AppDomain.VAULT,
    contentDescription = "Computer Science academic vault"
  )
}
