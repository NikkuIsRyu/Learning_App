package tech.nikku.learningapp.feature.japanese.chapter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.component.FormulaPills
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.designsystem.component.MarkdownText
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.model.*

@Composable
fun ChapterRoute(
  chapter: Int,
  modifier: Modifier = Modifier,
  onBackClick: () -> Unit = {},
  onStartDrill: (String) -> Unit = {},
  onKanjiClick: (String) -> Unit = {},
  viewModel: ChapterViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ChapterScreen(
    state = uiState,
    onEvent = { event ->
      when (event) {
        is ChapterEvent.Back -> onBackClick()
        is ChapterEvent.StartDrill -> onStartDrill(event.drillType)
        is ChapterEvent.OpenKanji -> onKanjiClick(event.literal)
        else -> viewModel.onEvent(event)
      }
    },
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(
  state: ChapterUiState,
  onEvent: (ChapterEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "第${state.chapter}課",
              fontWeight = FontWeight.Bold
            )
            if (state.chapter in 26..32) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = "Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = { onEvent(ChapterEvent.Back) }) {
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
      PrimaryTabRow(selectedTabIndex = state.selectedTab.ordinal) {
        ChapterTab.entries.forEach { tab ->
          Tab(
            selected = state.selectedTab == tab,
            onClick = { onEvent(ChapterEvent.SelectTab(tab)) },
            text = { Text(text = tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
          )
        }
      }

      when (state.selectedTab) {
        ChapterTab.GRAMMAR -> GrammarTabContent(state = state, onEvent = onEvent)
        ChapterTab.WORDS -> WordsTabContent(state = state)
        ChapterTab.KANJI -> KanjiTabContent(state = state, onEvent = onEvent)
        ChapterTab.DRILLS -> DrillsTabContent(onEvent = onEvent)
      }
    }
  }
}

@Composable
private fun GrammarTabContent(
  state: ChapterUiState,
  onEvent: (ChapterEvent) -> Unit
) {
  if (state.rules.isEmpty()) {
    EmptyTabPlaceholder(message = "No grammar rules loaded for Chapter ${state.chapter}.")
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(state.rules, key = { it.id }) { rule ->
        val isExpanded = state.expandedRuleId == rule.id
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent(ChapterEvent.ToggleRule(rule.id)) }
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = rule.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
              )
              Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
              )
            }

            if (rule.formulaPills.isNotEmpty()) {
              FormulaPills(tokens = rule.formulaPills)
            } else {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
              ) {
                Text(
                  text = rule.formula,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Text(
              text = rule.explanationEn,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = isExpanded) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                HorizontalDivider()

                if (rule.examples.isNotEmpty()) {
                  Text(
                    text = "Examples",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  rule.examples.forEach { example ->
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                    ) {
                      FuriganaText(
                        text = example.rubyMarkup ?: example.japanese ?: "",
                        style = MaterialTheme.typography.bodyLarge
                      )
                      Text(
                        text = example.translationEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun WordsTabContent(state: ChapterUiState) {
  if (state.words.isEmpty()) {
    EmptyTabPlaceholder(message = "No words loaded for Chapter ${state.chapter}.")
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(state.words, key = { it.id }) { word ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.weight(1f)
            ) {
              FuriganaText(
                text = word.rubyMarkup ?: word.headword,
                style = MaterialTheme.typography.titleMedium
              )
              Text(
                text = word.meaningEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.secondaryContainer
            ) {
              Text(
                text = word.pos.name.replace('_', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KanjiTabContent(
  state: ChapterUiState,
  onEvent: (ChapterEvent) -> Unit
) {
  if (state.kanji.isEmpty()) {
    EmptyTabPlaceholder(message = "No kanji introduced in Chapter ${state.chapter}.")
  } else {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(state.kanji, key = { it.literal }) { k ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onEvent(ChapterEvent.OpenKanji(k.literal)) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(56.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = k.literal,
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            Column(
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = k.meaningsEn.joinToString(", "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              if (k.onReadings.isNotEmpty()) {
                Text(
                  text = "音: ${k.onReadings.joinToString("、")}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
              if (k.kunReadings.isNotEmpty()) {
                Text(
                  text = "訓: ${k.kunReadings.joinToString("、")}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Text(
              text = "${k.strokeCount}画",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.outline
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DrillsTabContent(onEvent: (ChapterEvent) -> Unit) {
  val drills = listOf(
    "conjugation" to "Conjugation Matrix Drills" to "Practice Group I, II, III inflections with instant diff feedback",
    "cloze" to "Particle Cloze Fill-in-the-Blank" to "Master は, が, を, に, で particles with context feedback",
    "scramble" to "Sentence Builder (Scramble)" to "Reconstruct natural Japanese expressions chunk by chunk",
    "listening" to "Listening & Dialogue Player" to "Shadow native cadence audio with kana/furigana controls"
  )

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    items(drills) { item ->
      val (pair, desc) = item
      val (drillId, title) = pair
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onEvent(ChapterEvent.StartDrill(drillId)) }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = desc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline
            )
          }

          FilledIconButton(onClick = { onEvent(ChapterEvent.StartDrill(drillId)) }) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Drill")
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyTabPlaceholder(message: String) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.outline
    )
  }
}

@Preview(name = "Chapter Screen Preview", showBackground = true)
@Composable
fun ChapterScreenPreview() {
  val fakeRules = listOf(
    GrammarRule(
      id = "ja:mnn:26.1",
      domain = Domain.JAPANESE,
      chapter = 26,
      titleEn = "~んです / ~のです",
      formula = "V-plain + んです",
      formulaPills = listOf(
        PatternToken("V-plain", SyntaxRole.VERB),
        PatternToken("+", SyntaxRole.CONNECTOR),
        PatternToken("んです", SyntaxRole.PARTICLE)
      ),
      explanationEn = "Used when asking for an explanation or giving an emphasized reason.",
      examples = listOf(
        GrammarExample(
          ruleId = "ja:mnn:26.1",
          idx = 1,
          rubyMarkup = "{行|い}くんです。",
          translationEn = "I am going (explaining)."
        )
      )
    )
  )

  val fakeWords = listOf(
    Word(
      id = "w1",
      domain = Domain.JAPANESE,
      headword = "診ます",
      reading = "みます",
      rubyMarkup = "{診|み}ます",
      meaningEn = "check, examine",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.ICHIDAN,
      chapter = 26,
      verified = true
    ),
    Word(
      id = "w2",
      domain = Domain.JAPANESE,
      headword = "探します",
      reading = "さがします",
      rubyMarkup = "{探|さが}します",
      meaningEn = "look for, search",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.GODAN,
      chapter = 26,
      verified = true
    )
  )

  val fakeKanji = listOf(
    Kanji(
      literal = "探",
      strokeCount = 11,
      onReadings = listOf("タン"),
      kunReadings = listOf("さが.す", "さぐ.る"),
      meaningsEn = listOf("grope", "search", "look for"),
      chapter = 26
    )
  )

  LearningAppTheme(domain = AppDomain.JAPANESE) {
    Surface {
      ChapterScreen(
        state = ChapterUiState(
          chapter = 26,
          selectedTab = ChapterTab.GRAMMAR,
          rules = fakeRules,
          words = fakeWords,
          kanji = fakeKanji,
          expandedRuleId = "ja:mnn:26.1"
        ),
        onEvent = {}
      )
    }
  }
}
