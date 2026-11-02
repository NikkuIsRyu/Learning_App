package tech.nikku.learningapp.feature.japanese.chapter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.*
import tech.nikku.learningapp.core.domain.repository.GrammarRepository
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import javax.inject.Inject

enum class ChapterTab {
  GRAMMAR,
  WORDS,
  KANJI,
  DRILLS
}

data class ChapterUiState(
  val chapter: Int,
  val selectedTab: ChapterTab = ChapterTab.GRAMMAR,
  val rules: List<GrammarRule> = emptyList(),
  val words: List<Word> = emptyList(),
  val kanji: List<Kanji> = emptyList(),
  val isLoading: Boolean = false,
  val expandedRuleId: String? = null
)

sealed interface ChapterEvent {
  data class SelectTab(val tab: ChapterTab) : ChapterEvent
  data class ToggleRule(val ruleId: String) : ChapterEvent
  data object Back : ChapterEvent
  data class StartDrill(val drillType: String) : ChapterEvent
}

@HiltViewModel
class ChapterViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository,
  private val kanjiRepository: KanjiRepository,
  private val grammarRepository: GrammarRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  val chapter: Int = savedStateHandle.get<Int>("chapter") ?: 26

  private val _selectedTab = MutableStateFlow(ChapterTab.GRAMMAR)
  private val _expandedRuleId = MutableStateFlow<String?>(null)

  private val _words = lexiconRepository.observeWords(
    domain = Domain.JAPANESE,
    chapters = setOf(chapter)
  )

  private val _kanji = kanjiRepository.observeKanjiForChapter(chapter)

  private val _rules = grammarRepository.observeRulesForChapter(chapter)

  val uiState: StateFlow<ChapterUiState> = combine(
    _selectedTab,
    _expandedRuleId,
    _rules,
    _words,
    _kanji
  ) { tab, expandedId, rules, words, kanji ->
    ChapterUiState(
      chapter = chapter,
      selectedTab = tab,
      rules = rules,
      words = words,
      kanji = kanji,
      isLoading = false,
      expandedRuleId = expandedId
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ChapterUiState(chapter = chapter, isLoading = true)
  )

  fun onEvent(event: ChapterEvent) {
    when (event) {
      is ChapterEvent.SelectTab -> _selectedTab.value = event.tab
      is ChapterEvent.ToggleRule -> {
        _expandedRuleId.value = if (_expandedRuleId.value == event.ruleId) null else event.ruleId
      }
      is ChapterEvent.Back, is ChapterEvent.StartDrill -> {
        // Handled by route navigation
      }
    }
  }
}
