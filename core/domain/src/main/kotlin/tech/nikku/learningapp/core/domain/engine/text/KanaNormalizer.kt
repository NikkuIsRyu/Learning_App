package tech.nikku.learningapp.core.domain.engine.text

import java.text.Normalizer

object KanaNormalizer {

  /**
   * Converts full-width Katakana characters (U+30A1..U+30F6) to Hiragana (U+3041..U+3096).
   */
  fun katakanaToHiragana(s: String): String {
    val sb = StringBuilder(s.length)
    var prevChar: Char? = null

    for (c in s) {
      when {
        // Katakana range
        c in '\u30A1'..'\u30F6' -> {
          val hira = (c.code - 0x60).toChar()
          sb.append(hira)
          prevChar = hira
        }
        // Long vowel mark ー
        c == 'ー' -> {
          val vowel = resolveLongVowel(prevChar)
          sb.append(vowel)
          prevChar = vowel
        }
        else -> {
          sb.append(c)
          prevChar = c
        }
      }
    }
    return sb.toString()
  }

  /**
   * Converts Hiragana characters (U+3041..U+3096) to Katakana (U+30A1..U+30F6).
   */
  fun hiraganaToKatakana(s: String): String {
    val sb = StringBuilder(s.length)
    for (c in s) {
      if (c in '\u3041'..'\u3096') {
        sb.append((c.code + 0x60).toChar())
      } else {
        sb.append(c)
      }
    }
    return sb.toString()
  }

  /**
   * Comprehensive cleanup: NFKC normalisation, trims, strips punctuation, katakana to hiragana.
   */
  fun normalize(input: String): String {
    val nfkc = Normalizer.normalize(input, Normalizer.Form.NFKC)
    val stripped = nfkc
      .replace(Regex("[\\s　。、.,!?！？\"']"), "")
    return katakanaToHiragana(stripped)
  }

  private fun resolveLongVowel(prev: Char?): Char {
    if (prev == null) return 'ー'
    return when (prev) {
      'あ', 'か', 'さ', 'た', 'な', 'は', 'ま', 'や', 'ら', 'わ', 'が', 'ざ', 'だ', 'ば', 'ぱ', 'ぁ', 'ゃ' -> 'あ'
      'い', 'き', 'し', 'ち', 'に', 'ひ', 'み', 'り', 'ぎ', 'じ', 'ぢ', 'び', 'ぴ', 'ぃ' -> 'い'
      'う', 'く', 'す', 'つ', 'ぬ', 'ふ', 'む', 'ゆ', 'る', 'ぐ', 'ず', 'づ', 'ぶ', 'ぷ', 'ぅ', 'ゅ' -> 'う'
      'え', 'け', 'せ', 'て', 'ね', 'へ', 'め', 'れ', 'げ', 'ぜ', 'で', 'べ', 'ぺ', 'ぇ' -> 'え'
      'お', 'こ', 'そ', 'と', 'の', 'ほ', 'も', 'よ', 'ろ', 'ご', 'ぞ', 'ど', 'ぼ', 'ぽ', 'ぉ', 'ょ' -> 'う'
      else -> 'ー'
    }
  }
}
