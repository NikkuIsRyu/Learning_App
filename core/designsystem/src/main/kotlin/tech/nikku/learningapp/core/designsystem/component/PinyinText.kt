package tech.nikku.learningapp.core.designsystem.component

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.theme.AppDomain
import tech.nikku.learningapp.core.designsystem.theme.ChineseLocaleList
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme
import tech.nikku.learningapp.core.designsystem.theme.toneColor
import tech.nikku.learningapp.core.domain.engine.text.Pinyin

data class PinyinChar(
  val char: String,
  val pinyin: String,
  val tone: Int = Pinyin.toneOf(pinyin)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinyinText(
  characters: List<PinyinChar>,
  modifier: Modifier = Modifier,
  showToneColors: Boolean = true,
  pinyinVisible: Boolean = true,
  charStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
  pinyinStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
  darkTheme: Boolean = isSystemInDarkTheme()
) {
  val resolvedCharStyle = remember(charStyle) {
    charStyle.copy(localeList = ChineseLocaleList)
  }
  val resolvedPinyinStyle = remember(pinyinStyle) {
    pinyinStyle.copy(localeList = ChineseLocaleList)
  }

  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    for (item in characters) {
      val itemColor = if (showToneColors) {
        toneColor(item.tone, darkTheme)
      } else {
        MaterialTheme.colorScheme.onSurface
      }

      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
      ) {
        if (pinyinVisible) {
          Text(
            text = Pinyin.numberedToDiacritic(item.pinyin),
            style = resolvedPinyinStyle,
            color = itemColor,
            textAlign = TextAlign.Center
          )
        }
        Text(
          text = item.char,
          style = resolvedCharStyle,
          color = itemColor,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Preview(name = "Pinyin Tone Colors Light", showBackground = true)
@Composable
fun PinyinTextPreview() {
  val sample = listOf(
    PinyinChar("你", "ni3", 3),
    PinyinChar("好", "hao3", 3),
    PinyinChar("，", "", 0),
    PinyinChar("欢", "huan1", 1),
    PinyinChar("迎", "ying2", 2),
    PinyinChar("来", "lai2", 2),
    PinyinChar("到", "dao4", 4),
    PinyinChar("北", "bei3", 3),
    PinyinChar("京", "jing1", 1),
    PinyinChar("！", "", 0)
  )

  LearningAppTheme(domain = AppDomain.CHINESE, darkTheme = false) {
    Surface(modifier = Modifier.padding(16.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          text = "Tone Colors ON",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
        PinyinText(characters = sample, showToneColors = true)

        Text(
          text = "Tone Colors OFF (Monochrome)",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
        PinyinText(characters = sample, showToneColors = false)
      }
    }
  }
}

@Preview(name = "Pinyin Tone Colors Dark", showBackground = true)
@Composable
fun PinyinTextDarkPreview() {
  val sample = listOf(
    PinyinChar("中", "zhong1", 1),
    PinyinChar("文", "wen2", 2),
    PinyinChar("学", "xue2", 2),
    PinyinChar("习", "xi2", 2)
  )

  LearningAppTheme(domain = AppDomain.CHINESE, darkTheme = true) {
    Surface(modifier = Modifier.padding(16.dp)) {
      PinyinText(characters = sample, showToneColors = true, darkTheme = true)
    }
  }
}
