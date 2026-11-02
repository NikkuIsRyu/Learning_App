package tech.nikku.learningapp.core.domain.engine.text

import org.junit.Assert.assertEquals
import org.junit.Test

class PinyinTest {

  @Test
  fun numberedToDiacritic_exactSamplesFromSpec() {
    // "hao3→hǎo, lv4→lǜ, xiu1→xiū, gui4→guì, nv3→nǚ, neutral ma5→ma"
    assertEquals("hǎo", Pinyin.numberedToDiacritic("hao3"))
    assertEquals("lǜ", Pinyin.numberedToDiacritic("lv4"))
    assertEquals("xiū", Pinyin.numberedToDiacritic("xiu1"))
    assertEquals("guì", Pinyin.numberedToDiacritic("gui4"))
    assertEquals("nǚ", Pinyin.numberedToDiacritic("nv3"))
    assertEquals("ma", Pinyin.numberedToDiacritic("ma5"))
  }

  @Test
  fun toneMarkPlacement_specialRules() {
    // a/e rule
    assertEquals("bài", Pinyin.numberedToDiacritic("bai4"))
    assertEquals("lèi", Pinyin.numberedToDiacritic("lei4"))
    // ou rule -> o gets mark
    assertEquals("dōu", Pinyin.numberedToDiacritic("dou1"))
    assertEquals("tóu", Pinyin.numberedToDiacritic("tou2"))
    // last vowel rule
    assertEquals("huì", Pinyin.numberedToDiacritic("hui4"))
    assertEquals("jiǔ", Pinyin.numberedToDiacritic("jiu3"))
    assertEquals("qiú", Pinyin.numberedToDiacritic("qiu2"))
  }

  @Test
  fun toneOf_detectsCorrectTone() {
    assertEquals(3, Pinyin.toneOf("hǎo"))
    assertEquals(4, Pinyin.toneOf("lǜ"))
    assertEquals(1, Pinyin.toneOf("xiū"))
    assertEquals(4, Pinyin.toneOf("guì"))
    assertEquals(3, Pinyin.toneOf("nǚ"))
    assertEquals(0, Pinyin.toneOf("ma"))
    assertEquals(3, Pinyin.toneOf("hao3"))
    assertEquals(0, Pinyin.toneOf("ma5"))
  }

  @Test
  fun sentenceConversion_numberedToDiacritic() {
    val input = "ni3 hao3 ma5"
    val expected = "nǐ hǎo ma"
    assertEquals(expected, Pinyin.numberedToDiacritic(input))
  }
}
