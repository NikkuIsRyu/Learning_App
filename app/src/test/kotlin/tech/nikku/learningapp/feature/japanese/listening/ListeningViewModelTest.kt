package tech.nikku.learningapp.feature.japanese.listening

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.nikku.learningapp.core.domain.model.Dialogue
import tech.nikku.learningapp.core.domain.model.DialogueTurn
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.repository.DialogueRepository
import tech.nikku.learningapp.core.domain.repository.MediaRepository
import tech.nikku.learningapp.core.domain.repository.MediaResolution
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.feature.shared.player.AudioPlayerController
import tech.nikku.learningapp.feature.shared.player.DisplayMode
import tech.nikku.learningapp.feature.shared.player.TtsController

class FakeDialogueRepository : DialogueRepository {
  var dialogueMap = mutableMapOf<String, Dialogue>()

  override fun observeDialogues(domain: Domain): Flow<List<Dialogue>> = flowOf(dialogueMap.values.toList())
  override suspend fun getDialogueById(id: String): Dialogue? = dialogueMap[id]
}

class FakeMediaRepository : MediaRepository {
  override suspend fun resolve(ref: String): MediaResolution = MediaResolution.NotFound
}

class FakeSettingsRepository : SettingsRepository {
  private val speedFlow = MutableStateFlow(1.0f)

  override fun observeToneColorsEnabled(): Flow<Boolean> = flowOf(true)
  override suspend fun setToneColorsEnabled(enabled: Boolean) {}
  override fun observeFuriganaMode(): Flow<String> = flowOf("ALWAYS")
  override suspend fun setFuriganaMode(mode: String) {}
  override fun observeDarkTheme(): Flow<Boolean?> = flowOf(null)
  override suspend fun setDarkTheme(darkTheme: Boolean?) {}
  override fun observeUiLanguage(): Flow<String> = flowOf("EN")
  override suspend fun setUiLanguage(language: String) {}
  override fun observeDailyNewLimit(): Flow<Int> = flowOf(20)
  override suspend fun setDailyNewLimit(limit: Int) {}
  override fun observeActiveChapters(): Flow<Set<Int>> = flowOf(setOf(26))
  override suspend fun setActiveChapters(chapters: Set<Int>) {}
  override fun observePlaybackSpeed(): Flow<Float> = speedFlow
  override suspend fun setPlaybackSpeed(speed: Float) { speedFlow.value = speed }
  override fun observeContentVersion(): Flow<Int> = flowOf(1)
  override suspend fun setContentVersion(version: Int) {}
  override fun observeResetAt(): Flow<Long> = flowOf(0L)
  override suspend fun setResetAt(timestamp: Long) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ListeningViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var context: Context
  private lateinit var dialogueRepo: FakeDialogueRepository
  private lateinit var mediaRepo: FakeMediaRepository
  private lateinit var settingsRepo: FakeSettingsRepository
  private lateinit var audioPlayer: AudioPlayerController
  private lateinit var ttsController: TtsController

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    context = ApplicationProvider.getApplicationContext()
    dialogueRepo = FakeDialogueRepository()
    mediaRepo = FakeMediaRepository()
    settingsRepo = FakeSettingsRepository()
    audioPlayer = AudioPlayerController(context)
    ttsController = TtsController(context)
  }

  @After
  fun tearDown() {
    audioPlayer.release()
    ttsController.release()
    Dispatchers.resetMain()
  }

  @Test
  fun initialLoad_loadsDialogueAndDefaultDisplayMode() = runTest {
    val viewModel = ListeningViewModel(
      dialogueRepository = dialogueRepo,
      mediaRepository = mediaRepo,
      settingsRepository = settingsRepo,
      audioPlayer = audioPlayer,
      ttsController = ttsController,
      savedStateHandle = SavedStateHandle(mapOf("chapter" to 26))
    )

    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(26, state.chapter)
    assertNotNull(state.dialogue)
    assertEquals(DisplayMode.FURIGANA, state.displayMode)
    assertEquals(1.0f, state.playbackSpeed)
  }

  @Test
  fun changeDisplayMode_updatesStateProperly() = runTest {
    val viewModel = ListeningViewModel(
      dialogueRepository = dialogueRepo,
      mediaRepository = mediaRepo,
      settingsRepository = settingsRepo,
      audioPlayer = audioPlayer,
      ttsController = ttsController,
      savedStateHandle = SavedStateHandle(mapOf("chapter" to 26))
    )
    advanceUntilIdle()

    viewModel.onEvent(ListeningEvent.ChangeDisplayMode(DisplayMode.KANJI_ONLY))
    assertEquals(DisplayMode.KANJI_ONLY, viewModel.uiState.value.displayMode)

    viewModel.onEvent(ListeningEvent.ChangeDisplayMode(DisplayMode.HIDDEN))
    assertEquals(DisplayMode.HIDDEN, viewModel.uiState.value.displayMode)

    viewModel.onEvent(ListeningEvent.ChangeDisplayMode(DisplayMode.ROMAJI))
    assertEquals(DisplayMode.ROMAJI, viewModel.uiState.value.displayMode)
  }

  @Test
  fun changeSpeed_persistsToSettingsAndUpdatesState() = runTest {
    val viewModel = ListeningViewModel(
      dialogueRepository = dialogueRepo,
      mediaRepository = mediaRepo,
      settingsRepository = settingsRepo,
      audioPlayer = audioPlayer,
      ttsController = ttsController,
      savedStateHandle = SavedStateHandle(mapOf("chapter" to 26))
    )
    advanceUntilIdle()

    viewModel.onEvent(ListeningEvent.ChangeSpeed(1.25f))
    advanceUntilIdle()

    assertEquals(1.25f, viewModel.uiState.value.playbackSpeed)
  }

  @Test
  fun toggleAutoPause_togglesState() = runTest {
    val viewModel = ListeningViewModel(
      dialogueRepository = dialogueRepo,
      mediaRepository = mediaRepo,
      settingsRepository = settingsRepo,
      audioPlayer = audioPlayer,
      ttsController = ttsController,
      savedStateHandle = SavedStateHandle(mapOf("chapter" to 26))
    )
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.autoPause)
    viewModel.onEvent(ListeningEvent.ToggleAutoPause(true))
    assertTrue(viewModel.uiState.value.autoPause)
  }

  @Test
  fun replayTurn_updatesActiveTurnIndex() = runTest {
    val viewModel = ListeningViewModel(
      dialogueRepository = dialogueRepo,
      mediaRepository = mediaRepo,
      settingsRepository = settingsRepo,
      audioPlayer = audioPlayer,
      ttsController = ttsController,
      savedStateHandle = SavedStateHandle(mapOf("chapter" to 26))
    )
    advanceUntilIdle()

    val turn = DialogueTurn(
      dialogueId = "ja:dia:26:01",
      idx = 1,
      speaker = "A",
      text = "テスト文です。",
      startMs = 2000L,
      endMs = 4500L
    )

    viewModel.onEvent(ListeningEvent.ReplayTurn(turn))
    assertEquals(1, viewModel.uiState.value.activeTurnIndex)
  }
}
