package tech.nikku.learningapp.feature.japanese.conjugation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.engine.conjugation.Conjugation
import tech.nikku.learningapp.core.domain.engine.exercise.AnswerDiff
import tech.nikku.learningapp.core.domain.engine.exercise.AnswerResult
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.feature.japanese.conjugation.components.KanaInputField
import tech.nikku.learningapp.ui.theme.LearningAppTheme

@Composable
fun ConjugationDrillRoute(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ConjugationDrillViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ConjugationDrillScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onBackClick = onBackClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationDrillScreen(
  state: ConjugationDrillUiState,
  onEvent: (ConjugationDrillEvent) -> Unit,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Conjugation Drill", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
      DrillCompletedView(
        correctCount = state.correctCount,
        totalCards = state.totalCards,
        onBackClick = onBackClick,
        modifier = Modifier.padding(padding)
      )
    } else {
      DrillActiveCardView(
        state = state,
        card = state.currentCard,
        onEvent = onEvent,
        modifier = Modifier.padding(padding)
      )
    }
  }
}

@Composable
private fun DrillActiveCardView(
  state: ConjugationDrillUiState,
  card: DrillCard,
  onEvent: (ConjugationDrillEvent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Progress bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        "Card ${state.currentCardIndex + 1} of ${state.totalCards}",
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
      progress = { if (state.totalCards > 0) (state.currentCardIndex + 1).toFloat() / state.totalCards else 0f },
      modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
    )

    // Prompt card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Group chip
        val groupName = when (card.word.verbGroup) {
          VerbGroup.GODAN -> "Group I · Godan"
          VerbGroup.ICHIDAN -> "Group II · Ichidan"
          VerbGroup.SURU -> "Group III · Suru"
          VerbGroup.KURU -> "Group III · Kuru"
          null -> "Verb"
        }
        SuggestionChip(
          onClick = {},
          label = { Text(groupName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          modifier = Modifier.padding(bottom = 8.dp)
        )

        // Headword with Furigana
        val ruby = card.word.rubyMarkup
        if (ruby != null) {
          FuriganaText(
            text = ruby,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            rubyStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp)
          )
        } else {
          Text(
            text = card.word.headword,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = card.word.meaningEn,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Transformation Chips: Source Form -> Target Form
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = card.sourceForm.name,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "transforms to",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${card.targetForm.name} FORM",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // Input Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        KanaInputField(
          value = state.input,
          onValueChange = { onEvent(ConjugationDrillEvent.UpdateInput(it)) },
          onSubmit = { onEvent(ConjugationDrillEvent.Submit) },
          enabled = state.result == null,
          placeholder = "Type ${card.targetForm.name.lowercase()} form..."
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { onEvent(ConjugationDrillEvent.RequestHint) },
            enabled = state.result == null && !state.hintShown
          ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Hint")
          }

          Button(
            onClick = { onEvent(ConjugationDrillEvent.Submit) },
            enabled = state.input.isNotBlank() && state.result == null
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Check")
          }
        }

        // Hint text if requested
        if (state.hintShown && state.result == null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFFFF8E1))
              .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Rule: ${card.expected.ruleExplanation}",
                fontSize = 12.sp,
                color = Color(0xFF5D4037)
              )
            }
          }
        }
      }
    }

    // Feedback Result Section
    if (state.result != null) {
      FeedbackResultCard(
        result = state.result,
        onNextCard = { onEvent(ConjugationDrillEvent.NextCard) }
      )
    }
  }
}

