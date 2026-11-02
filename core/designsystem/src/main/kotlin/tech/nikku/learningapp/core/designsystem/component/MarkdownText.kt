package tech.nikku.learningapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.nikku.learningapp.core.designsystem.theme.LearningAppTheme

@Composable
fun MarkdownText(
  markdown: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.bodyLarge,
  color: Color = Color.Unspecified
) {
  val lines = markdown.lines()

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    var inCodeBlock = false
    val codeBlockLines = mutableListOf<String>()

    for (line in lines) {
      val trimmed = line.trim()

      if (trimmed.startsWith("```")) {
        if (inCodeBlock) {
          // End of code block
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = codeBlockLines.joinToString("\n"),
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              modifier = Modifier.padding(12.dp)
            )
          }
          codeBlockLines.clear()
          inCodeBlock = false
        } else {
          inCodeBlock = true
        }
        continue
      }

      if (inCodeBlock) {
        codeBlockLines.add(line)
        continue
      }

      when {
        trimmed.isEmpty() -> {
          Spacer(modifier = Modifier.height(4.dp))
        }
        trimmed.startsWith("#### ") -> {
          Text(
            text = parseInlineMarkdown(trimmed.removePrefix("#### ")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        trimmed.startsWith("### ") -> {
          Text(
            text = parseInlineMarkdown(trimmed.removePrefix("### ")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        trimmed.startsWith("## ") -> {
          Text(
            text = parseInlineMarkdown(trimmed.removePrefix("## ")),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        trimmed.startsWith("# ") -> {
          Text(
            text = parseInlineMarkdown(trimmed.removePrefix("# ")),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
          val bulletContent = trimmed.substring(2)
          Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(text = "•", style = style, color = MaterialTheme.colorScheme.primary)
            if (bulletContent.contains('{') && bulletContent.contains('|') && bulletContent.contains('}')) {
              FuriganaText(
                text = bulletContent,
                style = style,
                color = color
              )
            } else {
              Text(
                text = parseInlineMarkdown(bulletContent),
                style = style,
                color = color
              )
            }
          }
        }
        else -> {
          if (trimmed.contains('{') && trimmed.contains('|') && trimmed.contains('}')) {
            FuriganaText(
              text = trimmed,
              style = style,
              color = color
            )
          } else {
            Text(
              text = parseInlineMarkdown(trimmed),
              style = style,
              color = color
            )
          }
        }
      }
    }
  }
}

/**
 * Parses `**bold**`, `*italic*`, and `` `code` `` into an [AnnotatedString].
 */
fun parseInlineMarkdown(text: String): AnnotatedString {
  return buildAnnotatedString {
    var i = 0
    val len = text.length
    while (i < len) {
      when {
        // Bold: **text**
        text.startsWith("**", i) -> {
          val end = text.indexOf("**", i + 2)
          if (end != -1) {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(text.substring(i + 2, end))
            pop()
            i = end + 2
          } else {
            append("**")
            i += 2
          }
        }
        // Inline code: `text`
        text.startsWith("`", i) -> {
          val end = text.indexOf("`", i + 1)
          if (end != -1) {
            pushStyle(
              SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
            )
            append(text.substring(i + 1, end))
            pop()
            i = end + 1
          } else {
            append("`")
            i += 1
          }
        }
        // Italic: *text*
        text.startsWith("*", i) -> {
          val end = text.indexOf("*", i + 1)
          if (end != -1) {
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            append(text.substring(i + 1, end))
            pop()
            i = end + 1
          } else {
            append("*")
            i += 1
          }
        }
        else -> {
          append(text[i])
          i++
        }
      }
    }
  }
}

@Preview(name = "MarkdownText Preview", showBackground = true)
@Composable
fun MarkdownTextPreview() {
  LearningAppTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      MarkdownText(
        markdown = """
          ### 26.1 `～んです` / `～のです`
          The explanatory predicate `～んです` is used when **asking for an explanation** or *giving reason*.
          - {窓|まど}が{閉|し}めて あります
          - Used with plain forms: `行く` → `行くんです`
        """.trimIndent()
      )
    }
  }
}
