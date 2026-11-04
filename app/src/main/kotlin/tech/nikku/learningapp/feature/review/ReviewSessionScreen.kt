package tech.nikku.learningapp.feature.review

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.model.ContentSource
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.feature.review.cards.KanjiReadingCard
import tech.nikku.learningapp.feature.review.cards.MeaningRecallCard

@Composable
fun ReviewSessionDestination(
  onFinishClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ReviewSessionViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ReviewSessionScreen(
    state = uiState,
    onEvent = viewModel::onEvent,
    onFinishClick = onFinishClick,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSessionScreen(
  state: ReviewSessionUiState,
  onEvent: (ReviewSessionEvent) -> Unit,
  onFinishClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    topBar = {
      Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
          title = {
            if (!state.isFinished && state.items.isNotEmpty()) {
              val current = state.currentIndex + 1
              val total = state.items.size
              Text(
                text = "Review ($current / $total)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            } else {
              Text(
                text = "Session Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          },
          navigationIcon = {
            IconButton(onClick = onFinishClick) {
              Icon(imageVector = Icons.Default.Close, contentDescription = "Exit review session")
            }
          }
        )

        if (!state.isFinished && state.items.isNotEmpty()) {
          LinearProgressIndicator(
            progress = { state.progressFraction },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    bottomBar = {
      if (!state.isFinished && state.isAnswerRevealed && state.currentItem != null) {
        GradingButtonBar(
          onGradeSelected = { grade ->
            onEvent(ReviewSessionEvent.SubmitGrade(grade))
          }
        )
      }
    },
    modifier = modifier
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (state.isLoading) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator()
        }
      } else if (state.isFinished || state.items.isEmpty()) {
        // Session Completed View
        SessionCompleteView(
          stats = state.stats,
          onFinishClick = onFinishClick,
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        )
      } else {
        // Active Card Content
        val item = state.currentItem
        if (item != null) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            val word = item.word
            val kanji = item.kanji
            when {
              word != null -> {
                MeaningRecallCard(
                  word = word,
                  isAnswerRevealed = state.isAnswerRevealed,
                  onRevealClick = { onEvent(ReviewSessionEvent.RevealAnswer) }
                )
              }
              kanji != null -> {
                KanjiReadingCard(
                  kanji = kanji,
                  isAnswerRevealed = state.isAnswerRevealed,
                  onRevealClick = { onEvent(ReviewSessionEvent.RevealAnswer) }
                )
              }
              else -> {
                GenericReviewCard(
                  item = item,
                  isAnswerRevealed = state.isAnswerRevealed,
                  onRevealClick = { onEvent(ReviewSessionEvent.RevealAnswer) }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GradingButtonBar(
  onGradeSelected: (ReviewGrade) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shadowElevation = 8.dp,
    color = MaterialTheme.colorScheme.surface,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // AGAIN (<1d)
      Button(
        onClick = { onGradeSelected(ReviewGrade.AGAIN) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.weight(1f)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Again", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          Text(text = "<10m", fontSize = 10.sp)
        }
      }

      // HARD (1d)
      Button(
        onClick = { onGradeSelected(ReviewGrade.HARD) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.tertiaryContainer,
          contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.weight(1f)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Hard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          Text(text = "1d", fontSize = 10.sp)
        }
      }

      // GOOD (3d)
      Button(
        onClick = { onGradeSelected(ReviewGrade.GOOD) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.weight(1f)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Good", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          Text(text = "3d", fontSize = 10.sp)
        }
      }

      // EASY (5d)
      Button(
        onClick = { onGradeSelected(ReviewGrade.EASY) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.weight(1f)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "Easy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          Text(text = "5d", fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
fun GenericReviewCard(
  item: ReviewItem,
  isAnswerRevealed: Boolean,
  onRevealClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Item: ${item.progress.itemKey}",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )

      if (!isAnswerRevealed) {
        Button(
          onClick = onRevealClick,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(text = "Show Answer")
        }
      } else {
        Text(
          text = "Type: ${item.progress.itemType.name} • State: ${item.progress.state.name}",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
fun SessionCompleteView(
  stats: ReviewSessionStats,
  onFinishClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = "🎉", fontSize = 64.sp)
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "Session Complete!",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Great job! All cards in this session have been reviewed.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Stats Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth(0.9f)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        StatItem(label = "Again", count = stats.againCount, color = MaterialTheme.colorScheme.error)
        StatItem(label = "Hard", count = stats.hardCount, color = MaterialTheme.colorScheme.tertiary)
        StatItem(label = "Good", count = stats.goodCount, color = MaterialTheme.colorScheme.secondary)
        StatItem(label = "Easy", count = stats.easyCount, color = MaterialTheme.colorScheme.primary)
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
      onClick = onFinishClick,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth(0.7f)
    ) {
      Text(
        text = "Done",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
private fun StatItem(
  label: String,
  count: Int,
  color: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = "$count",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = color
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Preview(name = "Review Session Meaning Recall", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ReviewSessionMeaningPreview() {
  LearningAppTheme {
    val sampleWord = Word(
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
      chapter = 29,
      jlpt = JlptLevel.N4,
      source = ContentSource.MINNA,
      verified = true
    )

    val sampleItem = ReviewItem(
      progress = UserProgress(
        itemKey = sampleWord.id,
        itemType = ItemType.WORD,
        domain = Domain.JAPANESE,
        state = SrsState.REVIEW
      ),
      word = sampleWord
    )

    ReviewSessionScreen(
      state = ReviewSessionUiState(
        items = listOf(sampleItem),
        currentIndex = 0,
        isAnswerRevealed = true,
        isFinished = false,
        isLoading = false
      ),
      onEvent = {},
      onFinishClick = {}
    )
  }
}
