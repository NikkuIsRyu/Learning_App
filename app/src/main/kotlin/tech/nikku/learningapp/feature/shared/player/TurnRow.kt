package tech.nikku.learningapp.feature.shared.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.component.FuriganaText
import tech.nikku.learningapp.core.domain.model.DialogueTurn
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.SpeakerInfo

enum class DisplayMode(val label: String) {
  FURIGANA("Furigana"),
  KANJI_ONLY("Kanji"),
  KANA_ONLY("Kana"),
  ROMAJI("Romaji"),
  HIDDEN("Shadowing")
}

@Composable
fun TurnRow(
  turn: DialogueTurn,
  isActive: Boolean,
  displayMode: DisplayMode,
  speakerInfo: SpeakerInfo?,
  onTurnClick: () -> Unit,
  modifier: Modifier = Modifier,
  domain: Domain = Domain.JAPANESE
) {
  val containerColor = if (isActive) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
  } else {
    MaterialTheme.colorScheme.surface
  }

  val borderColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
  }

  val defaultSpeakerColor = MaterialTheme.colorScheme.primary
  val parsedInt = runCatching { speakerInfo?.colorHex?.let { android.graphics.Color.parseColor(it) } }.getOrNull()
  val speakerColor = if (parsedInt != null) Color(parsedInt) else defaultSpeakerColor

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onTurnClick),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isActive) 1.5.dp else 1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Speaker Avatar / Badge
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(speakerColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = turn.speaker.take(1),
          fontWeight = FontWeight.Bold,
          color = speakerColor,
          fontSize = 15.sp
        )
      }

      // Turn Content Column
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = speakerInfo?.label ?: turn.speaker,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = speakerColor
          )
          if (turn.startMs != null && turn.endMs != null) {
            val durationSec = (turn.endMs!! - turn.startMs!!) / 1000.0
            Text(
              text = "• ${"%.1f".format(durationSec)}s",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Script content according to DisplayMode
        when (displayMode) {
          DisplayMode.HIDDEN -> {
            Text(
              text = "• • • • • • •",
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.outline
            )
          }
          DisplayMode.FURIGANA -> {
            val ruby = turn.rubyMarkup
            if (ruby != null && (ruby.contains("{") || ruby.contains("["))) {
              FuriganaText(
                text = ruby,
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontSize = 18.sp,
                  fontWeight = FontWeight.SemiBold,
                  localeList = LocaleList(if (domain == Domain.CHINESE) "zh-CN" else "ja-JP")
                ),
                rubyStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
              )
            } else {
              Text(
                text = turn.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
          DisplayMode.KANJI_ONLY -> {
            Text(
              text = turn.text,
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          DisplayMode.KANA_ONLY -> {
            // Strip kanji markup
            val plain = (turn.rubyMarkup ?: turn.text)
              .replace(Regex("""\{[^|]+\|([^}]+)\}"""), "$1")
              .replace(Regex("""\[[^\]]+\]"""), "")
            Text(
              text = plain,
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
          DisplayMode.ROMAJI -> {
            Text(
              text = turn.romanization ?: turn.text,
              fontSize = 16.sp,
              fontWeight = FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // English Translation
        val en = turn.translationEn
        if (en != null && displayMode != DisplayMode.HIDDEN) {
          Text(
            text = en,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // German Translation
        val de = turn.translationDe
        if (de != null && displayMode != DisplayMode.HIDDEN) {
          Text(
            text = de,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
        }
      }

      // Quick replay button for turn
      IconButton(
        onClick = onTurnClick,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          Icons.Default.PlayArrow,
          contentDescription = "Replay turn",
          tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
