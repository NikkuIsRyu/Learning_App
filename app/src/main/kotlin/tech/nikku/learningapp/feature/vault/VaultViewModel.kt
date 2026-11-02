package tech.nikku.learningapp.feature.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.data.loader.VaultRoadmapLoader
import tech.nikku.learningapp.core.domain.model.VaultModule
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
  @ApplicationContext private val context: Context
) : ViewModel() {

  private val _uiState = MutableStateFlow(VaultUiState())
  val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

  init {
    loadRoadmap()
  }

  private fun loadRoadmap() {
    viewModelScope.launch {
      try {
        val modules = VaultRoadmapLoader.loadFromAssets(context)
        _uiState.value = _uiState.value.copy(
          modules = modules,
          isLoading = false
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false
        )
      }
    }
  }

  fun onEvent(event: VaultEvent) {
    when (event) {
      is VaultEvent.NotifyInterest -> {
        val updated = _uiState.value.notifiedModuleIds + event.moduleId
        _uiState.value = _uiState.value.copy(notifiedModuleIds = updated)
      }
    }
  }
}
