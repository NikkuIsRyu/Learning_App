package tech.nikku.learningapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import tech.nikku.learningapp.feature.chinese.ChineseHubRoute
import tech.nikku.learningapp.feature.japanese.chapter.ChapterRoute
import tech.nikku.learningapp.feature.japanese.conjugation.ConjugationDrillRoute as ConjugationDrillDestination
import tech.nikku.learningapp.feature.japanese.conjugation.ConjugationMatrixRoute as ConjugationMatrixDestination
import tech.nikku.learningapp.feature.japanese.particle.ParticleClozeRoute as ParticleClozeDestination
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
      JapaneseHubRoute(
        onChapterClick = { chapter ->
          navController.navigate(ChapterDetailRoute(chapter))
        }
      )
    }
    composable<ChapterDetailRoute> { backStackEntry ->
      val route = backStackEntry.toRoute<ChapterDetailRoute>()
      ChapterRoute(
        chapter = route.chapter,
        onBackClick = { navController.popBackStack() },
        onStartDrill = { _ -> navController.navigate(ConjugationMatrixRoute(route.chapter)) }
      )
    }
    composable<ConjugationMatrixRoute> {
      ConjugationMatrixDestination(
        onNavigateToDrill = { wordId, form ->
          navController.navigate(ConjugationDrillRoute(wordId = wordId, targetForm = form?.name))
        },
        onBackClick = { navController.popBackStack() }
      )
    }
    composable<ConjugationDrillRoute> {
      ConjugationDrillDestination(
        onBackClick = { navController.popBackStack() }
      )
    }
    composable<ParticleClozeRoute> {
      ParticleClozeDestination(
        onBackClick = { navController.popBackStack() }
      )
    }
    composable<ChineseRoute> {
      ChineseHubRoute()
    }
    composable<VaultRoute> {
      VaultRoute()
    }
  }
}