@Composable
private fun FeedbackResultCard(
  result: AnswerResult,
  onNextCard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isCorrect = result.isCorrect
  val containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
  val borderColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
  val titleColor = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          if (isCorrect) Icons.Default.Check else Icons.Default.Close,
          contentDescription = null,
          tint = titleColor,
          modifier = Modifier.size(24.dp)
        )
        Text(
          text = if (isCorrect) "Correct!" else "Incorrect",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = titleColor
        )
      }

      // If incorrect, render highlighted ending diff
      val diff = result.diff
      if (!isCorrect && diff != null) {
        EndingDiffView(diff = diff)
      }

      // If accepted override note exists (e.g. ra-nuki note)
      val note = result.note
      if (note != null) {
        Text(
          text = "Note: $note",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF33691E),
          fontWeight = FontWeight.Medium
        )
      }

      // Rule explanation
      val rule = result.ruleExplanation
      if (rule != null) {
        Text(
          text = rule,
          style = MaterialTheme.typography.bodySmall,
          color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Button(
        onClick = onNextCard,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
      ) {
        Text("Continue", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun EndingDiffView(
  diff: AnswerDiff,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.White)
      .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(8.dp))
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text(
      text = "Ending comparison:",
      style = MaterialTheme.typography.labelSmall,
      color = Color.Gray
    )

    // User's answer with wrong ending highlighted
    val userAnnotated = buildAnnotatedString {
      append("You wrote: ")
      append(diff.commonPrefix)
      withStyle(
        SpanStyle(
          color = Color(0xFFC62828),
          background = Color(0xFFFFCDD2),
          fontWeight = FontWeight.Bold,
          textDecoration = TextDecoration.LineThrough
        )
      ) {
        append(diff.userEnding.ifEmpty { "∅" })
      }
    }
    Text(text = userAnnotated, fontSize = 14.sp)

    // Expected answer with correct ending highlighted
    val expectedAnnotated = buildAnnotatedString {
      append("Expected:   ")
      append(diff.commonPrefix)
      withStyle(
        SpanStyle(
          color = Color(0xFF2E7D32),
          background = Color(0xFFC8E6C9),
          fontWeight = FontWeight.Bold
        )
      ) {
        append(diff.expectedEnding)
      }
    }
    Text(text = expectedAnnotated, fontSize = 14.sp)
  }
}

@Composable
private fun DrillCompletedView(
  correctCount: Int,
  totalCards: Int,
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
        Text("Session Complete!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
          "You scored $correctCount out of $totalCards",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
          onClick = onBackClick,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Return to Hub")
        }
      }
    }
  }
}

// ──────────────────────────────── PREVIEWS ────────────────────────────────

@Preview(showBackground = true)
@Composable
fun ConjugationDrillCorrectPreview() {
  LearningAppTheme {
    val sampleWord = Word(
      id = "w_26_01",
      domain = Domain.JAPANESE,
      headword = "食べる",
      reading = "たべる",
      rubyMarkup = "{食|た}べる",
      meaningEn = "eat",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.ICHIDAN,
      chapter = 26
    )

    val expected = Conjugation(
      surface = "食べられる",
      reading = "たべられる",
      ruleExplanation = "Group II (Ichidan): drop る + られる",
      acceptedOverrides = listOf("食べれる", "たべれる"),
      notes = listOf("ら抜き (colloquial): 食べれる")
    )

    val card = DrillCard(
      word = sampleWord,
      sourceForm = VerbForm.DICTIONARY,
      targetForm = VerbForm.POTENTIAL,
      polite = false,
      expected = expected
    )

    val state = ConjugationDrillUiState(
      isLoading = false,
      currentCardIndex = 1,
      totalCards = 5,
      currentCard = card,
      input = "食べれる",
      result = AnswerResult(
        isCorrect = true,
        normalizedInput = "食べれる",
        matchedTarget = "食べれる",
        ruleExplanation = "Group II (Ichidan): drop る + られる",
        note = "ら抜き (colloquial): 食べれる"
      ),
      correctCount = 1
    )

    ConjugationDrillScreen(
      state = state,
      onEvent = {},
      onBackClick = {}
    )
  }
}

@Preview(showBackground = true)
@Composable
fun ConjugationDrillWrongPreview() {
  LearningAppTheme {
    val sampleWord = Word(
      id = "w_26_02",
      domain = Domain.JAPANESE,
      headword = "書く",
      reading = "かく",
      rubyMarkup = "{書|か}く",
      meaningEn = "write, compose",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.GODAN,
      chapter = 26
    )

    val expected = Conjugation(
      surface = "書いた",
      reading = "かいた",
      ruleExplanation = "Group I (Godan): past ta-form (く → いた)"
    )

    val card = DrillCard(
      word = sampleWord,
      sourceForm = VerbForm.DICTIONARY,
      targetForm = VerbForm.TA,
      polite = false,
      expected = expected
    )

    val state = ConjugationDrillUiState(
      isLoading = false,
      currentCardIndex = 0,
      totalCards = 5,
      currentCard = card,
      input = "書いて",
      result = AnswerResult(
        isCorrect = false,
        normalizedInput = "書いて",
        diff = AnswerDiff(
          commonPrefix = "書い",
          userEnding = "て",
          expectedEnding = "た"
        ),
        ruleExplanation = "Expected \"かいた\" (書いた): Group I (Godan): past ta-form (く → いた)"
      ),
      correctCount = 0
    )

    ConjugationDrillScreen(
      state = state,
      onEvent = {},
      onBackClick = {}
    )
  }
}
