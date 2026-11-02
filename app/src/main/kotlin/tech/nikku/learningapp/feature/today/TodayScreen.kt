package tech.nikku.learningapp.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tech.nikku.learningapp.R
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

data class TodayUiState(
  val dueCount: Int = 0,
  val streakDays: Int = 0,
  val isLoading: Boolean = false
)

sealed interface TodayEvent {
  object StartReview : TodayEvent
}

@Composable
fun TodayRoute(
  modifier: Modifier = Modifier,
  onStartReview: () -> Unit = {}
) {
  TodayScreen(
    state = TodayUiState(dueCount = 12, streakDays = 5),
    onEvent = { event ->
      when (event) {
        TodayEvent.StartReview -> onStartReview()
      }
    },
    modifier = modifier
  )
}

@Composable
fun TodayScreen(
  state: TodayUiState,
  onEvent: (TodayEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(R.string.today_title),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.primary
    )

    Card(
      modifier = Modifier.padding(top = 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      )
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "${state.dueCount}",
          style = MaterialTheme.typography.displayLarge,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
          text = "Reviews due today",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

@Preview(name = "Today Screen Light", showBackground = true)
@Composable
fun TodayScreenPreview() {
  LearningAppTheme(domain = AppDomain.TODAY) {
    Surface {
      TodayScreen(
        state = TodayUiState(dueCount = 15, streakDays = 7),
        onEvent = {}
      )
    }
  }
}
