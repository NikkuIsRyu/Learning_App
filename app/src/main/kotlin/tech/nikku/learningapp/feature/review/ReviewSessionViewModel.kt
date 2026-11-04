package tech.nikku.learningapp.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.core.domain.usecase.StartReviewSessionUseCase
import javax.inject.Inject

data class ReviewSessionStats(
  val againCount: Int = 0,
  val hardCount: Int = 0,
  val goodCount: Int = 0,
  val easyCount: Int = 0
) {
  val total: Int get() = againCount + hardCount + goodCount + easyCount
}

data class ReviewSessionUiState(
  val items: List<ReviewItem> = emptyList(),
  val currentIndex: Int = 0,
  val isAnswerRevealed: Boolean = false,
  val isFinished: Boolean = false,
  val isLoading: Boolean = true,
  val stats: ReviewSessionStats = ReviewSessionStats()
) {
  val currentItem: ReviewItem? get() = items.getOrNull(currentIndex)
  val progressFraction: Float
    get() = if (items.isNotEmpty()) (currentIndex.toFloat() / items.size.toFloat()).coerceIn(0f, 1f) else 0f
}

sealed interface ReviewSessionEvent {
  data object RevealAnswer : ReviewSessionEvent
  data class SubmitGrade(val grade: ReviewGrade) : ReviewSessionEvent
}

@HiltViewModel
class ReviewSessionViewModel @Inject constructor(
  private val progressRepository: ProgressRepository,
  private val settingsRepository: SettingsRepository,
  private val lexiconRepository: LexiconRepository,
  private val kanjiRepository: KanjiRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val domainFilter: Domain? = savedStateHandle.get<String>("domain")?.let { str ->
    runCatching { Domain.valueOf(str.uppercase()) }.getOrNull()
  }

  private val _uiState = MutableStateFlow(ReviewSessionUiState())
  val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

  init {
    loadSessionItems()
  }

  fun loadSessionItems() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      val nowMs = System.currentTimeMillis()
      val startSessionUseCase = StartReviewSessionUseCase(progressRepository, settingsRepository)

      var sessionItems = startSessionUseCase(
        domain = domainFilter,
        nowMs = nowMs
      )

      // Fallback: If no scheduled items are in DB yet (fresh install / preview testing),
      // pull active chapter words and kanji as NEW items so the user can study immediately.
      if (sessionItems.isEmpty()) {
        val targetDomain = domainFilter ?: Domain.JAPANESE
        val words = lexiconRepository.getWordsForChapter(targetDomain, 26).take(10)
        val kanji = kanjiRepository.observeKanjiForChapter(26).first().take(5)

        val fallbackNewItems = mutableListOf<ReviewItem>()
        words.forEach { w ->
          fallbackNewItems.add(
            ReviewItem(
              progress = UserProgress(
                itemKey = w.id,
                itemType = ItemType.WORD,
                domain = w.domain,
                state = SrsState.NEW
              ),
              word = w
            )
          )
        }
        kanji.forEach { k ->
          fallbackNewItems.add(
            ReviewItem(
              progress = UserProgress(
                itemKey = "ja:kanji:${k.literal}",
                itemType = ItemType.KANJI,
                domain = Domain.JAPANESE,
                state = SrsState.NEW
              ),
              kanji = k
            )
          )
        }
        sessionItems = fallbackNewItems
      }

      _uiState.value = ReviewSessionUiState(
        items = sessionItems,
        currentIndex = 0,
        isAnswerRevealed = false,
        isFinished = sessionItems.isEmpty(),
        isLoading = false
      )
    }
  }

  fun onEvent(event: ReviewSessionEvent) {
    when (event) {
      is ReviewSessionEvent.RevealAnswer -> {
        _uiState.value = _uiState.value.copy(isAnswerRevealed = true)
      }

      is ReviewSessionEvent.SubmitGrade -> {
        val current = _uiState.value
        val item = current.currentItem ?: return

        // Record review in repository asynchronously
        viewModelScope.launch {
          progressRepository.recordReview(
            itemKey = item.progress.itemKey,
            itemType = item.progress.itemType,
            domain = item.progress.domain,
            grade = event.grade,
            nowMs = System.currentTimeMillis()
          )
        }

        // Update stats
        val newStats = when (event.grade) {
          ReviewGrade.AGAIN -> current.stats.copy(againCount = current.stats.againCount + 1)
          ReviewGrade.HARD -> current.stats.copy(hardCount = current.stats.hardCount + 1)
          ReviewGrade.GOOD -> current.stats.copy(goodCount = current.stats.goodCount + 1)
          ReviewGrade.EASY -> current.stats.copy(easyCount = current.stats.easyCount + 1)
        }

        // If AGAIN, append to end of list for re-testing
        val updatedList = current.items.toMutableList()
        if (event.grade == ReviewGrade.AGAIN) {
          updatedList.add(item)
        }

        val nextIndex = current.currentIndex + 1
        val isFinished = nextIndex >= updatedList.size

        _uiState.value = current.copy(
          items = updatedList,
          currentIndex = nextIndex,
          isAnswerRevealed = false,
          isFinished = isFinished,
          stats = newStats
        )
      }
    }
  }
}
