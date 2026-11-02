package tech.nikku.learningapp.feature.japanese.kanji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.Kanji

@Composable
fun ChapterKanjiDestination(
  onBackClick: () -> Unit,
  onKanjiClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ChapterKanjiViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChapterKanjiScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    onKanjiClick = onKanjiClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterKanjiScreen(
  state: ChapterKanjiUiState,
  onEvent: (ChapterKanjiEvent) -> Unit,
  onBackClick: () -> Unit,
  onKanjiClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          val titleText = if (state.selectedChapter != null) {
            "Chapter ${state.selectedChapter} Kanji (漢字)"
          } else {
            "All Kanji (漢字)"
          }
          Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        }
      )
    },
    modifier = modifier
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Search Bar
      OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { onEvent(ChapterKanjiEvent.UpdateSearch(it)) },
        placeholder = { Text("Search kanji, reading, meaning...") },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
          if (state.searchQuery.isNotBlank()) {
            IconButton(onClick = { onEvent(ChapterKanjiEvent.UpdateSearch("")) }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      )

      // Chapter Filter Chips Row
      val chapters = listOf(null) + (26..32).toList()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        chapters.forEach { ch ->
          val isSelected = state.selectedChapter == ch
          FilterChip(
            selected = isSelected,
            onClick = { onEvent(ChapterKanjiEvent.SelectChapter(ch)) },
            label = {
              Text(if (ch == null) "All" else "Ch $ch")
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      // Results header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${state.kanjiList.size} kanji found",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Content Grid or Loading/Empty
      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else if (state.kanjiList.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No kanji found matching the criteria.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 150.dp),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(state.kanjiList, key = { it.literal }) { kanji ->
            KanjiCard(
              kanji = kanji,
              onClick = { onKanjiClick(kanji.literal) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun KanjiCard(
  kanji: Kanji,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Big Kanji glyph in a round/square container
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(54.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = kanji.literal,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Meanings
      Text(
        text = kanji.meaningsEn.firstOrNull() ?: "",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      // On reading (katakana)
      if (kanji.onReadings.isNotEmpty()) {
        Text(
          text = kanji.onReadings.take(2).joinToString("・"),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      } else if (kanji.kunReadings.isNotEmpty()) {
        Text(
          text = kanji.kunReadings.take(2).joinToString("・"),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.secondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Badges row: strokes + JLPT
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "${kanji.strokeCount}画",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        val jlpt = kanji.jlpt
        if (jlpt != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
          ) {
            Text(
              text = jlpt.name,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ChapterKanjiPreview() {
  LearningAppTheme {
    val sampleKanji = listOf(
      Kanji(
        literal = "開",
        onReadings = listOf("カイ"),
        kunReadings = listOf("あ.く", "ひら.く"),
        meaningsEn = listOf("open", "unfold"),
        strokeCount = 12,
        chapter = 29,
        jlpt = JlptLevel.N4
      ),
      Kanji(
        literal = "閉",
        onReadings = listOf("ヘイ"),
        kunReadings = listOf("し.まる", "と.じる"),
        meaningsEn = listOf("closed", "shut"),
        strokeCount = 11,
        chapter = 29,
        jlpt = JlptLevel.N4
      ),
      Kanji(
        literal = "下",
        onReadings = listOf("カ", "ゲ"),
        kunReadings = listOf("した", "しも", "さ.がる"),
        meaningsEn = listOf("below", "down", "descend"),
        strokeCount = 3,
        chapter = 26,
        jlpt = JlptLevel.N5
      )
    )

    ChapterKanjiScreen(
      state = ChapterKanjiUiState(
        selectedChapter = 29,
        kanjiList = sampleKanji,
        isLoading = false
      ),
      onEvent = {},
      onBackClick = {},
      onKanjiClick = {}
    )
  }
}
