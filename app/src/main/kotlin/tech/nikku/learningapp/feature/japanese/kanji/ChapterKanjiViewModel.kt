package tech.nikku.learningapp.feature.japanese.kanji

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import javax.inject.Inject

data class ChapterKanjiUiState(
  val selectedChapter: Int? = null,
  val kanjiList: List<Kanji> = emptyList(),
  val isLoading: Boolean = false,
  val searchQuery: String = ""
)

sealed interface ChapterKanjiEvent {
  data class SelectChapter(val chapter: Int?) : ChapterKanjiEvent
  data class UpdateSearch(val query: String) : ChapterKanjiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChapterKanjiViewModel @Inject constructor(
  private val kanjiRepository: KanjiRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val initialChapter = savedStateHandle.get<Int>("chapter")
  private val _selectedChapter = MutableStateFlow(initialChapter)
  private val _searchQuery = MutableStateFlow("")

  val uiState: StateFlow<ChapterKanjiUiState> = combine(
    _selectedChapter,
    _searchQuery
  ) { chapter, query ->
    Pair(chapter, query)
  }.flatMapLatest { (chapter, query) ->
    val baseFlow = if (chapter != null) {
      kanjiRepository.observeKanjiForChapter(chapter)
    } else {
      flow {
        emit(kanjiRepository.getAllKanji())
      }
    }
    baseFlow.map { list ->
      val filtered = if (query.isNotBlank()) {
        val q = query.trim()
        list.filter { k ->
          k.literal.contains(q) ||
            k.meaningsEn.any { it.contains(q, ignoreCase = true) } ||
            k.onReadings.any { it.contains(q) } ||
            k.kunReadings.any { it.contains(q) }
        }
      } else {
        list
      }
      ChapterKanjiUiState(
        selectedChapter = chapter,
        kanjiList = filtered,
        isLoading = false,
        searchQuery = query
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ChapterKanjiUiState(selectedChapter = initialChapter, isLoading = true)
  )

  fun onEvent(event: ChapterKanjiEvent) {
    when (event) {
      is ChapterKanjiEvent.SelectChapter -> _selectedChapter.value = event.chapter
      is ChapterKanjiEvent.UpdateSearch -> _searchQuery.value = event.query
    }
  }
}
