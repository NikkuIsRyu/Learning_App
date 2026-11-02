package tech.nikku.learningapp.core.domain.engine.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.nikku.learningapp.core.domain.engine.conjugation.Conjugator
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup

class AnswerNormalizerTest {

  @Test
  fun testNormalizationStripsWhitespaceAndPunctuation() {
    val norm = AnswerNormalizer.normalize("　たべる。 ")
    assertEquals("たべる", norm)

    val norm2 = AnswerNormalizer.normalize("タベル!")
    assertEquals("たべる", norm2)
  }

  @Test
  fun testNormalizationKatakanaToHiragana() {
    val norm = AnswerNormalizer.normalize("タベラレル")
    assertEquals("たべられる", norm)
  }

  @Test
  fun testNormalizationRomajiConversion() {
    val norm = AnswerNormalizer.normalize("kakeru")
    assertEquals("かける", norm)
  }

  @Test
  fun testExactMatchSurfaceAndReading() {
    val target = Conjugator.conjugate("書く", "かく", VerbGroup.GODAN, VerbForm.TA) // 書いた / かいた

    val evalKanji = AnswerNormalizer.evaluate("書いた", target)
    assertTrue("Should match kanji surface", evalKanji.isCorrect)
    assertEquals("書いた", evalKanji.matchedTarget)

    val evalKana = AnswerNormalizer.evaluate("かいた", target)
    assertTrue("Should match kana reading", evalKana.isCorrect)
    assertEquals("かいた", evalKana.matchedTarget)

    val evalWithSpaces = AnswerNormalizer.evaluate("　かいた。 ", target)
    assertTrue("Should match with spaces and punctuation", evalWithSpaces.isCorrect)
  }

  @Test
  fun testRaNukiAcceptedWithNote() {
    val target = Conjugator.conjugate("食べる", "たべる", VerbGroup.ICHIDAN, VerbForm.POTENTIAL)
    // target.surface = "食べられる", target.reading = "たべられる"
    // target.acceptedOverrides contains "食べれる", "たべれる"

    val evalKanji = AnswerNormalizer.evaluate("食べれる", target)
    assertTrue("Ra-nuki kanji should be accepted as correct", evalKanji.isCorrect)
    assertNotNull(evalKanji.note)
    assertTrue("Note should mention ra-nuki", evalKanji.note!!.contains("ら抜き"))

    val evalKana = AnswerNormalizer.evaluate("たべれる", target)
    assertTrue("Ra-nuki kana should be accepted as correct", evalKana.isCorrect)
    assertNotNull(evalKana.note)
  }

  @Test
  fun testShortCausativePassiveAccepted() {
    val target = Conjugator.conjugate("書く", "かく", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE)
    // standard: 書かせられる, short: 書かされる

    val evalShort = AnswerNormalizer.evaluate("書かされる", target)
    assertTrue("Short causative-passive should be accepted", evalShort.isCorrect)
    assertNotNull(evalShort.note)
  }

  @Test
  fun testWrongEndingProducesDiffAndRule() {
    val target = Conjugator.conjugate("書く", "かく", VerbGroup.GODAN, VerbForm.TA) // 書いた / かいた

    // User wrote 書いて (te-form instead of ta-form)
    val evalWrong = AnswerNormalizer.evaluate("書いて", target)
    assertFalse("Wrong form should be marked incorrect", evalWrong.isCorrect)
    assertNotNull("Diff must not be null", evalWrong.diff)
    assertEquals("書い", evalWrong.diff!!.commonPrefix)
    assertEquals("て", evalWrong.diff!!.userEnding)
    assertEquals("た", evalWrong.diff!!.expectedEnding)
    assertTrue("Rule explanation should be provided", evalWrong.ruleExplanation!!.contains("past ta-form"))
  }
}
