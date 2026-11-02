package tech.nikku.learningapp.feature.japanese.listening

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.engine.audio.TurnLocator
import tech.nikku.learningapp.core.domain.model.Dialogue
import tech.nikku.learningapp.core.domain.model.DialogueTurn
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.SpeakerInfo
import tech.nikku.learningapp.core.domain.repository.DialogueRepository
import tech.nikku.learningapp.core.domain.repository.MediaRepository
import tech.nikku.learningapp.core.domain.repository.MediaResolution
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.feature.shared.player.AudioPlayerController
import tech.nikku.learningapp.feature.shared.player.DisplayMode
import tech.nikku.learningapp.feature.shared.player.TtsController
import java.util.Locale
import javax.inject.Inject

data class ListeningUiState(
  val isLoading: Boolean = true,
  val chapter: Int = 26,
  val dialogue: Dialogue? = null,
  val activeTurnIndex: Int = -1,
  val isPlaying: Boolean = false,
  val displayMode: DisplayMode = DisplayMode.FURIGANA,
  val playbackSpeed: Float = 1.0f,
  val autoPause: Boolean = false,
  val errorMessage: String? = null
)

sealed interface ListeningEvent {
  data object TogglePlayPause : ListeningEvent
  data class ReplayTurn(val turn: DialogueTurn) : ListeningEvent
  data class ChangeDisplayMode(val mode: DisplayMode) : ListeningEvent
  data class ChangeSpeed(val speed: Float) : ListeningEvent
  data class ToggleAutoPause(val enabled: Boolean) : ListeningEvent
}

