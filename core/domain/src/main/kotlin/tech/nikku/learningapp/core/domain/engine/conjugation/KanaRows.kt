package tech.nikku.learningapp.core.domain.engine.conjugation

object KanaRows {

  // a, i, u, e, o
  private val GODAN_ROWS: Map<Char, List<Char>> = mapOf(
    'う' to listOf('わ', 'い', 'う', 'え', 'お'),
    'く' to listOf('か', 'き', 'く', 'け', 'こ'),
    'ぐ' to listOf('が', 'ぎ', 'ぐ', 'げ', 'ご'),
    'す' to listOf('さ', 'し', 'す', 'せ', 'そ'),
    'つ' to listOf('た', 'ち', 'つ', 'て', 'と'),
    'ぬ' to listOf('な', 'に', 'ぬ', 'ね', 'の'),
    'ぶ' to listOf('ば', 'び', 'ぶ', 'べ', 'ぼ'),
    'む' to listOf('ま', 'み', 'む', 'め', 'も'),
    'る' to listOf('ら', 'り', 'る', 'れ', 'ろ')
  )

  fun toARow(lastKana: Char): Char =
    GODAN_ROWS[lastKana]?.get(0) ?: throw IllegalArgumentException("Unknown Godan final kana: $lastKana")

  fun toIRow(lastKana: Char): Char =
    GODAN_ROWS[lastKana]?.get(1) ?: throw IllegalArgumentException("Unknown Godan final kana: $lastKana")

  fun toURow(lastKana: Char): Char =
    GODAN_ROWS[lastKana]?.get(2) ?: lastKana

  fun toERow(lastKana: Char): Char =
    GODAN_ROWS[lastKana]?.get(3) ?: throw IllegalArgumentException("Unknown Godan final kana: $lastKana")

  fun toORow(lastKana: Char): Char =
    GODAN_ROWS[lastKana]?.get(4) ?: throw IllegalArgumentException("Unknown Godan final kana: $lastKana")

  /**
   * Returns Te/Ta form suffix for Godan verbs.
   */
  fun teTaSuffix(lastKana: Char, isTa: Boolean): String {
    return when (lastKana) {
      'う', 'つ', 'る' -> if (isTa) "った" else "って"
      'む', 'ぶ', 'ぬ' -> if (isTa) "んだ" else "んで"
      'く' -> if (isTa) "いた" else "いて"
      'ぐ' -> if (isTa) "いだ" else "いで"
      'す' -> if (isTa) "した" else "して"
      else -> throw IllegalArgumentException("Unknown Godan final kana for te/ta: $lastKana")
    }
  }
}
