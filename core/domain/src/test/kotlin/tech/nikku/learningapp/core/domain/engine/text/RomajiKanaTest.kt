package tech.nikku.learningapp.core.domain.engine.text

import org.junit.Assert.assertEquals
import org.junit.Test

class RomajiKanaTest {

  @Test
  fun testRomajiToKanaBasicAndSokuon() {
    assertEquals("かける", RomajiKana.romajiToKana("kakeru"))
    assertEquals("きって", RomajiKana.romajiToKana("kitte"))
  }

  @Test
  fun testRomajiToKanaNVariations() {
    // Apostrophe n'ya
    assertEquals("こんや", RomajiKana.romajiToKana("kon'ya"))
    // Double nn
    assertEquals("こんや", RomajiKana.romajiToKana("konnya"))
    // Single n before consonant
    assertEquals("しんぶん", RomajiKana.romajiToKana("shinbun"))
    // m before b/p
    assertEquals("しんぶん", RomajiKana.romajiToKana("shimbun"))
    // n at end of word
    assertEquals("にほん", RomajiKana.romajiToKana("nihon"))
  }

  @Test
  fun testRomajiToKanaDigraphsAndPalatalized() {
    assertEquals("とうきょう", RomajiKana.romajiToKana("toukyou"))
    assertEquals("しゃしん", RomajiKana.romajiToKana("shashin"))
    assertEquals("おちゃ", RomajiKana.romajiToKana("ocha"))
    assertEquals("ちょっと", RomajiKana.romajiToKana("chotto"))
    assertEquals("じゅぎょう", RomajiKana.romajiToKana("jugyou"))
  }

  @Test
  fun testKanaToRomajiHepburn() {
    assertEquals("kakeru", RomajiKana.kanaToRomaji("かける"))
    assertEquals("kitte", RomajiKana.kanaToRomaji("きって"))
    assertEquals("shinbun", RomajiKana.kanaToRomaji("しんぶん"))
    assertEquals("tokyo", RomajiKana.kanaToRomaji("ときょ"))
    assertEquals("chotto", RomajiKana.kanaToRomaji("ちょっと"))
    assertEquals("shashin", RomajiKana.kanaToRomaji("しゃしん"))
  }
}
