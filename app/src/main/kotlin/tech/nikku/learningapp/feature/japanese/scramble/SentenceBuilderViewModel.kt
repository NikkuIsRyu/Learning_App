package tech.nikku.learningapp.feature.japanese.scramble

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.engine.exercise.ScrambleValidationResult
import tech.nikku.learningapp.core.domain.engine.exercise.ScrambleValidator
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExercisePayload
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.repository.ExerciseRepository
import tech.nikku.learningapp.core.domain.usecase.GradeAnswerUseCase
import javax.inject.Inject
import kotlin.random.Random

data class ScrambleCardUiModel(
  val exercise: Exercise,
  val payload: ExercisePayload.Scramble,
  val availableChunks: List<Pair<Int, String>>,
  val placedIndices: List<Int> = emptyList(),
  val validation: ScrambleValidationResult? = null,
  val isChecked: Boolean = false
)

data class SentenceBuilderUiState(
  val isLoading: Boolean = true,
  val chapter: Int = 26,
  val currentIndex: Int = 0,
  val totalCount: Int = 0,
  val currentCard: ScrambleCardUiModel? = null,
  val isCompleted: Boolean = false,
  val correctCount: Int = 0
)

sealed interface SentenceBuilderEvent {
  data class TapAvailableChunk(val chunkIndex: Int) : SentenceBuilderEvent
  data class TapPlacedChunk(val position: Int) : SentenceBuilderEvent
  data object ResetCurrent : SentenceBuilderEvent
  data object CheckAnswer : SentenceBuilderEvent
  data object NextCard : SentenceBuilderEvent
}

@HiltViewModel
class SentenceBuilderViewModel @Inject constructor(
  private val exerciseRepository: ExerciseRepository,
  private val gradeAnswer: GradeAnswerUseCase,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val chapterArg: Int = savedStateHandle.get<Int>("chapter") ?: 26

  private val _uiState = MutableStateFlow(SentenceBuilderUiState(chapter = chapterArg))
  val uiState: StateFlow<SentenceBuilderUiState> = _uiState.asStateFlow()

  private var exercisesQueue: List<Exercise> = emptyList()

  init {
    loadExercises()
  }

  private fun loadExercises() {
    viewModelScope.launch {
      val exercises = exerciseRepository.getExercisesForChapter(
        domain = Domain.JAPANESE,
        chapter = chapterArg,
        type = ExerciseType.SCRAMBLE
      )

      exercisesQueue = exercises
      if (exercisesQueue.isNotEmpty()) {
        val firstCard = createCardUiModel(exercisesQueue.first())
        _uiState.value = SentenceBuilderUiState(
          isLoading = false,
          chapter = chapterArg,
          currentIndex = 0,
          totalCount = exercisesQueue.size,
          currentCard = firstCard
        )
      } else {
        _uiState.value = SentenceBuilderUiState(
          isLoading = false,
          chapter = chapterArg,
          isCompleted = true
        )
      }
    }
  }

  private fun createCardUiModel(exercise: Exercise): ScrambleCardUiModel? {
    val payload = exercise.payload as? ExercisePayload.Scramble ?: return null
    val rng = Random(exercise.id.hashCode())
    var available = payload.chunks.mapIndexed { idx, s -> idx to s }.shuffled(rng)
    // Avoid showing chunks in already correct order
    if (available.map { it.first } == payload.acceptedOrders.firstOrNull()) {
      available = available.reversed()
    }
    return ScrambleCardUiModel(
      exercise = exercise,
      payload = payload,
      availableChunks = available,
      placedIndices = emptyList(),
      validation = null,
      isChecked = false
    )
  }

  fun onEvent(event: SentenceBuilderEvent) {
    when (event) {
      is SentenceBuilderEvent.TapAvailableChunk -> tapAvailableChunk(event.chunkIndex)
      is SentenceBuilderEvent.TapPlacedChunk -> tapPlacedChunk(event.position)
      is SentenceBuilderEvent.ResetCurrent -> resetCurrent()
      is SentenceBuilderEvent.CheckAnswer -> checkAnswer()
      is SentenceBuilderEvent.NextCard -> nextCard()
    }
  }

  private fun tapAvailableChunk(chunkIndex: Int) {
    val state = _uiState.value
    val card = state.currentCard ?: return
    if (card.isChecked) return

    val matchIndex = card.availableChunks.indexOfFirst { it.first == chunkIndex }
    if (matchIndex == -1) return

    val updatedAvailable = card.availableChunks.toMutableList().apply { removeAt(matchIndex) }
    val updatedPlaced = card.placedIndices + chunkIndex

    _uiState.value = state.copy(
      currentCard = card.copy(
        availableChunks = updatedAvailable,
        placedIndices = updatedPlaced
      )
    )
  }

  private fun tapPlacedChunk(position: Int) {
    val state = _uiState.value
    val card = state.currentCard ?: return
    if (card.isChecked) return
    if (position !in card.placedIndices.indices) return

    val chunkIndex = card.placedIndices[position]
    val chunkText = card.payload.chunks.getOrNull(chunkIndex) ?: return

    val updatedPlaced = card.placedIndices.toMutableList().apply { removeAt(position) }
    val updatedAvailable = card.availableChunks + (chunkIndex to chunkText)

    _uiState.value = state.copy(
      currentCard = card.copy(
        availableChunks = updatedAvailable,
        placedIndices = updatedPlaced
      )
    )
  }

  private fun resetCurrent() {
    val state = _uiState.value
    val card = state.currentCard ?: return
    val initial = createCardUiModel(card.exercise) ?: return

    _uiState.value = state.copy(currentCard = initial)
  }

  private fun checkAnswer() {
    val state = _uiState.value
    val card = state.currentCard ?: return
    if (card.placedIndices.isEmpty()) return

    val validation = ScrambleValidator.validate(card.payload, card.placedIndices)
    val isCorrect = validation.isCorrect

    viewModelScope.launch {
      val grade = if (isCorrect) ReviewGrade.GOOD else ReviewGrade.AGAIN
      gradeAnswer(
        itemKey = card.exercise.id,
        itemType = ItemType.EXERCISE,
        domain = Domain.JAPANESE,
        grade = grade
      )
    }

    _uiState.value = state.copy(
      currentCard = card.copy(
        validation = validation,
        isChecked = true
      ),
      correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount
    )
  }

  private fun nextCard() {
    val nextIndex = _uiState.value.currentIndex + 1
    if (nextIndex < exercisesQueue.size) {
      val nextCard = createCardUiModel(exercisesQueue[nextIndex])
      _uiState.value = _uiState.value.copy(
        currentIndex = nextIndex,
        currentCard = nextCard
      )
    } else {
      _uiState.value = _uiState.value.copy(
        isCompleted = true,
        currentCard = null
      )
    }
  }
}
