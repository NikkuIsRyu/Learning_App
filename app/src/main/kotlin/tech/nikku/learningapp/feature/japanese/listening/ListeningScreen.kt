package tech.nikku.learningapp.feature.japanese.listening

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.feature.shared.player.DialoguePlayerScreen

@Composable
fun ListeningRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ListeningViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ListeningScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@Composable
fun ListeningScreen(
  state: ListeningUiState,
  onEvent: (ListeningEvent) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (state.isLoading) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
  } else if (state.dialogue == null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No dialogue found for Chapter ${state.chapter}.")
    }
  } else {
    DialoguePlayerScreen(
      dialogue = state.dialogue,
      activeTurnIndex = state.activeTurnIndex,
      isPlaying = state.isPlaying,
      displayMode = state.displayMode,
      speed = state.playbackSpeed,
      autoPause = state.autoPause,
      onPlayPauseClick = { onEvent(ListeningEvent.TogglePlayPause) },
      onTurnClick = { turn -> onEvent(ListeningEvent.ReplayTurn(turn)) },
      onDisplayModeChange = { mode -> onEvent(ListeningEvent.ChangeDisplayMode(mode)) },
      onSpeedChange = { speed -> onEvent(ListeningEvent.ChangeSpeed(speed)) },
      onAutoPauseToggle = { enabled -> onEvent(ListeningEvent.ToggleAutoPause(enabled)) },
      onBackClick = onBackClick,
      modifier = modifier
    )
  }
}
