package tech.nikku.learningapp.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.JapaneseLocaleList
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.domain.engine.text.RubyParser
import tech.nikku.learningapp.core.domain.engine.text.RubySegment

enum class FuriganaMode {
  FURIGANA,
  KANJI_ONLY,
  KANA_ONLY
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FuriganaText(
  text: String,
  modifier: Modifier = Modifier,
  mode: FuriganaMode = FuriganaMode.FURIGANA,
  style: TextStyle = MaterialTheme.typography.bodyLarge,
  rubyStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
  color: Color = Color.Unspecified,
  rubyColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
  val segments = remember(text) { RubyParser.parse(text) }

  val baseTextStyle = remember(style) {
    style.copy(localeList = JapaneseLocaleList)
  }
  val resolvedRubyStyle = remember(rubyStyle) {
    rubyStyle.copy(localeList = JapaneseLocaleList)
  }

  when (mode) {
    FuriganaMode.KANJI_ONLY -> {
      Text(
        text = RubyParser.toKanjiOnly(segments),
        style = baseTextStyle,
        color = color,
        modifier = modifier
      )
    }
    FuriganaMode.KANA_ONLY -> {
      Text(
        text = RubyParser.toKanaOnly(segments),
        style = baseTextStyle,
        color = color,
        modifier = modifier
      )
    }
    FuriganaMode.FURIGANA -> {
      FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Bottom
      ) {
        for (segment in segments) {
          when (segment) {
            is RubySegment.Plain -> {
              Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                // Invisible placeholder to align baseline with ruby items
                Text(
                  text = " ",
                  style = resolvedRubyStyle,
                  color = Color.Transparent
                )
                Text(
                  text = segment.text,
                  style = baseTextStyle,
                  color = color
                )
              }
            }
            is RubySegment.Ruby -> {
              Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
              ) {
                Text(
                  text = segment.reading,
                  style = resolvedRubyStyle,
                  color = rubyColor,
                  textAlign = TextAlign.Center
                )
                Text(
                  text = segment.base,
                  style = baseTextStyle,
                  color = color,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }
    }
  }
}

@Preview(name = "Furigana Modes", showBackground = true)
@Composable
fun FuriganaTextPreview() {
  LearningAppTheme(domain = AppDomain.JAPANESE) {
    Surface(modifier = Modifier.padding(16.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = "Mode: FURIGANA",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
        FuriganaText(text = "{窓|まど}が{閉|し}めて あります。{明日|あした}、{東京|とうきょう}へ{行|い}きます。")

        Text(
          text = "Mode: KANJI_ONLY",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
        FuriganaText(
          text = "{窓|まど}が{閉|し}めて あります。{明日|あした}、{東京|とうきょう}へ{行|い}きます。",
          mode = FuriganaMode.KANJI_ONLY
        )

        Text(
          text = "Mode: KANA_ONLY",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
        FuriganaText(
          text = "{窓|まど}が{閉|し}めて あります。{明日|あした}、{東京|とうきょう}へ{行|い}きます。",
          mode = FuriganaMode.KANA_ONLY
        )
      }
    }
  }
}
