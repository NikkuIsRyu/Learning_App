package tech.nikku.learningapp.feature.japanese.lexicon.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup

@Composable
fun FilterChipsRow(
  selectedChapters: Set<Int>,
  selectedPos: PartOfSpeech?,
  selectedVerbGroup: VerbGroup?,
  selectedTransitivity: Transitivity?,
  selectedJlpt: JlptLevel?,
  verifiedOnly: Boolean,
  onToggleChapter: (Int) -> Unit,
  onSelectPos: (PartOfSpeech?) -> Unit,
  onSelectVerbGroup: (VerbGroup?) -> Unit,
  onSelectTransitivity: (Transitivity?) -> Unit,
  onSelectJlpt: (JlptLevel?) -> Unit,
  onToggleVerifiedOnly: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Row 1: Chapters + Verified
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChip(
        selected = verifiedOnly,
        onClick = onToggleVerifiedOnly,
        label = { Text("Verified Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )

      (26..32).forEach { ch ->
        val isSelected = selectedChapters.contains(ch)
        FilterChip(
          selected = isSelected,
          onClick = { onToggleChapter(ch) },
          label = { Text("Ch $ch", fontSize = 11.sp) }
        )
      }
    }

    // Row 2: Part of Speech, Transitivity, Verb Groups, JLPT
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Transitivity chips
      listOf(
        Transitivity.INTRANSITIVE to "自動詞",
        Transitivity.TRANSITIVE to "他動詞"
      ).forEach { (t, label) ->
        val isSelected = selectedTransitivity == t
        FilterChip(
          selected = isSelected,
          onClick = { onSelectTransitivity(if (isSelected) null else t) },
          label = { Text(label, fontSize = 11.sp) }
        )
      }

      // Verb Group chips
      listOf(
        VerbGroup.GODAN to "五段 (I)",
        VerbGroup.ICHIDAN to "一段 (II)",
        VerbGroup.SURU to "する",
        VerbGroup.KURU to "来る"
      ).forEach { (vg, label) ->
        val isSelected = selectedVerbGroup == vg
        FilterChip(
          selected = isSelected,
          onClick = { onSelectVerbGroup(if (isSelected) null else vg) },
          label = { Text(label, fontSize = 11.sp) }
        )
      }

      // JLPT chips
      JlptLevel.entries.forEach { level ->
        val isSelected = selectedJlpt == level
        FilterChip(
          selected = isSelected,
          onClick = { onSelectJlpt(if (isSelected) null else level) },
          label = { Text(level.name, fontSize = 11.sp) }
        )
      }
    }
  }
}
