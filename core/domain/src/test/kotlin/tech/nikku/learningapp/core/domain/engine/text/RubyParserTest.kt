package tech.nikku.learningapp.core.domain.engine.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RubyParserTest {

  private val samples = listOf(
    "{窓|まど}が{閉|し}めて あります",
    "明日、{京都|きょうと}へ{行|い}きます。",
    "{私|わたし}は{田中|たなか}です。",
    "これは{本|ほん}です。",
    "Plain text without any furigana markup.",
    "",
    "{食|た}べます",
    "{開|あ}けます",
    "{一|いち}{二|に}{三|さん}",
    "{食|た}べ{過|す}ぎました",
    "{日|に}{本|ほん}{語|ご}を{勉|べん}{強|きょう}しています",
    "{先|せん}{生|せい}、こんにちは。",
    "{車|くるま}を{買|か}いたいです。",
    "{雨|あめ}が{降|ふ}っています。",
    "{新|しん}{幹|かん}{線|せん}で{行|い}きます。",
    "テスト{文|ぶん}",
    "123 {数|すう}{字|じ} 456",
    "{友|とも}{達|だち}と{遊|あそ}びました。",
    "{電|でん}{気|き}を{消|け}してください。",
    "{時|じ}{計|けい}が{止|と}まっています。",
    "{鞄|かばん}の{中|なか}に{鍵|かぎ}があります。"
  )

  @Test
  fun roundTrip_renderMatchesParseOnAllSamples() {
    assertTrue(samples.size >= 20)
    for (sample in samples) {
      val parsed = RubyParser.parse(sample)
      val rendered = RubyParser.render(parsed)
      assertEquals("Round trip failed for: $sample", sample, rendered)
    }
  }

  @Test
  fun toKanjiOnly_stripsReadings() {
    val markup = "{窓|まど}が{閉|し}めて あります"
    val result = RubyParser.toKanjiOnly(markup)
    assertEquals("窓が閉めて あります", result)
  }

  @Test
  fun toKanaOnly_stripsKanjiBases() {
    val markup = "{窓|まど}が{閉|し}めて あります"
    val result = RubyParser.toKanaOnly(markup)
    assertEquals("まどがしめて あります", result)
  }

  @Test
  fun fromAnkiFormat_convertsCorrectly() {
    val anki1 = " 開[あ]きます"
    assertEquals("{開|あ}きます", RubyParser.fromAnkiFormat(anki1).trim())

    val anki2 = " 窓[まど]が 開[あ]きます"
    val converted = RubyParser.fromAnkiFormat(anki2)
    assertEquals("{窓|まど}が {開|あ}きます", converted.trim())

    val anki3 = "東京[とうきょう]タワー"
    assertEquals("{東京|とうきょう}タワー", RubyParser.fromAnkiFormat(anki3))
  }

  @Test
  fun emptyInput_returnsEmptyList() {
    val parsed = RubyParser.parse("")
    assertTrue(parsed.isEmpty())
    assertEquals("", RubyParser.render(parsed))
  }
}
