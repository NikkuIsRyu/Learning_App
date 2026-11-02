package tech.nikku.learningapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tech.nikku.learningapp.core.designsystem.theme.DomainTheme
import tech.nikku.learningapp.feature.today.TodayRoute
import tech.nikku.learningapp.navigation.AppNavHost
import tech.nikku.learningapp.navigation.TopLevelDestination

@Composable
fun LearningAppRoot(
  navController: NavHostController = rememberNavController()
) {
  var selectedDestination by rememberSaveable { mutableStateOf(TopLevelDestination.TODAY) }

  DomainTheme(domain = selectedDestination.domain) {
    NavigationSuiteScaffold(
      navigationSuiteItems = {
        TopLevelDestination.entries.forEach { destination ->
          item(
            selected = (selectedDestination == destination),
            onClick = {
              if (selectedDestination != destination) {
                selectedDestination = destination
                navController.navigate(destination.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            },
            icon = {
              Icon(
                imageVector = destination.icon,
                contentDescription = destination.contentDescription
              )
            },
            label = { Text(text = stringResource(destination.labelRes)) }
          )
        }
      }
    ) {
      Surface(modifier = Modifier.fillMaxSize()) {
        AppNavHost(navController = navController)
      }
    }
  }
}

@Composable
fun LearningAppRootScaffold(
  selectedDestination: TopLevelDestination,
  onDestinationSelected: (TopLevelDestination) -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  DomainTheme(domain = selectedDestination.domain) {
    NavigationSuiteScaffold(
      modifier = modifier,
      navigationSuiteItems = {
        TopLevelDestination.entries.forEach { destination ->
          item(
            selected = (selectedDestination == destination),
            onClick = { onDestinationSelected(destination) },
            icon = {
              Icon(
                imageVector = destination.icon,
                contentDescription = destination.contentDescription
              )
            },
            label = { Text(text = stringResource(destination.labelRes)) }
          )
        }
      }
    ) {
      Surface(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}

@Preview(name = "App Shell Phone (Today)", showBackground = true)
@Composable
fun LearningAppRootPreview() {
  LearningAppRootScaffold(
    selectedDestination = TopLevelDestination.TODAY,
    onDestinationSelected = {}
  ) {
    TodayRoute()
  }
}

@Preview(name = "App Shell Tablet (Navigation Rail)", device = Devices.TABLET, showBackground = true)
@Composable
fun LearningAppRootTabletPreview() {
  LearningAppRootScaffold(
    selectedDestination = TopLevelDestination.JAPANESE,
    onDestinationSelected = {}
  ) {
    TodayRoute()
  }
}
