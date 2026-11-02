package tech.nikku.learningapp.core.domain.engine.text

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface RubySegment {
  @Serializable
  @SerialName("plain")
  data class Plain(val text: String) : RubySegment

  @Serializable
  @SerialName("ruby")
  data class Ruby(val base: String, val reading: String) : RubySegment
}

object RubyParser {
  private val CANONICAL_REGEX = Regex("""\{([^|{}]+)\|([^{}]+)\}""")
  private val ANKI_REGEX = Regex("""(?:(?<=\s)|^)([\u4e00-\u9faf\u3400-\u4dbfヶ々]+)\[([^\]\s]+)\]|([\u4e00-\u9faf\u3400-\u4dbfヶ々]+)\[([^\]\s]+)\]""")

  fun parse(markup: String): List<RubySegment> {
    if (markup.isEmpty()) return emptyList()

    val segments = mutableListOf<RubySegment>()
    var lastIndex = 0

    for (match in CANONICAL_REGEX.findAll(markup)) {
      if (match.range.first > lastIndex) {
        val plainText = markup.substring(lastIndex, match.range.first)
        segments.add(RubySegment.Plain(plainText))
      }
      val base = match.groupValues[1]
      val reading = match.groupValues[2]
      segments.add(RubySegment.Ruby(base = base, reading = reading))
      lastIndex = match.range.last + 1
    }

    if (lastIndex < markup.length) {
      val remaining = markup.substring(lastIndex)
      segments.add(RubySegment.Plain(remaining))
    }

    return segments
  }

  fun render(segments: List<RubySegment>): String {
    return buildString {
      for (segment in segments) {
        when (segment) {
          is RubySegment.Plain -> append(segment.text)
          is RubySegment.Ruby -> {
            append('{')
            append(segment.base)
            append('|')
            append(segment.reading)
            append('}')
          }
        }
      }
    }
  }

  fun toKanjiOnly(markup: String): String = toKanjiOnly(parse(markup))

  fun toKanjiOnly(segments: List<RubySegment>): String {
    return buildString {
      for (segment in segments) {
        when (segment) {
          is RubySegment.Plain -> append(segment.text)
          is RubySegment.Ruby -> append(segment.base)
        }
      }
    }
  }

  fun toKanaOnly(markup: String): String = toKanaOnly(parse(markup))

  fun toKanaOnly(segments: List<RubySegment>): String {
    return buildString {
      for (segment in segments) {
        when (segment) {
          is RubySegment.Plain -> append(segment.text)
          is RubySegment.Ruby -> append(segment.reading)
        }
      }
    }
  }

  /**
   * Converts Anki-style furigana (e.g. " 窓[まど]が 開[あ]きます" or "漢字[かんじ]")
   * into canonical format ("{窓|まど}が {開|あ}きます").
   */
  fun fromAnkiFormat(ankiText: String): String {
    if (ankiText.isEmpty()) return ""
    // Handles forms: " 漢字[かんじ]" and inline "漢字[かんじ]"
    val result = StringBuilder()
    var lastIndex = 0

    // Match leading space if present before kanji
    val pattern = Regex("""([\u4e00-\u9faf\u3400-\u4dbfヶ々]+)\[([^\]\r\n]+)\]""")
    for (match in pattern.findAll(ankiText)) {
      if (match.range.first > lastIndex) {
        result.append(ankiText.substring(lastIndex, match.range.first))
      }
      val kanji = match.groupValues[1]
      val reading = match.groupValues[2]
      result.append('{').append(kanji).append('|').append(reading).append('}')
      lastIndex = match.range.last + 1
    }

    if (lastIndex < ankiText.length) {
      result.append(ankiText.substring(lastIndex))
    }

    return result.toString()
  }
}
