package tech.nikku.learningapp.feature.japanese.particle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.engine.exercise.ClozeEvaluation
import tech.nikku.learningapp.core.domain.engine.exercise.ClozeGrader
import tech.nikku.learningapp.core.domain.model.ClozeSegment
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExercisePayload
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.repository.ExerciseRepository
import tech.nikku.learningapp.core.domain.usecase.GradeAnswerUseCase
import javax.inject.Inject

data class ParticleClozeCardUiModel(
  val exercise: Exercise,
  val payload: ExercisePayload.ParticleCloze,
  val selectedOptions: Map<Int, String> = emptyMap(),
  val evaluations: Map<Int, ClozeEvaluation> = emptyMap()
)

data class ParticleClozeUiState(
  val isLoading: Boolean = true,
  val chapter: Int = 26,
  val currentIndex: Int = 0,
  val totalCount: Int = 0,
  val currentCard: ParticleClozeCardUiModel? = null,
  val isCompleted: Boolean = false,
  val correctCount: Int = 0
)

sealed interface ParticleClozeEvent {
  data class SelectOption(val blankIndex: Int, val option: String) : ParticleClozeEvent
  data object NextCard : ParticleClozeEvent
}

@HiltViewModel
class ParticleClozeViewModel @Inject constructor(
  private val exerciseRepository: ExerciseRepository,
  private val gradeAnswer: GradeAnswerUseCase,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val chapterArg: Int = savedStateHandle.get<Int>("chapter") ?: 26

  private val _uiState = MutableStateFlow(ParticleClozeUiState(chapter = chapterArg))
  val uiState: StateFlow<ParticleClozeUiState> = _uiState.asStateFlow()

  private var exercisesQueue: List<ParticleClozeCardUiModel> = emptyList()

  init {
    loadExercises()
  }

  private fun loadExercises() {
    viewModelScope.launch {
      val exercises = exerciseRepository.getExercisesForChapter(
        domain = Domain.JAPANESE,
        chapter = chapterArg,
        type = ExerciseType.PARTICLE_CLOZE
      )

      val validCards = exercises.mapNotNull { ex ->
        val payload = ex.payload as? ExercisePayload.ParticleCloze ?: return@mapNotNull null
        ParticleClozeCardUiModel(
          exercise = ex,
          payload = payload
        )
      }

      exercisesQueue = validCards
      if (exercisesQueue.isNotEmpty()) {
        _uiState.value = ParticleClozeUiState(
          isLoading = false,
          chapter = chapterArg,
          currentIndex = 0,
          totalCount = exercisesQueue.size,
          currentCard = exercisesQueue.first()
        )
      } else {
        _uiState.value = ParticleClozeUiState(
          isLoading = false,
          chapter = chapterArg,
          isCompleted = true
        )
      }
    }
  }

  fun onEvent(event: ParticleClozeEvent) {
    when (event) {
      is ParticleClozeEvent.SelectOption -> selectOption(event.blankIndex, event.option)
      is ParticleClozeEvent.NextCard -> nextCard()
    }
  }

  private fun selectOption(blankIndex: Int, option: String) {
    val state = _uiState.value
    val card = state.currentCard ?: return
    if (card.evaluations.containsKey(blankIndex)) return // Already answered this blank

    val blank = card.payload.segments.getOrNull(blankIndex) as? ClozeSegment.Blank ?: return
    val evaluation = ClozeGrader.grade(blank, option)

    val updatedSelections = card.selectedOptions + (blankIndex to option)
    val updatedEvaluations = card.evaluations + (blankIndex to evaluation)
    val updatedCard = card.copy(
      selectedOptions = updatedSelections,
      evaluations = updatedEvaluations
    )

    // Check if all blanks have been answered
    val totalBlanks = card.payload.segments.count { it is ClozeSegment.Blank }
    val allAnswered = updatedEvaluations.size >= totalBlanks
    val allCorrect = allAnswered && updatedEvaluations.values.all { it.isCorrect }

    if (allAnswered) {
      viewModelScope.launch {
        val grade = if (allCorrect) ReviewGrade.GOOD else ReviewGrade.AGAIN
        gradeAnswer(
          itemKey = card.exercise.id,
          itemType = ItemType.EXERCISE,
          domain = Domain.JAPANESE,
          grade = grade
        )
      }
    }

    _uiState.value = state.copy(
      currentCard = updatedCard,
      correctCount = if (allCorrect) state.correctCount + 1 else state.correctCount
    )
  }

  private fun nextCard() {
    val nextIndex = _uiState.value.currentIndex + 1
    if (nextIndex < exercisesQueue.size) {
      _uiState.value = _uiState.value.copy(
        currentIndex = nextIndex,
        currentCard = exercisesQueue[nextIndex]
      )
    } else {
      _uiState.value = _uiState.value.copy(
        isCompleted = true,
        currentCard = null
      )
    }
  }
}
