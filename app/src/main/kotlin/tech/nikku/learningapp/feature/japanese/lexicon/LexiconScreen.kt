package tech.nikku.learningapp.feature.japanese.lexicon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.ContentSource
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.feature.japanese.lexicon.components.FilterChipsRow
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@Composable
fun LexiconRoute(
  onBackClick: () -> Unit,
  onWordClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LexiconViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LexiconScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    onWordClick = onWordClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexiconScreen(
  state: LexiconUiState,
  onEvent: (LexiconEvent) -> Unit,
  onBackClick: () -> Unit,
  onWordClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Master Lexicon (辞書)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Search Bar
      OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { onEvent(LexiconEvent.UpdateSearchQuery(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search romaji, kana, kanji, English...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (state.searchQuery.isNotEmpty()) {
            IconButton(onClick = { onEvent(LexiconEvent.UpdateSearchQuery("")) }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
      )

      // Filter Chips
      FilterChipsRow(
        selectedChapters = state.selectedChapters,
        selectedPos = state.selectedPos,
        selectedVerbGroup = state.selectedVerbGroup,
        selectedTransitivity = state.selectedTransitivity,
        selectedJlpt = state.selectedJlpt,
        verifiedOnly = state.verifiedOnly,
        onToggleChapter = { onEvent(LexiconEvent.ToggleChapter(it)) },
        onSelectPos = { onEvent(LexiconEvent.SelectPos(it)) },
        onSelectVerbGroup = { onEvent(LexiconEvent.SelectVerbGroup(it)) },
        onSelectTransitivity = { onEvent(LexiconEvent.SelectTransitivity(it)) },
        onSelectJlpt = { onEvent(LexiconEvent.SelectJlpt(it)) },
        onToggleVerifiedOnly = { onEvent(LexiconEvent.ToggleVerifiedOnly) }
      )

      // Results Count Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${state.words.size} words found",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Words List
      if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      } else if (state.words.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
          Text(
            text = "No words matching your query or filters.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(state.words, key = { it.id }) { word ->
            WordListItemCard(
              word = word,
              onClick = { onWordClick(word.id) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun WordListItemCard(
  word: Word,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Headword + reading
        val ruby = word.rubyMarkup
        if (ruby != null && ruby.contains("{")) {
          FuriganaText(
            text = ruby,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
          )
        } else {
          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = word.headword,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            if (word.reading.isNotEmpty() && word.reading != word.headword) {
              Text(
                text = word.reading,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Tags & Badges
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (word.chapter != null) {
            BadgeChip(text = "L${word.chapter}", bgColor = MaterialTheme.colorScheme.secondaryContainer, textColor = MaterialTheme.colorScheme.onSecondaryContainer)
          }
          val jlpt = word.jlpt
          if (jlpt != null) {
            BadgeChip(text = jlpt.name, bgColor = MaterialTheme.colorScheme.tertiaryContainer, textColor = MaterialTheme.colorScheme.onTertiaryContainer)
          }
        }
      }

      // Meaning
      Text(
        text = word.meaningEn ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium
      )

      // Sub-row: POS, VerbGroup, Transitivity
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        word.pos?.let {
          Text(text = it.name.lowercase().replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        word.verbGroup?.let {
          Text(text = "• ${it.name}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        word.transitivity?.let {
          val label = if (it == Transitivity.INTRANSITIVE) "自動詞" else if (it == Transitivity.TRANSITIVE) "他動詞" else "自他"
          val col = if (it == Transitivity.INTRANSITIVE) Color(0xFF00695C) else Color(0xFFE65100)
          Text(text = "• $label", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = col)
        }
      }
    }
  }
}

@Composable
fun BadgeChip(
  text: String,
  bgColor: Color,
  textColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

// ──────────────────────────────── PREVIEWS ────────────────────────────────

@Preview(showBackground = true)
@Composable
fun LexiconScreenPreview() {
  LearningAppTheme {
    val sampleWords = listOf(
      Word(
        id = "ja:mnn:29:w001",
        domain = Domain.JAPANESE,
        headword = "開きます",
        reading = "あきます",
        rubyMarkup = "{開|あ}きます",
        meaningEn = "(a door) to open, be opened",
        pos = PartOfSpeech.VERB,
        verbGroup = VerbGroup.GODAN,
        transitivity = Transitivity.INTRANSITIVE,
        pairWordId = "ja:mnn:29:w002",
        chapter = 29,
        jlpt = JlptLevel.N4,
        source = ContentSource.MINNA,
        verified = true
      ),
      Word(
        id = "ja:mnn:29:w002",
        domain = Domain.JAPANESE,
        headword = "開けます",
        reading = "あけます",
        rubyMarkup = "{開|あ}けます",
        meaningEn = "to open (something)",
        pos = PartOfSpeech.VERB,
        verbGroup = VerbGroup.ICHIDAN,
        transitivity = Transitivity.TRANSITIVE,
        pairWordId = "ja:mnn:29:w001",
        chapter = 29,
        jlpt = JlptLevel.N5,
        source = ContentSource.MINNA,
        verified = true
      ),
      Word(
        id = "ja:mnn:26:w001",
        domain = Domain.JAPANESE,
        headword = "見ます（診ます）",
        reading = "みます",
        rubyMarkup = "{見|み}ます（診ます）",
        meaningEn = "to check, examine, look at",
        pos = PartOfSpeech.VERB,
        verbGroup = VerbGroup.ICHIDAN,
        transitivity = Transitivity.TRANSITIVE,
        chapter = 26,
        jlpt = JlptLevel.N4,
        source = ContentSource.MINNA,
        verified = true
      )
    )

    LexiconScreen(
      state = LexiconUiState(
        searchQuery = "",
        selectedChapters = setOf(29),
        words = sampleWords,
        isLoading = false
      ),
      onEvent = {},
      onBackClick = {},
      onWordClick = {}
    )
  }
}
