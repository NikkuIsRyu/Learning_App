package tech.nikku.learningapp.feature.japanese.conjugation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.VerbForm

@Composable
fun MatrixCell(
  form: VerbForm,
  state: SrsState?,
  intervalDays: Double,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (bgColor, borderColor, textColor) = when (state) {
    SrsState.REVIEW -> Triple(
      Color(0xFFE8F5E9),
      Color(0xFF4CAF50),
      Color(0xFF1B5E20)
    )
    SrsState.LEARNING, SrsState.RELEARNING -> Triple(
      Color(0xFFFFF8E1),
      Color(0xFFFFB300),
      Color(0xFFF57F17)
    )
    SrsState.NEW -> Triple(
      Color(0xFFF5F5F5),
      Color(0xFFBDBDBD),
      Color(0xFF757575)
    )
    SrsState.SUSPENDED, null -> Triple(
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      MaterialTheme.colorScheme.outlineVariant,
      MaterialTheme.colorScheme.onSurfaceVariant
    )
  }

  val label = when (state) {
    SrsState.REVIEW -> if (intervalDays >= 1.0) "${intervalDays.toInt()}d" else "rev"
    SrsState.LEARNING, SrsState.RELEARNING -> "lrn"
    SrsState.NEW -> "new"
    SrsState.SUSPENDED -> "susp"
    null -> "—"
  }

  val description = "${form.name}: " + (state?.name ?: "untested")

  Box(
    modifier = modifier
      .size(width = 54.dp, height = 36.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .semantics { contentDescription = description }
      .padding(2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
