package tech.nikku.learningapp.feature.japanese.conjugation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import javax.inject.Inject

data class MatrixRowUiModel(
  val word: Word,
  val cells: Map<VerbForm, UserProgress?> = emptyMap()
)

data class ConjugationMatrixUiState(
  val isLoading: Boolean = true,
  val activeChapters: Set<Int> = (26..32).toSet(),
  val selectedChapter: Int? = null,
  val searchQuery: String = "",
  val rows: List<MatrixRowUiModel> = emptyList(),
  val visibleForms: List<VerbForm> = VerbForm.entries
)

sealed interface ConjugationMatrixEvent {
  data class SelectChapter(val chapter: Int?) : ConjugationMatrixEvent
  data class UpdateSearchQuery(val query: String) : ConjugationMatrixEvent
}

@HiltViewModel
class ConjugationMatrixViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository,
  private val settingsRepository: SettingsRepository,
  private val progressRepository: ProgressRepository
) : ViewModel() {

  private val _selectedChapter = MutableStateFlow<Int?>(null)
  private val _searchQuery = MutableStateFlow("")

  val uiState: StateFlow<ConjugationMatrixUiState> = combine(
    lexiconRepository.observeWords(domain = Domain.JAPANESE, pos = PartOfSpeech.VERB),
    settingsRepository.observeActiveChapters(),
    _selectedChapter,
    _searchQuery
  ) { words, activeChapters, chapter, query ->
    val filteredWords = words.filter { word ->
      word.verbGroup != null &&
        (chapter == null || word.chapter == chapter) &&
        (query.isBlank() || word.headword.contains(query) || word.reading.contains(query) || word.meaningEn.contains(query, ignoreCase = true))
    }

    val rows = filteredWords.map { word ->
      MatrixRowUiModel(
        word = word,
        cells = emptyMap() // Progress overlay is loaded reactively or on drill
      )
    }

    ConjugationMatrixUiState(
      isLoading = false,
      activeChapters = activeChapters,
      selectedChapter = chapter,
      searchQuery = query,
      rows = rows
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ConjugationMatrixUiState()
  )

  fun onEvent(event: ConjugationMatrixEvent) {
    when (event) {
      is ConjugationMatrixEvent.SelectChapter -> _selectedChapter.value = event.chapter
      is ConjugationMatrixEvent.UpdateSearchQuery -> _searchQuery.value = event.query
    }
  }
}
