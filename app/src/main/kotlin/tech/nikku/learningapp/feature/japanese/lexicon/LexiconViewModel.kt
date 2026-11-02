package tech.nikku.learningapp.feature.japanese.lexicon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import javax.inject.Inject

data class LexiconUiState(
  val searchQuery: String = "",
  val selectedChapters: Set<Int> = emptySet(),
  val selectedPos: PartOfSpeech? = null,
  val selectedVerbGroup: VerbGroup? = null,
  val selectedTransitivity: Transitivity? = null,
  val selectedJlpt: JlptLevel? = null,
  val verifiedOnly: Boolean = false,
  val words: List<Word> = emptyList(),
  val isLoading: Boolean = false
)

sealed interface LexiconEvent {
  data class UpdateSearchQuery(val query: String) : LexiconEvent
  data class ToggleChapter(val chapter: Int) : LexiconEvent
  data class SelectPos(val pos: PartOfSpeech?) : LexiconEvent
  data class SelectVerbGroup(val verbGroup: VerbGroup?) : LexiconEvent
  data class SelectTransitivity(val transitivity: Transitivity?) : LexiconEvent
  data class SelectJlpt(val jlpt: JlptLevel?) : LexiconEvent
  data object ToggleVerifiedOnly : LexiconEvent
  data object ClearFilters : LexiconEvent
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LexiconViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  private val _selectedChapters = MutableStateFlow<Set<Int>>(emptySet())
  private val _selectedPos = MutableStateFlow<PartOfSpeech?>(null)
  private val _selectedVerbGroup = MutableStateFlow<VerbGroup?>(null)
  private val _selectedTransitivity = MutableStateFlow<Transitivity?>(null)
  private val _selectedJlpt = MutableStateFlow<JlptLevel?>(null)
  private val _verifiedOnly = MutableStateFlow(false)
  private val _isLoading = MutableStateFlow(false)

  // Debounced search query (250 ms for user typing, immediate for blank initial)
  private val debouncedQuery = _searchQuery
    .debounce { query -> if (query.isEmpty()) 0L else 250L }
    .distinctUntilChanged()

  @Suppress("UNCHECKED_CAST")
  val uiState: StateFlow<LexiconUiState> = combine(
    debouncedQuery,
    _selectedChapters,
    _selectedPos,
    _selectedVerbGroup,
    _selectedTransitivity,
    _selectedJlpt,
    _verifiedOnly
  ) { array: Array<Any?> ->
    LexiconFilterParams(
      query = array[0] as String,
      chapters = array[1] as Set<Int>,
      pos = array[2] as PartOfSpeech?,
      verbGroup = array[3] as VerbGroup?,
      transitivity = array[4] as Transitivity?,
      jlpt = array[5] as JlptLevel?,
      verifiedOnly = array[6] as Boolean
    )
  }.flatMapLatest { params ->
    _isLoading.value = true
    lexiconRepository.observeWords(
      domain = Domain.JAPANESE,
      chapters = params.chapters,
      pos = params.pos,
      verbGroup = params.verbGroup,
      jlpt = params.jlpt,
      searchQuery = params.query
    ).combine(_isLoading) { wordList, loading ->
      var filtered = wordList
      if (params.transitivity != null) {
        filtered = filtered.filter { it.transitivity == params.transitivity }
      }
      if (params.verifiedOnly) {
        filtered = filtered.filter { it.verified }
      }
      LexiconUiState(
        searchQuery = params.query,
        selectedChapters = params.chapters,
        selectedPos = params.pos,
        selectedVerbGroup = params.verbGroup,
        selectedTransitivity = params.transitivity,
        selectedJlpt = params.jlpt,
        verifiedOnly = params.verifiedOnly,
        words = filtered,
        isLoading = false
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = LexiconUiState(isLoading = true)
  )

  fun onEvent(event: LexiconEvent) {
    when (event) {
      is LexiconEvent.UpdateSearchQuery -> _searchQuery.value = event.query
      is LexiconEvent.ToggleChapter -> {
        val curr = _selectedChapters.value
        _selectedChapters.value = if (curr.contains(event.chapter)) curr - event.chapter else curr + event.chapter
      }
      is LexiconEvent.SelectPos -> _selectedPos.value = event.pos
      is LexiconEvent.SelectVerbGroup -> _selectedVerbGroup.value = event.verbGroup
      is LexiconEvent.SelectTransitivity -> _selectedTransitivity.value = event.transitivity
      is LexiconEvent.SelectJlpt -> _selectedJlpt.value = event.jlpt
      is LexiconEvent.ToggleVerifiedOnly -> _verifiedOnly.value = !_verifiedOnly.value
      is LexiconEvent.ClearFilters -> {
        _searchQuery.value = ""
        _selectedChapters.value = emptySet()
        _selectedPos.value = null
        _selectedVerbGroup.value = null
        _selectedTransitivity.value = null
        _selectedJlpt.value = null
        _verifiedOnly.value = false
      }
    }
  }

  private data class LexiconFilterParams(
    val query: String,
    val chapters: Set<Int>,
    val pos: PartOfSpeech?,
    val verbGroup: VerbGroup?,
    val transitivity: Transitivity?,
    val jlpt: JlptLevel?,
    val verifiedOnly: Boolean
  )
}
