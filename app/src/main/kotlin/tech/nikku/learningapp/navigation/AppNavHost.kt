package tech.nikku.learningapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tech.nikku.learningapp.feature.chinese.ChineseHubRoute
import tech.nikku.learningapp.feature.japanese.hub.JapaneseHubRoute
import tech.nikku.learningapp.feature.today.TodayRoute
import tech.nikku.learningapp.feature.vault.VaultRoute

@Composable
fun AppNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = TodayRoute,
    modifier = modifier
  ) {
    composable<TodayRoute> {
      TodayRoute()
    }
    composable<JapaneseRoute> {
      JapaneseHubRoute()
    }
    composable<ChineseRoute> {
      ChineseHubRoute()
    }
    composable<VaultRoute> {
      VaultRoute()
    }
  }
}