@HiltViewModel
class ListeningViewModel @Inject constructor(
  private val dialogueRepository: DialogueRepository,
  private val mediaRepository: MediaRepository,
  private val settingsRepository: SettingsRepository,
  private val audioPlayer: AudioPlayerController,
  private val ttsController: TtsController,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val chapterArg: Int = savedStateHandle.get<Int>("chapter") ?: 26

  private val _uiState = MutableStateFlow(ListeningUiState(chapter = chapterArg))
  val uiState: StateFlow<ListeningUiState> = _uiState.asStateFlow()

  private var resolvedMediaUri: Uri? = null

  init {
    observeSettings()
    observePlayer()
    loadDialogue()
  }

  private fun observeSettings() {
    viewModelScope.launch {
      settingsRepository.observePlaybackSpeed().collectLatest { speed ->
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
        audioPlayer.setSpeed(speed)
      }
    }
  }

  private fun observePlayer() {
    viewModelScope.launch {
      audioPlayer.isPlaying.collectLatest { playing ->
        _uiState.value = _uiState.value.copy(isPlaying = playing)
      }
    }

    viewModelScope.launch {
      audioPlayer.currentPositionMs.collectLatest { posMs ->
        val dialogue = _uiState.value.dialogue ?: return@collectLatest
        val activeIndex = TurnLocator.locateTurnIndex(dialogue.turns, posMs)
        if (activeIndex != _uiState.value.activeTurnIndex) {
          _uiState.value = _uiState.value.copy(activeTurnIndex = activeIndex)
          if (_uiState.value.autoPause && activeIndex != -1) {
            // Shadowing auto-pause mode at end of sentence
          }
        }
      }
    }
  }

  private fun loadDialogue() {
    viewModelScope.launch {
      val dialogues = dialogueRepository.observeDialogues(Domain.JAPANESE)
      // Check if dialogue exists in DB for this chapter
      var selected = dialogueRepository.getDialogueById("ja:dia:$chapterArg:01")

      if (selected == null) {
        // Create standard listening set for chapter
        selected = createFallbackChapterDialogue(chapterArg)
      }

      // Resolve audioRef
      val audioRef = selected.audioRef
      if (audioRef != null) {
        val resolution = mediaRepository.resolve(audioRef)
        resolvedMediaUri = when (resolution) {
          is MediaResolution.FilePath -> Uri.parse("file://${resolution.absolutePath}")
          is MediaResolution.AssetPath -> Uri.parse("asset:///${resolution.assetPath}")
          else -> null
        }
      }

      _uiState.value = _uiState.value.copy(
        isLoading = false,
        dialogue = selected
      )
    }
  }

  fun onEvent(event: ListeningEvent) {
    when (event) {
      is ListeningEvent.TogglePlayPause -> togglePlayPause()
      is ListeningEvent.ReplayTurn -> replayTurn(event.turn)
      is ListeningEvent.ChangeDisplayMode -> changeDisplayMode(event.mode)
      is ListeningEvent.ChangeSpeed -> changeSpeed(event.speed)
      is ListeningEvent.ToggleAutoPause -> toggleAutoPause(event.enabled)
    }
  }

  private fun togglePlayPause() {
    if (_uiState.value.isPlaying) {
      audioPlayer.pause()
      ttsController.stop()
    } else {
      val uri = resolvedMediaUri
      if (uri != null) {
        audioPlayer.playUri(uri)
      } else {
        // Fallback to TTS sequential playback
        playTtsFromTurn(_uiState.value.activeTurnIndex.coerceAtLeast(0))
      }
    }
  }

  private fun replayTurn(turn: DialogueTurn) {
    val uri = resolvedMediaUri
    if (uri != null && turn.startMs != null) {
      audioPlayer.playTurn(uri, turn)
      _uiState.value = _uiState.value.copy(activeTurnIndex = turn.idx)
    } else {
      // TTS fallback
      _uiState.value = _uiState.value.copy(activeTurnIndex = turn.idx)
      ttsController.speak(turn.text, Locale.JAPANESE, _uiState.value.playbackSpeed)
    }
  }

  private fun playTtsFromTurn(startIdx: Int) {
    val dialogue = _uiState.value.dialogue ?: return
    val turns = dialogue.turns
    if (startIdx !in turns.indices) return

    val turn = turns[startIdx]
    _uiState.value = _uiState.value.copy(activeTurnIndex = startIdx, isPlaying = true)

    ttsController.speak(turn.text, Locale.JAPANESE, _uiState.value.playbackSpeed) {
      if (_uiState.value.autoPause) {
        _uiState.value = _uiState.value.copy(isPlaying = false)
      } else if (startIdx + 1 < turns.size) {
        playTtsFromTurn(startIdx + 1)
      } else {
        _uiState.value = _uiState.value.copy(isPlaying = false, activeTurnIndex = -1)
      }
    }
  }

  private fun changeDisplayMode(mode: DisplayMode) {
    _uiState.value = _uiState.value.copy(displayMode = mode)
  }

  private fun changeSpeed(speed: Float) {
    viewModelScope.launch {
      settingsRepository.setPlaybackSpeed(speed)
    }
  }

  private fun toggleAutoPause(enabled: Boolean) {
    _uiState.value = _uiState.value.copy(autoPause = enabled)
  }

  private fun createFallbackChapterDialogue(chapter: Int): Dialogue {
    return Dialogue(
      id = "ja:dia:$chapter:01",
      domain = Domain.JAPANESE,
      chapter = chapter,
      titleEn = "Chapter $chapter Listening Practice",
      titleDe = "Kapitel $chapter Hörverständnis",
      nativeTitle = "第${chapter}課 会話と聴解",
      topicEn = "Dialogue and conversational practice",
      audioRef = "media/ja/dialogue/l${chapter}_01.mp3",
      durationMs = 12000L,
      speakers = mapOf(
        "A" to SpeakerInfo(label = "田中", role = "Colleague", gender = "M", colorHex = "#1E88E5"),
        "B" to SpeakerInfo(label = "佐藤", role = "Colleague", gender = "F", colorHex = "#43A047")
      ),
      turns = listOf(
        DialogueTurn(
          dialogueId = "ja:dia:$chapter:01",
          idx = 0,
          speaker = "A",
          text = "来週の金曜日に何をしますか。",
          rubyMarkup = "{来週|らいしゅう}の {金曜日|きんようび}に {何|なに}を しますか。",
          romanization = "Raishū no kinyōbi ni nani o shimasu ka.",
          translationEn = "What will you do next Friday?",
          translationDe = "Was machst du nächsten Freitag?",
          startMs = 500L,
          endMs = 3200L
        ),
        DialogueTurn(
          dialogueId = "ja:dia:$chapter:01",
          idx = 1,
          speaker = "B",
          text = "友達と京都へ行こうと思っています。",
          rubyMarkup = "{友達|ともだち}と {京都|きょうと}へ {行|い}こうと {思|おも}って います。",
          romanization = "Tomodachi to Kyōto e ikō to omotte imasu.",
          translationEn = "I'm thinking of going to Kyoto with a friend.",
          translationDe = "Ich habe vor, mit einem Freund nach Kyoto zu fahren.",
          startMs = 3800L,
          endMs = 8200L
        ),
        DialogueTurn(
          dialogueId = "ja:dia:$chapter:01",
          idx = 2,
          speaker = "A",
          text = "いいですね。気をつけて行ってきてください。",
          rubyMarkup = "いいですね。{気|き}をつけて {行|い}ってきて ください。",
          romanization = "Ii desu ne. Ki o tsukete ittekite kudasai.",
          translationEn = "That sounds great. Have a safe trip!",
          translationDe = "Das klingt gut. Pass auf dich auf und gute Reise!",
          startMs = 8800L,
          endMs = 12500L
        )
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    audioPlayer.release()
    ttsController.release()
  }
}
