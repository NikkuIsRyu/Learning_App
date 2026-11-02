package tech.nikku.learningapp.feature.japanese.scramble

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import tech.nikku.learningapp.core.domain.engine.exercise.ScrambleValidationResult
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExercisePayload
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@Composable
fun SentenceBuilderRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SentenceBuilderViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  SentenceBuilderScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceBuilderScreen(
  state: SentenceBuilderUiState,
  onEvent: (SentenceBuilderEvent) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Sentence Builder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
      ScrambleCompletedView(
        correctCount = state.correctCount,
        totalCount = state.totalCount,
        onBackClick = onBackClick,
        modifier = Modifier.padding(padding)
      )
    } else {
      ScrambleActiveCardView(
        state = state,
        card = state.currentCard,
        onEvent = onEvent,
        modifier = Modifier.padding(padding)
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScrambleActiveCardView(
  state: SentenceBuilderUiState,
  card: ScrambleCardUiModel,
  onEvent: (SentenceBuilderEvent) -> Unit,
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

    // Translation Prompt Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SuggestionChip(
          onClick = {},
          label = { Text("Reconstruct the Japanese sentence", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        )
        Text(
          text = card.payload.translationEn,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        val de = card.payload.translationDe
        if (de != null) {
          Text(
            text = de,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Sentence Construction Slot (Assembled Sentence Area)
    val validation = card.validation
    val slotBorderColor = when {
      !card.isChecked -> MaterialTheme.colorScheme.outlineVariant
      validation?.isCorrect == true -> Color(0xFF4CAF50)
      else -> Color(0xFFE53935)
    }
    val slotBgColor = when {
      !card.isChecked -> MaterialTheme.colorScheme.surface
      validation?.isCorrect == true -> Color(0xFFF1F8E9)
      else -> Color(0xFFFFEBEE)
    }

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = slotBgColor),
      border = BorderStroke(1.5.dp, slotBorderColor)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        if (card.placedIndices.isEmpty()) {
          Text(
            text = "Tap word chunks below to assemble the sentence",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 16.dp)
          )
        } else {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            card.placedIndices.forEachIndexed { position, chunkIdx ->
              val text = card.payload.chunks.getOrElse(chunkIdx) { "" }
              ChunkChip(
                text = text,
                onClick = {
                  if (!card.isChecked) {
                    onEvent(SentenceBuilderEvent.TapPlacedChunk(position))
                  }
                },
                isPlaced = true,
                isChecked = card.isChecked,
                isCorrect = validation?.isCorrect == true
              )
            }
          }
        }
      }
    }

    // Available Words Pool
    if (card.availableChunks.isNotEmpty()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Available chunks:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          card.availableChunks.forEach { (chunkIdx, text) ->
            ChunkChip(
              text = text,
              onClick = {
                if (!card.isChecked) {
                  onEvent(SentenceBuilderEvent.TapAvailableChunk(chunkIdx))
                }
              },
              isPlaced = false,
              isChecked = card.isChecked,
              isCorrect = false
            )
          }
        }
      }
    }

    // Feedback & Action Area
    if (!card.isChecked) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = { onEvent(SentenceBuilderEvent.ResetCurrent) },
          enabled = card.placedIndices.isNotEmpty(),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Reset")
        }
        Button(
          onClick = { onEvent(SentenceBuilderEvent.CheckAnswer) },
          enabled = card.placedIndices.isNotEmpty(),
          modifier = Modifier.weight(2f),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Check Order", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      // Checked Feedback Banner
      val isCorrect = validation?.isCorrect == true
      val bannerContainer = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
      val bannerBorder = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
      val bannerText = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bannerContainer),
        border = BorderStroke(1.dp, bannerBorder),
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
              tint = bannerText,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = if (isCorrect) "Correct Order!" else "Incorrect Word Order",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = bannerText
            )
          }

          if (!isCorrect && validation != null) {
            Text(
              text = "Expected: ${validation.expectedSentence}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = bannerText
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            if (!isCorrect) {
              OutlinedButton(
                onClick = { onEvent(SentenceBuilderEvent.ResetCurrent) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                border = BorderStroke(1.dp, Color(0xFFE53935))
              ) {
                Text("Try Again")
              }
            }
            Button(
              onClick = { onEvent(SentenceBuilderEvent.NextCard) },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isCorrect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Text("Next", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ChunkChip(
  text: String,
  onClick: () -> Unit,
  isPlaced: Boolean,
  isChecked: Boolean,
  isCorrect: Boolean,
  modifier: Modifier = Modifier
) {
  val (bgColor, borderColor, contentColor) = when {
    !isPlaced -> Triple(
      MaterialTheme.colorScheme.surfaceContainerHigh,
      MaterialTheme.colorScheme.outlineVariant,
      MaterialTheme.colorScheme.onSurface
    )
    !isChecked -> Triple(
      MaterialTheme.colorScheme.primaryContainer,
      MaterialTheme.colorScheme.primary,
      MaterialTheme.colorScheme.onPrimaryContainer
    )
    isCorrect -> Triple(
      Color(0xFFE8F5E9),
      Color(0xFF4CAF50),
      Color(0xFF1B5E20)
    )
    else -> Triple(
      Color(0xFFFFEBEE),
      Color(0xFFE53935),
      Color(0xFFB71C1C)
    )
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(10.dp))
      .clickable(enabled = !isChecked || !isPlaced, onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    if (text.contains("{") && text.contains("|")) {
      FuriganaText(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = contentColor),
        rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = contentColor)
      )
    } else {
      Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = contentColor
      )
    }
  }
}

@Composable
private fun ScrambleCompletedView(
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
        Text("Builder Finished!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
fun SentenceBuilderPreview() {
  LearningAppTheme {
    val samplePayload = ExercisePayload.Scramble(
      chunks = listOf("明日", "病院で", "先生に", "喉を", "見てもらいます。"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3, 4),
        listOf(1, 0, 2, 3, 4)
      ),
      translationEn = "Tomorrow I will have the doctor examine my throat at the hospital.",
      translationDe = "Morgen lasse ich mir im Krankenhaus vom Arzt den Hals untersuchen."
    )

    val sampleExercise = Exercise(
      id = "ja:ex:scramble:26:01",
      domain = Domain.JAPANESE,
      chapter = 26,
      type = ExerciseType.SCRAMBLE,
      grammarRuleId = "ja:mnn:26.1",
      difficulty = 2,
      payload = samplePayload,
      verified = false,
      sortOrder = 1
    )

    val card = ScrambleCardUiModel(
      exercise = sampleExercise,
      payload = samplePayload,
      availableChunks = listOf(
        2 to "先生に",
        3 to "喉を",
        4 to "見てもらいます。"
      ),
      placedIndices = listOf(0, 1), // "明日", "病院で" placed
      validation = null,
      isChecked = false
    )

    val state = SentenceBuilderUiState(
      isLoading = false,
      chapter = 26,
      currentIndex = 0,
      totalCount = 12,
      currentCard = card,
      correctCount = 0
    )

    SentenceBuilderScreen(
      state = state,
      onEvent = {},
      onBackClick = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun SentenceBuilderWrongPreview() {
  LearningAppTheme {
    val samplePayload = ExercisePayload.Scramble(
      chunks = listOf("明日", "病院で", "先生に", "喉を", "見てもらいます。"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3, 4),
        listOf(1, 0, 2, 3, 4)
      ),
      translationEn = "Tomorrow I will have the doctor examine my throat at the hospital.",
      translationDe = "Morgen lasse ich mir im Krankenhaus vom Arzt den Hals untersuchen."
    )

    val sampleExercise = Exercise(
      id = "ja:ex:scramble:26:01",
      domain = Domain.JAPANESE,
      chapter = 26,
      type = ExerciseType.SCRAMBLE,
      grammarRuleId = "ja:mnn:26.1",
      difficulty = 2,
      payload = samplePayload,
      verified = false,
      sortOrder = 1
    )

    val card = ScrambleCardUiModel(
      exercise = sampleExercise,
      payload = samplePayload,
      availableChunks = emptyList(),
      placedIndices = listOf(4, 3, 2, 1, 0), // reversed wrong order
      validation = ScrambleValidationResult(
        isCorrect = false,
        userSentence = "見てもらいます。喉を先生に病院で明日",
        expectedSentence = "明日病院で先生に喉を見てもらいます。",
        matchedOrderIndex = null,
        isComplete = true
      ),
      isChecked = true
    )

    val state = SentenceBuilderUiState(
      isLoading = false,
      chapter = 26,
      currentIndex = 0,
      totalCount = 12,
      currentCard = card,
      correctCount = 0
    )

    SentenceBuilderScreen(
      state = state,
      onEvent = {},
      onBackClick = {}
    )
  }
}
