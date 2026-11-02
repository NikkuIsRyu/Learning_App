package tech.nikku.learningapp.feature.chinese

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
import tech.nikku.learningapp.core.designsystem.component.PinyinChar
import tech.nikku.learningapp.core.designsystem.component.PinyinText
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

data class ChineseHubUiState(
  val grammarRuleCount: Int = 26,
  val confusableCount: Int = 6,
  val dialogueCount: Int = 18,
  val isLoading: Boolean = false
)

sealed interface ChineseHubEvent {
  object OpenGrammar : ChineseHubEvent
}

@Composable
fun ChineseHubRoute(
  modifier: Modifier = Modifier,
  onOpenGrammar: () -> Unit = {}
) {
  ChineseHubScreen(
    state = ChineseHubUiState(),
    onEvent = { event ->
      when (event) {
        ChineseHubEvent.OpenGrammar -> onOpenGrammar()
      }
    },
    modifier = modifier
  )
}

@Composable
fun ChineseHubScreen(
  state: ChineseHubUiState,
  onEvent: (ChineseHubEvent) -> Unit,
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
      text = stringResource(R.string.chinese_hub_title),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.primary
    )

    PinyinText(
      characters = listOf(
        PinyinChar("中", "zhong1", 1),
        PinyinChar("文", "wen2", 2),
        PinyinChar("语", "yu3", 3),
        PinyinChar("法", "fa3", 3),
        PinyinChar("伴", "ban4", 4),
        PinyinChar("侣", "lv3", 3)
      ),
      showToneColors = true
    )

    Text(
      text = "${state.grammarRuleCount} Grammar Rules · ${state.confusableCount} Confusable Pairs · ${state.dialogueCount} Dialogues",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.secondary
    )
  }
}

@Preview(name = "Chinese Hub Preview", showBackground = true)
@Composable
fun ChineseHubPreview() {
  LearningAppTheme(domain = AppDomain.CHINESE) {
    Surface {
      ChineseHubScreen(
        state = ChineseHubUiState(),
        onEvent = {}
      )
    }
  }
}
