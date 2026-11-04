package tech.nikku.learningapp.feature.review.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.Word

@Composable
fun MeaningRecallCard(
  word: Word,
  isAnswerRevealed: Boolean,
  onRevealClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(20.dp),
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
      // Badges: Chapter & POS
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        val ch = word.chapter
        if (ch != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Text(
              text = "Chapter $ch",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = word.pos.name.replace('_', ' '),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Prompt: Word headword with ruby or plain
      val ruby = word.rubyMarkup
      if (ruby != null && ruby.contains("{")) {
        FuriganaText(
          text = ruby,
          style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold),
          rubyStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp)
        )
      } else {
        Text(
          text = word.headword,
          fontSize = 36.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center
        )
      }

      if (word.reading.isNotBlank()) {
        Text(
          text = word.reading,
          fontSize = 20.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Reveal / Answer Section
      if (!isAnswerRevealed) {
        Button(
          onClick = onRevealClick,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(0.7f)
        ) {
          Text(
            text = "Show Answer",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
          )
        }
      } else {
        AnimatedVisibility(
          visible = isAnswerRevealed,
          enter = fadeIn() + slideInVertically()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            HorizontalDivider(
              modifier = Modifier.padding(vertical = 8.dp),
              color = MaterialTheme.colorScheme.outlineVariant
            )

            // English Meaning
            Text(
              text = word.meaningEn ?: "",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center
            )

            // German Meaning (if present)
            val de = word.meaningDe
            if (!de.isNullOrBlank()) {
              Text(
                text = de,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
              )
            }

            // Grammatical details: Transitivity or VerbGroup
            val transitivity = word.transitivity
            val verbGroup = word.verbGroup
            if (transitivity != null || verbGroup != null) {
              val details = listOfNotNull(
                verbGroup?.name?.let { "Group: $it" },
                transitivity?.name?.let { "Transitivity: $it" }
              ).joinToString(" • ")

              Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }
        }
      }
    }
  }
}
