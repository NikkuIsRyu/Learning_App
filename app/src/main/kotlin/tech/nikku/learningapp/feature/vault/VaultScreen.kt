package tech.nikku.learningapp.feature.vault

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
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

data class VaultUiState(
  val isWip: Boolean = true
)

sealed interface VaultEvent {
  object NotifyMe : VaultEvent
}

@Composable
fun VaultRoute(
  modifier: Modifier = Modifier
) {
  VaultScreen(
    state = VaultUiState(),
    onEvent = {},
    modifier = modifier
  )
}

@Composable
fun VaultScreen(
  state: VaultUiState,
  onEvent: (VaultEvent) -> Unit,
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
      text = stringResource(R.string.vault_title),
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = stringResource(R.string.vault_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.secondary
    )
  }
}

@Preview(name = "Vault Preview", showBackground = true)
@Composable
fun VaultPreview() {
  LearningAppTheme(domain = AppDomain.VAULT) {
    Surface {
      VaultScreen(
        state = VaultUiState(),
        onEvent = {}
      )
    }
  }
}
