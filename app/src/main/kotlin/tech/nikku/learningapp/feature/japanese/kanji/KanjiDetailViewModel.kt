package tech.nikku.learningapp.feature.japanese.kanji

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import javax.inject.Inject

data class KanjiDetailUiState(
  val literal: String,
  val kanji: Kanji? = null,
  val isLoading: Boolean = true,
  val errorMessage: String? = null
)

@HiltViewModel
class KanjiDetailViewModel @Inject constructor(
  private val kanjiRepository: KanjiRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  val literal: String = checkNotNull(savedStateHandle["kanjiLiteral"])

  private val _uiState = MutableStateFlow(KanjiDetailUiState(literal = literal))
  val uiState: StateFlow<KanjiDetailUiState> = _uiState.asStateFlow()

  init {
    loadKanji()
  }

  fun loadKanji() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      try {
        val kanji = kanjiRepository.getKanjiByLiteral(literal)
        _uiState.value = _uiState.value.copy(
          kanji = kanji,
          isLoading = false,
          errorMessage = if (kanji == null) "Kanji not found" else null
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = e.message ?: "Failed to load kanji"
        )
      }
    }
  }
}
