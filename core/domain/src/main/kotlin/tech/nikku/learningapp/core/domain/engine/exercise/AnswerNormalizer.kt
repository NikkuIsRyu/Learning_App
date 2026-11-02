package tech.nikku.learningapp.core.domain.engine.exercise

import java.text.Normalizer
import tech.nikku.learningapp.core.domain.engine.conjugation.Conjugation
import tech.nikku.learningapp.core.domain.engine.text.KanaNormalizer
import tech.nikku.learningapp.core.domain.engine.text.RomajiKana

data class AnswerDiff(
  val commonPrefix: String,
  val userEnding: String,
  val expectedEnding: String
)

data class AnswerResult(
  val isCorrect: Boolean,
  val normalizedInput: String,
  val matchedTarget: String? = null,
  val diff: AnswerDiff? = null,
  val ruleExplanation: String? = null,
  val note: String? = null
)

object AnswerNormalizer {

  /**
   * Normalises user input:
   * 1. NFKC Unicode normalization
   * 2. Trim and remove whitespace (half/full) and common punctuation
   * 3. Katakana to Hiragana
   * 4. Converts Latin/Romaji characters to Hiragana if present
   */
  fun normalize(rawInput: String): String {
    val nfkc = Normalizer.normalize(rawInput, Normalizer.Form.NFKC)
    val stripped = nfkc
      .replace(Regex("[\\s　。、.,!?！？\"']"), "")
    val hiragana = KanaNormalizer.katakanaToHiragana(stripped)
    return if (hiragana.any { it in 'a'..'z' || it in 'A'..'Z' }) {
      RomajiKana.romajiToKana(hiragana)
    } else {
      hiragana
    }
  }

  /**
   * Evaluates user input against a Conjugation target.
   * Compares against {surface, reading} ∪ acceptedOverrides.
   * When incorrect, computes a prefix/suffix diff against expected.
   */
  fun evaluate(
    rawInput: String,
    expected: Conjugation
  ): AnswerResult {
    val normalizedInput = normalize(rawInput)
    val normSurface = normalize(expected.surface)
    val normReading = normalize(expected.reading)

    // Check exact match with surface or reading
    if (normalizedInput == normSurface || normalizedInput == normReading) {
      return AnswerResult(
        isCorrect = true,
        normalizedInput = normalizedInput,
        matchedTarget = if (normalizedInput == normSurface) expected.surface else expected.reading,
        ruleExplanation = expected.ruleExplanation,
        note = expected.notes.firstOrNull()
      )
    }

    // Check accepted overrides (e.g. colloquial forms like ら抜き)
    for (i in expected.acceptedOverrides.indices) {
      val override = expected.acceptedOverrides[i]
      if (normalizedInput == normalize(override)) {
        val note = expected.notes.getOrNull(i)
          ?: expected.notes.firstOrNull()
          ?: "Accepted variant"
        return AnswerResult(
          isCorrect = true,
          normalizedInput = normalizedInput,
          matchedTarget = override,
          ruleExplanation = expected.ruleExplanation,
          note = note
        )
      }
    }

    // Incorrect answer: determine whether to diff against surface or reading
    val inputHasKanji = normalizedInput.any { it in '\u4E00'..'\u9FFF' }
    val targetForDiff = if (inputHasKanji) normSurface else normReading

    val diff = computeDiff(normalizedInput, targetForDiff)

    val displayExpected = if (expected.surface != expected.reading) {
      "${expected.reading} (${expected.surface})"
    } else {
      expected.reading
    }

    val ruleText = "Expected \"$displayExpected\": ${expected.ruleExplanation}"

    return AnswerResult(
      isCorrect = false,
      normalizedInput = normalizedInput,
      diff = diff,
      ruleExplanation = ruleText,
      note = expected.notes.firstOrNull()
    )
  }

  fun computeDiff(actual: String, expected: String): AnswerDiff {
    var prefixLen = 0
    val minLen = minOf(actual.length, expected.length)
    while (prefixLen < minLen && actual[prefixLen] == expected[prefixLen]) {
      prefixLen++
    }
    return AnswerDiff(
      commonPrefix = actual.substring(0, prefixLen),
      userEnding = actual.substring(prefixLen),
      expectedEnding = expected.substring(prefixLen)
    )
  }
}
