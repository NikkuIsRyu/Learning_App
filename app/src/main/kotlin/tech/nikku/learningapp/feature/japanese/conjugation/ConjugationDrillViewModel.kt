package tech.nikku.learningapp.feature.japanese.conjugation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.engine.conjugation.Conjugation
import tech.nikku.learningapp.core.domain.engine.conjugation.Conjugator
import tech.nikku.learningapp.core.domain.engine.exercise.AnswerNormalizer
import tech.nikku.learningapp.core.domain.engine.exercise.AnswerResult
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.core.domain.usecase.GradeAnswerUseCase
import javax.inject.Inject

data class DrillCard(
  val word: Word,
  val sourceForm: VerbForm,
  val targetForm: VerbForm,
  val polite: Boolean = false,
  val expected: Conjugation
)

data class ConjugationDrillUiState(
  val isLoading: Boolean = true,
  val currentCardIndex: Int = 0,
  val totalCards: Int = 0,
  val currentCard: DrillCard? = null,
  val input: String = "",
  val result: AnswerResult? = null,
  val hintShown: Boolean = false,
  val isCompleted: Boolean = false,
  val correctCount: Int = 0
)

sealed interface ConjugationDrillEvent {
  data class UpdateInput(val input: String) : ConjugationDrillEvent
  data object Submit : ConjugationDrillEvent
  data object RequestHint : ConjugationDrillEvent
  data object NextCard : ConjugationDrillEvent
}

@HiltViewModel
class ConjugationDrillViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository,
  private val settingsRepository: SettingsRepository,
  private val gradeAnswer: GradeAnswerUseCase,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val initialWordId: String? = savedStateHandle.get<String>("wordId")?.takeIf { it.isNotBlank() }
  private val initialTargetFormStr: String? = savedStateHandle.get<String>("targetForm")?.takeIf { it.isNotBlank() }

  private val _uiState = MutableStateFlow(ConjugationDrillUiState())
  val uiState: StateFlow<ConjugationDrillUiState> = _uiState.asStateFlow()

  private var drillQueue: List<DrillCard> = emptyList()

  init {
    loadCards()
  }

  private fun loadCards() {
    viewModelScope.launch {
      val targetForm = initialTargetFormStr?.let { runCatching { VerbForm.valueOf(it) }.getOrNull() }

      val words = if (initialWordId != null) {
        val word = lexiconRepository.getWordById(initialWordId)
        if (word != null) listOf(word) else emptyList()
      } else {
        val activeChapters = settingsRepository.observeActiveChapters().first()
        lexiconRepository.observeWords(
          domain = Domain.JAPANESE,
          chapters = activeChapters,
          pos = PartOfSpeech.VERB
        ).first().filter { it.verbGroup != null }
      }

      val cards = mutableListOf<DrillCard>()
      for (word in words) {
        val group = word.verbGroup ?: continue
        val formsToTest = if (targetForm != null) {
          listOf(targetForm)
        } else {
          listOf(VerbForm.TE, VerbForm.TA, VerbForm.NAI, VerbForm.POTENTIAL)
        }

        for (tf in formsToTest) {
          val expected = Conjugator.conjugate(word.headword, word.reading, group, tf)
          cards.add(
            DrillCard(
              word = word,
              sourceForm = VerbForm.DICTIONARY,
              targetForm = tf,
              polite = false,
              expected = expected
            )
          )
        }
      }

      drillQueue = cards.shuffled()
      if (drillQueue.isNotEmpty()) {
        _uiState.value = ConjugationDrillUiState(
          isLoading = false,
          currentCardIndex = 0,
          totalCards = drillQueue.size,
          currentCard = drillQueue.first()
        )
      } else {
        _uiState.value = ConjugationDrillUiState(
          isLoading = false,
          isCompleted = true
        )
      }
    }
  }

  fun onEvent(event: ConjugationDrillEvent) {
    when (event) {
      is ConjugationDrillEvent.UpdateInput -> {
        _uiState.value = _uiState.value.copy(input = event.input)
      }
      is ConjugationDrillEvent.Submit -> submitAnswer()
      is ConjugationDrillEvent.RequestHint -> requestHint()
      is ConjugationDrillEvent.NextCard -> nextCard()
    }
  }

  private fun submitAnswer() {
    val state = _uiState.value
    val card = state.currentCard ?: return
    if (state.result != null) return // Already submitted

    val eval = AnswerNormalizer.evaluate(state.input, card.expected)
    val itemKey = "conj:${card.word.id}:${card.targetForm.name}"

    viewModelScope.launch {
      val grade = when {
        !eval.isCorrect -> ReviewGrade.AGAIN
        state.hintShown -> ReviewGrade.HARD
        else -> ReviewGrade.GOOD
      }

      gradeAnswer(
        itemKey = itemKey,
        itemType = ItemType.CONJUGATION_CELL,
        domain = Domain.JAPANESE,
        grade = grade
      )
    }

    _uiState.value = state.copy(
      result = eval,
      correctCount = if (eval.isCorrect) state.correctCount + 1 else state.correctCount
    )
  }

  private fun requestHint() {
    _uiState.value = _uiState.value.copy(hintShown = true)
  }

  private fun nextCard() {
    val nextIndex = _uiState.value.currentCardIndex + 1
    if (nextIndex < drillQueue.size) {
      _uiState.value = _uiState.value.copy(
        currentCardIndex = nextIndex,
        currentCard = drillQueue[nextIndex],
        input = "",
        result = null,
        hintShown = false
      )
    } else {
      _uiState.value = _uiState.value.copy(
        isCompleted = true,
        currentCard = null
      )
    }
  }
}
