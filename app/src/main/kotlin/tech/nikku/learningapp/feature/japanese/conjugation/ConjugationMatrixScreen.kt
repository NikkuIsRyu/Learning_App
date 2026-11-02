package tech.nikku.learningapp.feature.japanese.conjugation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.feature.japanese.conjugation.components.MatrixCell
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@Composable
fun ConjugationMatrixRoute(
  onNavigateToDrill: (wordId: String, form: VerbForm?) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ConjugationMatrixViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ConjugationMatrixScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onCellClick = { wordId, form -> onNavigateToDrill(wordId, form) },
    onDrillWeakestClick = { onNavigateToDrill("", null) },
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationMatrixScreen(
  state: ConjugationMatrixUiState,
  onEvent: (ConjugationMatrixEvent) -> Unit,
  onCellClick: (wordId: String, form: VerbForm) -> Unit,
  onDrillWeakestClick: () -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Conjugation Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              "${state.rows.size} verbs · Active chapters: ${state.activeChapters.minOrNull() ?: 26}–${state.activeChapters.maxOrNull() ?: 32}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Button(
            onClick = onDrillWeakestClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Drill Weakest", fontSize = 12.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Search bar
      OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { onEvent(ConjugationMatrixEvent.UpdateSearchQuery(it)) },
        placeholder = { Text("Filter verbs (e.g. 書く, write, L26)...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      )

      // Chapter filter chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        item {
          FilterChip(
            selected = state.selectedChapter == null,
            onClick = { onEvent(ConjugationMatrixEvent.SelectChapter(null)) },
            label = { Text("All Chapters") }
          )
        }
        items(state.activeChapters.sorted()) { ch ->
          FilterChip(
            selected = state.selectedChapter == ch,
            onClick = { onEvent(ConjugationMatrixEvent.SelectChapter(ch)) },
            label = { Text("Ch $ch") }
          )
        }
      }

      if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      } else if (state.rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("No verbs found matching the criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      } else {
        // Table content: LazyColumn of rows
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          // Sticky header for forms
          item(key = "header_row") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(vertical = 8.dp, horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Verb column spacer
              Text(
                "VERB",
                modifier = Modifier.width(130.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              // Form column headers
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
              ) {
                items(state.visibleForms, key = { it.name }) { form ->
                  Box(
                    modifier = Modifier.width(54.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = formShortLabel(form),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }

          items(state.rows, key = { it.word.id }) { row ->
            MatrixVerbRow(
              row = row,
              visibleForms = state.visibleForms,
              onCellClick = { form -> onCellClick(row.word.id, form) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MatrixVerbRow(
  row: MatrixRowUiModel,
  visibleForms: List<VerbForm>,
  onCellClick: (VerbForm) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp, horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left column: Verb Info (Width = 130.dp)
    Column(
      modifier = Modifier
        .width(130.dp)
        .padding(end = 8.dp)
    ) {
      val ruby = row.word.rubyMarkup
      if (ruby != null) {
        FuriganaText(
          text = ruby,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
          rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
        )
      } else {
        Text(
          text = row.word.headword,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = row.word.meaningEn,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 2.dp)
      ) {
        val groupLabel = when (row.word.verbGroup) {
          VerbGroup.GODAN -> "G I"
          VerbGroup.ICHIDAN -> "G II"
          VerbGroup.SURU, VerbGroup.KURU -> "G III"
          null -> ""
        }
        if (groupLabel.isNotEmpty()) {
          Text(
            text = groupLabel,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
          )
        }
        if (row.word.chapter != null) {
          Text(
            text = "L${row.word.chapter}",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    }

    // Right column: Horizontal scrollable form cells
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(visibleForms, key = { form -> "${row.word.id}_${form.name}" }) { form ->
        val progress = row.cells[form]
        MatrixCell(
          form = form,
          state = progress?.state,
          intervalDays = progress?.intervalDays ?: 0.0,
          onClick = { onCellClick(form) }
        )
      }
    }
  }
}

private fun formShortLabel(form: VerbForm): String = when (form) {
  VerbForm.MASU -> "MASU"
  VerbForm.DICTIONARY -> "DICT"
  VerbForm.NAI -> "NAI"
  VerbForm.TA -> "TA"
  VerbForm.TE -> "TE"
  VerbForm.POTENTIAL -> "POT"
  VerbForm.VOLITIONAL -> "VOL"
  VerbForm.PASSIVE -> "PAS"
  VerbForm.CAUSATIVE -> "CAUS"
  VerbForm.CAUSATIVE_PASSIVE -> "C-PAS"
}

@Preview(showBackground = true)
@Composable
fun ConjugationMatrixScreenPreview() {
  LearningAppTheme {
    val sampleWord1 = Word(
      id = "w_26_01",
      domain = Domain.JAPANESE,
      headword = "書く",
      reading = "かく",
      rubyMarkup = "{書|か}く",
      meaningEn = "write, compose",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.GODAN,
      chapter = 26
    )
    val sampleWord2 = Word(
      id = "w_26_02",
      domain = Domain.JAPANESE,
      headword = "食べる",
      reading = "たべる",
      rubyMarkup = "{食|た}べる",
      meaningEn = "eat",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.ICHIDAN,
      chapter = 26
    )
    val sampleWord3 = Word(
      id = "w_27_01",
      domain = Domain.JAPANESE,
      headword = "勉強する",
      reading = "べんきょうする",
      rubyMarkup = "{勉強|べんきょう}する",
      meaningEn = "study",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.SURU,
      chapter = 27
    )

    val rows = listOf(
      MatrixRowUiModel(
        word = sampleWord1,
        cells = mapOf(
          VerbForm.MASU to UserProgress(itemKey = "conj:w_26_01:MASU", itemType = tech.nikku.learningapp.core.domain.model.ItemType.CONJUGATION_CELL, domain = Domain.JAPANESE, state = SrsState.REVIEW, intervalDays = 14.0),
          VerbForm.TE to UserProgress(itemKey = "conj:w_26_01:TE", itemType = tech.nikku.learningapp.core.domain.model.ItemType.CONJUGATION_CELL, domain = Domain.JAPANESE, state = SrsState.LEARNING, intervalDays = 0.0),
          VerbForm.POTENTIAL to UserProgress(itemKey = "conj:w_26_01:POTENTIAL", itemType = tech.nikku.learningapp.core.domain.model.ItemType.CONJUGATION_CELL, domain = Domain.JAPANESE, state = SrsState.NEW, intervalDays = 0.0)
        )
      ),
      MatrixRowUiModel(
        word = sampleWord2,
        cells = mapOf(
          VerbForm.MASU to UserProgress(itemKey = "conj:w_26_02:MASU", itemType = tech.nikku.learningapp.core.domain.model.ItemType.CONJUGATION_CELL, domain = Domain.JAPANESE, state = SrsState.REVIEW, intervalDays = 30.0)
        )
      ),
      MatrixRowUiModel(word = sampleWord3)
    )

    ConjugationMatrixScreen(
      state = ConjugationMatrixUiState(
        isLoading = false,
        activeChapters = setOf(26, 27, 28),
        rows = rows
      ),
      onEvent = {},
      onCellClick = { _, _ -> },
      onDrillWeakestClick = {},
      onBackClick = {}
    )
  }
}
