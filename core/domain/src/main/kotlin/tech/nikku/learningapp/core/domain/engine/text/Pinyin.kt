package tech.nikku.learningapp.core.domain.engine.text

object Pinyin {

  private val VOWEL_DIACRITICS = mapOf(
    'a' to charArrayOf('ā', 'á', 'ǎ', 'à', 'a'),
    'e' to charArrayOf('ē', 'é', 'ě', 'è', 'e'),
    'i' to charArrayOf('ī', 'í', 'ǐ', 'ì', 'i'),
    'o' to charArrayOf('ō', 'ó', 'ǒ', 'ò', 'o'),
    'u' to charArrayOf('ū', 'ú', 'ǔ', 'ù', 'u'),
    'ü' to charArrayOf('ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'),
    'v' to charArrayOf('ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'),
    'A' to charArrayOf('Ā', 'Á', 'Ǎ', 'À', 'A'),
    'E' to charArrayOf('Ē', 'É', 'Ě', 'È', 'E'),
    'I' to charArrayOf('Ī', 'Í', 'Ǐ', 'Ì', 'I'),
    'O' to charArrayOf('Ō', 'Ó', 'Ǒ', 'Ò', 'O'),
    'U' to charArrayOf('Ū', 'Ú', 'Ǔ', 'Ù', 'U'),
    'Ü' to charArrayOf('Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü'),
    'V' to charArrayOf('Ǖ', 'Ǘ', 'Ǚ', 'Ǜ', 'Ü')
  )

  private val DIACRITIC_TO_BASE_AND_TONE = mutableMapOf<Char, Pair<Char, Int>>().apply {
    for ((base, marks) in VOWEL_DIACRITICS) {
      if (base == 'v' || base == 'V') continue
      for (toneIdx in 0..3) {
        put(marks[toneIdx], Pair(base, toneIdx + 1))
      }
    }
  }

  /**
   * Returns the tone number (1..4) or 0 / 5 for neutral tone.
   */
  fun toneOf(syllable: String): Int {
    if (syllable.isEmpty()) return 0
    val lastChar = syllable.last()
    if (lastChar in '1'..'5') {
      val t = lastChar.digitToInt()
      return if (t == 5) 0 else t
    }
    for (ch in syllable) {
      val pair = DIACRITIC_TO_BASE_AND_TONE[ch]
      if (pair != null) return pair.second
    }
    return 0 // Neutral tone
  }

  /**
   * Converts numbered pinyin (e.g. "hao3", "lv4", "xiu1", "gui4", "nv3", "ma5")
   * into diacritic pinyin ("hǎo", "lǜ", "xiū", "guì", "nǚ", "ma").
   */
  fun numberedToDiacritic(text: String): String {
    if (text.isEmpty()) return ""

    val syllableRegex = Regex("""([a-zA-ZüÜvV:]+)([1-5])""")
    return syllableRegex.replace(text) { matchResult ->
      val rawLetters = matchResult.groupValues[1]
      val tone = matchResult.groupValues[2].toInt()
      formatSyllable(rawLetters, tone)
    }
  }

  /**
   * Formats a single syllable with a tone number (1..5, where 5 is neutral).
   */
  fun formatSyllable(rawSyllable: String, tone: Int): String {
    var letters = rawSyllable
      .replace("u:", "ü")
      .replace("U:", "Ü")
      .replace("v", "ü")
      .replace("V", "Ü")

    if (tone !in 1..4) {
      return letters
    }

    val toneIndex = tone - 1
    val lower = letters.lowercase()

    // Tone placement rules:
    // 1. 'a' or 'e' gets the tone
    // 2. 'ou' -> 'o' gets the tone
    // 3. Otherwise, the last vowel gets the tone
    val vowelIndex = when {
      lower.contains('a') -> lower.indexOf('a')
      lower.contains('e') -> lower.indexOf('e')
      lower.contains("ou") -> lower.indexOf('o')
      else -> {
        // Find index of the last vowel in the syllable
        letters.indices.reversed().firstOrNull { idx ->
          lower[idx] in "aeiouü"
        } ?: -1
      }
    }

    if (vowelIndex == -1) return letters

    val targetChar = letters[vowelIndex]
    val replacement = VOWEL_DIACRITICS[targetChar]?.getOrNull(toneIndex)?.toString() ?: targetChar.toString()

    return letters.substring(0, vowelIndex) + replacement + letters.substring(vowelIndex + 1)
  }

  /**
   * Converts diacritic pinyin (e.g. "hǎo", "lǜ", "xiū", "guì")
   * into numbered pinyin ("hao3", "lü4", "xiu1", "gui4").
   */
  fun diacriticToNumbered(text: String): String {
    if (text.isEmpty()) return ""

    val words = text.split(" ")
    return words.joinToString(" ") { word ->
      var detectedTone = 0
      val sb = StringBuilder()
      for (ch in word) {
        val mapping = DIACRITIC_TO_BASE_AND_TONE[ch]
        if (mapping != null) {
          sb.append(mapping.first)
          detectedTone = mapping.second
        } else {
          sb.append(ch)
        }
      }
      if (detectedTone in 1..4) {
        sb.append(detectedTone)
      }
      sb.toString()
    }
  }
}
