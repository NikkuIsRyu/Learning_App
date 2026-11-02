package tech.nikku.learningapp.feature.japanese.lexicon.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.Word

@Composable
fun TransitivityPairCard(
  word: Word,
  partnerWord: Word?,
  onPartnerClick: (Word) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Transitivity Pair (自他動詞)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        SuggestionChip(
          onClick = {},
          label = { Text("Pair", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
          )
        )
      }

      // Comparison row (Current word vs Partner word)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Current Word Column
        PairSideBox(
          word = word,
          isCurrent = true,
          modifier = Modifier.weight(1f)
        )

        // Partner Word Column
        if (partnerWord != null) {
          PairSideBox(
            word = partnerWord,
            isCurrent = false,
            onClick = { onPartnerClick(partnerWord) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }
}

@Composable
private fun PairSideBox(
  word: Word,
  isCurrent: Boolean,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val transitivity = word.transitivity
  val (badgeText, badgeBg, badgeTextCol, particleHint) = when (transitivity) {
    Transitivity.INTRANSITIVE -> Quadruple("自動詞 (Intransitive)", Color(0xFFE0F2F1), Color(0xFF004D40), "N が V (State / Natural)")
    Transitivity.TRANSITIVE -> Quadruple("他動詞 (Transitive)", Color(0xFFFFF3E0), Color(0xFFE65100), "N を V (Action on object)")
    else -> Quadruple("自他動詞", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "")
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isCurrent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh)
      .border(
        width = if (isCurrent) 1.5.dp else 1.dp,
        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable(enabled = onClick != null, onClick = onClick ?: {})
      .padding(12.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      // Badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(badgeBg)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = badgeText,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = badgeTextCol
        )
      }

      // Word surface / ruby
      val ruby = word.rubyMarkup
      if (ruby != null && ruby.contains("{")) {
        FuriganaText(
          text = ruby,
          style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
          rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
      } else {
        Text(
          text = word.headword,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Meaning
      Text(
        text = word.meaningEn ?: "",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2
      )

      // Particle Hint
      if (particleHint.isNotEmpty()) {
        Text(
          text = particleHint,
          fontSize = 10.sp,
          color = badgeTextCol,
          fontWeight = FontWeight.Medium
        )
      }

      if (!isCurrent && onClick != null) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("View", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.width(2.dp))
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
