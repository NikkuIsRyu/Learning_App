package tech.nikku.learningapp.feature.shared.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeedSlider(
  speed: Float,
  onSpeedSelected: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f)

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Text(
      text = "Speed: ${"%.2f".format(speed).trimEnd('0').trimEnd('.')}×",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.SemiBold
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      speedPresets.forEach { preset ->
        val isSelected = kotlin.math.abs(speed - preset) < 0.05f
        FilterChip(
          selected = isSelected,
          onClick = { onSpeedSelected(preset) },
          label = {
            Text(
              text = "${preset}×",
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
