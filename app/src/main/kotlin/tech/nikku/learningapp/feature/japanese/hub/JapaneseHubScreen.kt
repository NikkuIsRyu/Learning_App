package tech.nikku.learningapp.feature.japanese.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

data class JapaneseHubUiState(
  val totalChapters: Int = 50,
  val verifiedChapters: List<Int> = listOf(26, 27, 28, 29, 30, 31, 32),
  val isLoading: Boolean = false
)

sealed interface JapaneseHubEvent {
  data class SelectChapter(val chapter: Int) : JapaneseHubEvent
}

@Composable
fun JapaneseHubRoute(
  modifier: Modifier = Modifier,
  onChapterClick: (Int) -> Unit = {}
) {
  JapaneseHubScreen(
    state = JapaneseHubUiState(),
    onEvent = { event ->
      when (event) {
        is JapaneseHubEvent.SelectChapter -> onChapterClick(event.chapter)
      }
    },
    modifier = modifier
  )
}

@Composable
fun JapaneseHubScreen(
  state: JapaneseHubUiState,
  onEvent: (JapaneseHubEvent) -> Unit,
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
      text = stringResource(R.string.japanese_hub_title),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.primary
    )

    FuriganaText(
      text = "{皆|みんな}の{日|に}{本|ほん}{語|ご} 初級 I & II (Chapters 1–50)",
      style = MaterialTheme.typography.titleMedium
    )

    Text(
      text = "Verified Chapters: 26–32 with Errata Fixes",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.secondary
    )
  }
}

@Preview(name = "Japanese Hub Preview", showBackground = true)
@Composable
fun JapaneseHubPreview() {
  LearningAppTheme(domain = AppDomain.JAPANESE) {
    Surface {
      JapaneseHubScreen(
        state = JapaneseHubUiState(),
        onEvent = {}
      )
    }
  }
}
