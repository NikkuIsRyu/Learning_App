package tech.nikku.learningapp.feature.japanese.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import javax.inject.Inject

data class ChapterHubItem(
  val chapter: Int,
  val wordCount: Int,
  val isVerified: Boolean
)

data class JapaneseHubUiState(
  val chapters: List<ChapterHubItem> = (1..50).map { ch ->
    ChapterHubItem(chapter = ch, wordCount = 0, isVerified = ch in 26..32)
  },
  val isLoading: Boolean = false
)

sealed interface JapaneseHubEvent {
  data class SelectChapter(val chapter: Int) : JapaneseHubEvent
}

@HiltViewModel
class JapaneseHubViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository
) : ViewModel() {

  val uiState: StateFlow<JapaneseHubUiState> = lexiconRepository
    .observeChapterCounts(Domain.JAPANESE)
    .map { countsMap ->
      val items = (1..50).map { ch ->
        ChapterHubItem(
          chapter = ch,
          wordCount = countsMap[ch] ?: 0,
          isVerified = ch in 26..32
        )
      }
      JapaneseHubUiState(chapters = items, isLoading = false)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = JapaneseHubUiState(isLoading = true)
    )
}
