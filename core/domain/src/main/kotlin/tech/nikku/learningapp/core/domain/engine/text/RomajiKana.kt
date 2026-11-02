package tech.nikku.learningapp.core.domain.engine.text

object RomajiKana {

  private val THREE_CHAR_MAP = mapOf(
    "kya" to "きゃ", "kyu" to "きゅ", "kyo" to "きょ",
    "gya" to "ぎゃ", "gyu" to "ぎゅ", "gyo" to "ぎょ",
    "sha" to "しゃ", "shu" to "しゅ", "sho" to "しょ", "shi" to "し",
    "sya" to "しゃ", "syu" to "しゅ", "syo" to "しょ",
    "cha" to "ちゃ", "chu" to "ちゅ", "cho" to "ちょ", "chi" to "ち",
    "tya" to "ちゃ", "tyu" to "ちゅ", "tyo" to "ちょ",
    "tsu" to "つ",
    "nya" to "にゃ", "nyu" to "にゅ", "nyo" to "にょ",
    "hya" to "ひゃ", "hyu" to "ひゅ", "hyo" to "ひょ",
    "bya" to "びゃ", "byu" to "びゅ", "byo" to "びょ",
    "pya" to "ぴゃ", "pyu" to "ぴゅ", "pyo" to "ぴょ",
    "mya" to "みゃ", "myu" to "みゅ", "myo" to "みょ",
    "rya" to "りゃ", "ryu" to "りゅ", "ryo" to "りょ",
    "jya" to "じゃ", "jyu" to "じゅ", "jyo" to "じょ"
  )

  private val TWO_CHAR_MAP = mapOf(
    "ka" to "か", "ki" to "き", "ku" to "く", "ke" to "け", "ko" to "こ",
    "sa" to "さ", "si" to "し", "su" to "す", "se" to "せ", "so" to "そ",
    "ta" to "た", "ti" to "ち", "tu" to "つ", "te" to "て", "to" to "と",
    "na" to "な", "ni" to "に", "nu" to "ぬ", "ne" to "ね", "no" to "の",
    "ha" to "は", "hi" to "ひ", "fu" to "ふ", "hu" to "ふ", "he" to "へ", "ho" to "ほ",
    "ma" to "ま", "mi" to "み", "mu" to "む", "me" to "め", "mo" to "も",
    "ya" to "や", "yu" to "ゆ", "yo" to "よ",
    "ra" to "ら", "ri" to "り", "ru" to "る", "re" to "れ", "ro" to "ろ",
    "wa" to "わ", "wo" to "を",
    "ga" to "が", "gi" to "ぎ", "gu" to "ぐ", "ge" to "げ", "go" to "ご",
    "za" to "ざ", "ji" to "じ", "zi" to "じ", "zu" to "ず", "ze" to "ぜ", "zo" to "ぞ",
    "da" to "だ", "di" to "ぢ", "du" to "づ", "de" to "で", "do" to "ど",
    "ba" to "ば", "bi" to "び", "bu" to "ぶ", "be" to "べ", "bo" to "ぼ",
    "pa" to "ぱ", "pi" to "ぴ", "pu" to "ぷ", "pe" to "ぺ", "po" to "ぽ",
    "ja" to "じゃ", "ju" to "じゅ", "jo" to "じょ",
    "nn" to "ん", "n'" to "ん"
  )

  private val ONE_CHAR_MAP = mapOf(
    "a" to "あ", "i" to "い", "u" to "う", "e" to "え", "o" to "お"
  )

  private val KANA_TO_ROMAJI = mapOf(
    "あ" to "a", "い" to "i", "う" to "u", "え" to "e", "お" to "o",
    "か" to "ka", "き" to "ki", "く" to "ku", "け" to "ke", "こ" to "ko",
    "さ" to "sa", "し" to "shi", "す" to "su", "せ" to "se", "そ" to "so",
    "た" to "ta", "ち" to "chi", "つ" to "tsu", "て" to "te", "と" to "to",
    "な" to "na", "に" to "ni", "ぬ" to "nu", "ね" to "ne", "の" to "no",
    "は" to "ha", "ひ" to "hi", "ふ" to "fu", "へ" to "he", "ほ" to "ho",
    "ま" to "ma", "み" to "mi", "む" to "mu", "め" to "me", "も" to "mo",
    "や" to "ya", "ゆ" to "yu", "よ" to "yo",
    "ら" to "ra", "り" to "ri", "る" to "ru", "れ" to "re", "ろ" to "ro",
    "わ" to "wa", "を" to "o", "ん" to "n",
    "が" to "ga", "ぎ" to "gi", "ぐ" to "gu", "げ" to "ge", "ご" to "go",
    "ざ" to "za", "じ" to "ji", "ず" to "zu", "ぜ" to "ze", "ぞ" to "zo",
    "だ" to "da", "ぢ" to "ji", "づ" to "zu", "で" to "de", "ど" to "do",
    "ば" to "ba", "び" to "bi", "ぶ" to "bu", "べ" to "be", "ぼ" to "bo",
    "ぱ" to "pa", "ぴ" to "pi", "ぷ" to "pu", "ぺ" to "pe", "ぽ" to "po",
    "きゃ" to "kya", "きゅ" to "kyu", "きょ" to "kyo",
    "しゃ" to "sha", "しゅ" to "shu", "しょ" to "sho",
    "ちゃ" to "cha", "ちゅ" to "chu", "ちょ" to "cho",
    "にゃ" to "nya", "にゅ" to "nyu", "にょ" to "nyo",
    "ひゃ" to "hya", "ひゅ" to "hyu", "ひょ" to "hyo",
    "みゃ" to "mya", "みゅ" to "myu", "みょ" to "myo",
    "りゃ" to "rya", "りゅ" to "ryu", "りょ" to "ryo",
    "ぎゃ" to "gya", "ぎゅ" to "gyu", "ぎょ" to "gyo",
    "じゃ" to "ja", "じゅ" to "ju", "じょ" to "jo",
    "びゃ" to "bya", "びゅ" to "byu", "びょ" to "byo",
    "ぴゃ" to "pya", "ぴゅ" to "pyu", "ぴょ" to "pyo"
  )

