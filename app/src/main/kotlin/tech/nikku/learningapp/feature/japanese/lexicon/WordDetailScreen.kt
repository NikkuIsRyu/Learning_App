package tech.nikku.learningapp.feature.japanese.lexicon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.ContentSource
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.feature.japanese.lexicon.components.TransitivityPairCard
import tech.nikku.learningapp.ui.theme.LearningAppTheme
import javax.inject.Inject

data class WordDetailUiState(
  val isLoading: Boolean = true,
  val word: Word? = null,
  val partnerWord: Word? = null
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
  private val lexiconRepository: LexiconRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val wordId: String = savedStateHandle.get<String>("wordId") ?: ""

  private val _uiState = MutableStateFlow(WordDetailUiState())
  val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

  init {
    loadWord()
  }

  private fun loadWord() {
    viewModelScope.launch {
      val word = lexiconRepository.getWordById(wordId)
      val partner = word?.pairWordId?.let { lexiconRepository.getWordById(it) }

      _uiState.value = WordDetailUiState(
        isLoading = false,
        word = word,
        partnerWord = partner
      )
    }
  }
}

@Composable
fun WordDetailRoute(
  onBackClick: () -> Unit,
  onPartnerClick: (String) -> Unit = {},
  onKanjiClick: (String) -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: WordDetailViewModel = hiltViewModel()
) {
  val uiState = viewModel.uiState.value

  WordDetailScreen(
    word = uiState.word,
    partnerWord = uiState.partnerWord,
    isLoading = uiState.isLoading,
    onBackClick = onBackClick,
    onPartnerClick = { onPartnerClick(it.id) },
    onKanjiClick = onKanjiClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
  word: Word?,
  partnerWord: Word?,
  isLoading: Boolean,
  onBackClick: () -> Unit,
  onPartnerClick: (Word) -> Unit,
  onKanjiClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = "Word Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    if (isLoading) {
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    } else if (word == null) {
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("Word not found.")
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Main Headword Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Badges row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              word.chapter?.let {
                BadgeChip("Chapter $it", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
              }
              word.jlpt?.let {
                BadgeChip(it.name, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
              }
              if (word.verified) {
                BadgeChip("Verified", Color(0xFFE8F5E9), Color(0xFF1B5E20))
              }
            }

            // Big Headword with ruby
            val ruby = word.rubyMarkup
            if (ruby != null && ruby.contains("{")) {
              FuriganaText(
                text = ruby,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                rubyStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp)
              )
            } else {
              Text(
                text = word.headword,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
              )
            }

            // Kana reading & romanization
            if (word.reading.isNotEmpty()) {
              Text(
                text = word.reading,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Meanings
            Text(
              text = word.meaningEn ?: "",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            val de = word.meaningDe
            if (de != null) {
              Text(
                text = de,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
              )
            }
          }
        }

        // Transitivity Pair Section
        if (word.transitivity != null || partnerWord != null) {
          TransitivityPairCard(
            word = word,
            partnerWord = partnerWord,
            onPartnerClick = onPartnerClick
          )
        }

        // Kanji Components Links
        val kanjiChars = word.headword.filter { it.code in 0x4E00..0x9FAF }.toSet()
        if (kanjiChars.isNotEmpty()) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "Kanji in this word:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                kanjiChars.forEach { kanji ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .clickable { onKanjiClick(kanji.toString()) }
                      .padding(horizontal = 14.dp, vertical = 8.dp)
                  ) {
                    Text(
                      text = kanji.toString(),
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

// ──────────────────────────────── PREVIEWS ────────────────────────────────

@Preview(showBackground = true)
@Composable
fun WordDetailPairPreview() {
  LearningAppTheme {
    val sampleIntransitive = Word(
      id = "ja:mnn:29:w001",
      domain = Domain.JAPANESE,
      headword = "開きます",
      reading = "あきます",
      rubyMarkup = "{開|あ}きます",
      meaningEn = "(a door) to open, be opened",
      meaningDe = "aufgehen, sich öffnen",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.GODAN,
      transitivity = Transitivity.INTRANSITIVE,
      pairWordId = "ja:mnn:29:w002",
      chapter = 29,
      jlpt = JlptLevel.N4,
      source = ContentSource.MINNA,
      verified = true
    )

    val sampleTransitive = Word(
      id = "ja:mnn:29:w002",
      domain = Domain.JAPANESE,
      headword = "開けます",
      reading = "あけます",
      rubyMarkup = "{開|あ}けます",
      meaningEn = "to open (something)",
      meaningDe = "öffnen, aufmachen",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.ICHIDAN,
      transitivity = Transitivity.TRANSITIVE,
      pairWordId = "ja:mnn:29:w001",
      chapter = 29,
      jlpt = JlptLevel.N5,
      source = ContentSource.MINNA,
      verified = true
    )

    WordDetailScreen(
      word = sampleIntransitive,
      partnerWord = sampleTransitive,
      isLoading = false,
      onBackClick = {},
      onPartnerClick = {},
      onKanjiClick = {}
    )
  }
}
