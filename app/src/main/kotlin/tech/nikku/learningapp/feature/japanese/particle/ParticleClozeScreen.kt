package tech.nikku.learningapp.feature.japanese.particle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import tech.nikku.learningapp.core.domain.engine.exercise.ClozeEvaluation
import tech.nikku.learningapp.core.domain.model.ClozeSegment
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExercisePayload
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@Composable
fun ParticleClozeRoute(
  onBackClick: () -> Unit,
  onNavigateToRule: (String) -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: ParticleClozeViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ParticleClozeScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    onNavigateToRule = onNavigateToRule,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticleClozeScreen(
  state: ParticleClozeUiState,
  onEvent: (ParticleClozeEvent) -> Unit,
  onBackClick: () -> Unit,
  onNavigateToRule: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Particle Cloze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Chapter ${state.chapter}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    if (state.isLoading) {
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    } else if (state.isCompleted || state.currentCard == null) {
      ClozeCompletedView(
        correctCount = state.correctCount,
        totalCount = state.totalCount,
        onBackClick = onBackClick,
        modifier = Modifier.padding(padding)
      )
    } else {
      ClozeActiveCardView(
        state = state,
        card = state.currentCard,
        onEvent = onEvent,
        onNavigateToRule = onNavigateToRule,
        modifier = Modifier.padding(padding)
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClozeActiveCardView(
  state: ParticleClozeUiState,
  card: ParticleClozeCardUiModel,
  onEvent: (ParticleClozeEvent) -> Unit,
  onNavigateToRule: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Progress Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        "Question ${state.currentIndex + 1} of ${state.totalCount}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        "Score: ${state.correctCount}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
    LinearProgressIndicator(
      progress = { if (state.totalCount > 0) (state.currentIndex + 1).toFloat() / state.totalCount else 0f },
      modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
    )

    // Sentence Card with inline blanks
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        SuggestionChip(
          onClick = {},
          label = { Text("Choose the correct particle", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )

        // Sentence flow with blanks
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          card.payload.segments.forEachIndexed { index, segment ->
            when (segment) {
              is ClozeSegment.Text -> {
                if (segment.ruby.contains("{") || segment.ruby.contains("[")) {
                  FuriganaText(
                    text = segment.ruby,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                  )
                } else {
                  Text(
                    text = segment.ruby,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                  )
                }
              }
              is ClozeSegment.Blank -> {
                val eval = card.evaluations[index]
                val selectedOpt = card.selectedOptions[index]

                val (blankBg, blankBorder, blankText) = when {
                  eval == null -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                  eval.isCorrect -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), Color(0xFF1B5E20))
                  else -> Triple(Color(0xFFFFEBEE), Color(0xFFE53935), Color(0xFFB71C1C))
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(blankBg)
                    .border(1.5.dp, blankBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .align(Alignment.CenterVertically),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = selectedOpt ?: "___",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = blankText
                  )
                }
              }
            }
          }
        }

        // Translation text
        Text(
          text = card.payload.translationEn,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Active Blank Options
    val firstUnansweredBlank = card.payload.segments.withIndex()
      .firstOrNull { it.value is ClozeSegment.Blank && !card.evaluations.containsKey(it.index) }

    val activeBlankEntry = firstUnansweredBlank
      ?: card.payload.segments.withIndex().firstOrNull { it.value is ClozeSegment.Blank }

    if (activeBlankEntry != null && activeBlankEntry.value is ClozeSegment.Blank) {
      val blankIndex = activeBlankEntry.index
      val blank = activeBlankEntry.value as ClozeSegment.Blank
      val isAnswered = card.evaluations.containsKey(blankIndex)

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Select particle:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            blank.options.forEach { option ->
              val isSelected = card.selectedOptions[blankIndex] == option
              FilterChip(
                selected = isSelected,
                onClick = {
                  if (!isAnswered) {
                    onEvent(ParticleClozeEvent.SelectOption(blankIndex, option))
                  }
                },
                enabled = !isAnswered,
                label = {
                  Text(
                    text = option,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
              )
            }
          }
        }
      }
    }

    // Feedback section for evaluated blanks
    card.evaluations.forEach { (blankIndex, eval) ->
      ClozeFeedbackCard(
        evaluation = eval,
        onLearnWhyClick = { eval.ruleRef?.let(onNavigateToRule) }
      )
    }

    // Next Button (enabled when all blanks answered)
    val totalBlanks = card.payload.segments.count { it is ClozeSegment.Blank }
    val allAnswered = card.evaluations.size >= totalBlanks

    if (allAnswered) {
      Button(
        onClick = { onEvent(ParticleClozeEvent.NextCard) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Next Exercise", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun ClozeFeedbackCard(
  evaluation: ClozeEvaluation,
  onLearnWhyClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isCorrect = evaluation.isCorrect
  val containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
  val borderColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
  val textColor = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          if (isCorrect) Icons.Default.Check else Icons.Default.Close,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = if (isCorrect) "Correct!" else "Incorrect Choice",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = textColor
        )
      }

      val feedbackText = evaluation.feedback
      if (feedbackText != null) {
        Text(
          text = feedbackText,
          style = MaterialTheme.typography.bodyMedium,
          color = textColor
        )
      }

      val ruleRef = evaluation.ruleRef
      if (!isCorrect && ruleRef != null) {
        OutlinedButton(
          onClick = onLearnWhyClick,
          modifier = Modifier.padding(top = 4.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935))
        ) {
          Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Learn why (${ruleRef})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
private fun ClozeCompletedView(
  correctCount: Int,
  totalCount: Int,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxSize().padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text("Drill Finished!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
          "You completed $correctCount out of $totalCount correctly.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
          onClick = onBackClick,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Back to Chapter")
        }
      }
    }
  }
}

// ──────────────────────────────── PREVIEWS ────────────────────────────────

@Preview(showBackground = true)
@Composable
fun ParticleClozeFeedbackPreview() {
  LearningAppTheme {
    val samplePayload = ExercisePayload.ParticleCloze(
      segments = listOf(
        ClozeSegment.Text("{窓|まど}"),
        ClozeSegment.Blank(
          answers = listOf("が"),
          options = listOf("は", "が", "を", "に"),
          feedback = mapOf(
            "を" to "Direct object marker を is used with transitive actions, not with intransitive verbs or states.",
            "は" to "は marks the global topic, whereas が marks the specific subject.",
            "に" to "に marks target or location, not the subject."
          ),
          ruleRef = "ja:mnn:29.1"
        ),
        ClozeSegment.Text(" {割|わ}れて います。")
      ),
      translationEn = "The window is broken."
    )

    val sampleExercise = Exercise(
      id = "ja:ex:cloze:29:01",
      domain = Domain.JAPANESE,
      chapter = 29,
      type = ExerciseType.PARTICLE_CLOZE,
      grammarRuleId = "ja:mnn:29.1",
      difficulty = 2,
      payload = samplePayload,
      verified = false,
      sortOrder = 1
    )

    val card = ParticleClozeCardUiModel(
      exercise = sampleExercise,
      payload = samplePayload,
      selectedOptions = mapOf(1 to "を"),
      evaluations = mapOf(
        1 to ClozeEvaluation(
          isCorrect = false,
          selectedOption = "を",
          correctAnswers = listOf("が"),
          feedback = "Direct object marker を is used with transitive actions, not with intransitive verbs or states.",
          ruleRef = "ja:mnn:29.1"
        )
      )
    )

    val state = ParticleClozeUiState(
      isLoading = false,
      chapter = 29,
      currentIndex = 0,
      totalCount = 12,
      currentCard = card,
      correctCount = 0
    )

    ParticleClozeScreen(
      state = state,
      onEvent = {},
      onBackClick = {},
      onNavigateToRule = {}
    )
  }
}