  /**
   * Converts Romaji text (Hepburn, Kunrei, typing input) to Hiragana.
   */
  fun romajiToKana(input: String): String {
    val lower = input.lowercase()
    val sb = StringBuilder()
    var i = 0
    val len = lower.length

    while (i < len) {
      // 1. Sokuon / double consonant check: e.g. "kk", "tt", "ss", "pp", "cc" ("cch")
      if (i + 1 < len && isDoubleConsonant(lower[i], lower[i + 1])) {
        sb.append("っ")
        i++
        continue
      }

      // 2. "m" before "b", "p", "m" -> "ん" (e.g. shimbun -> しんぶん)
      if (lower[i] == 'm' && i + 1 < len && lower[i + 1] in "bpm") {
        sb.append("ん")
        i++
        continue
      }

      // 3. Three-char match
      if (i + 3 <= len) {
        val sub3 = lower.substring(i, i + 3)
        val kana3 = THREE_CHAR_MAP[sub3]
        if (kana3 != null) {
          sb.append(kana3)
          i += 3
          continue
        }
      }

      // 4. Two-char match
      if (i + 2 <= len) {
        val sub2 = lower.substring(i, i + 2)
        val kana2 = TWO_CHAR_MAP[sub2]
        if (kana2 != null) {
          sb.append(kana2)
          i += 2
          continue
        }
      }

      // 5. Single "n" before consonant or end of string
      if (lower[i] == 'n') {
        if (i + 1 == len || lower[i + 1] !in "aiueoy") {
          sb.append("ん")
          i++
          continue
        }
      }

      // 6. One-char match (vowels)
      val sub1 = lower.substring(i, i + 1)
      val kana1 = ONE_CHAR_MAP[sub1]
      if (kana1 != null) {
        sb.append(kana1)
        i++
        continue
      }

      // 7. Fallback: preserve character verbatim (spaces, punctuation, kanji)
      sb.append(lower[i])
      i++
    }

    return sb.toString()
  }

  /**
   * Converts Hiragana string into standard Hepburn Romaji.
   */
  fun kanaToRomaji(kana: String): String {
    val hira = KanaNormalizer.katakanaToHiragana(kana)
    val sb = StringBuilder()
    var i = 0
    val len = hira.length

    while (i < len) {
      if (hira[i] == 'っ' && i + 1 < len) {
        // Look ahead to next syllable and duplicate first roman consonant
        val nextTwo = if (i + 3 <= len) hira.substring(i + 1, i + 3) else ""
        val nextRomaji = KANA_TO_ROMAJI[nextTwo]
          ?: KANA_TO_ROMAJI[hira.substring(i + 1, i + 2)]
          ?: ""
        if (nextRomaji.isNotEmpty()) {
          val consonant = if (nextRomaji.startsWith("ch")) "t" else nextRomaji.take(1)
          sb.append(consonant)
        }
        i++
        continue
      }

      // Try 2-char compound (きゃ, しゃ, etc.)
      if (i + 2 <= len) {
        val sub2 = hira.substring(i, i + 2)
        val rom2 = KANA_TO_ROMAJI[sub2]
        if (rom2 != null) {
          sb.append(rom2)
          i += 2
          continue
        }
      }

      // Single kana
      val sub1 = hira.substring(i, i + 1)
      val rom1 = KANA_TO_ROMAJI[sub1]
      if (rom1 != null) {
        sb.append(rom1)
        i++
        continue
      }

      // Passthrough
      sb.append(hira[i])
      i++
    }

    return sb.toString()
  }

  private fun isDoubleConsonant(c1: Char, c2: Char): Boolean {
    if (c1 == 'n') return false // handled as nn -> ん
    if (c1 == 'c' && c2 == 'h') return false
    if (c1 == 't' && c2 == 'c') return true // "tch" -> っch
    return c1 == c2 && c1 in "bcdfghjklmpqrstvwxyz"
  }
}
