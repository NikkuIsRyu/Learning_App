package tech.nikku.learningapp.core.domain.engine.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.ClozeSegment

class ClozeGraderTest {

  private val sampleBlank = ClozeSegment.Blank(
    answers = listOf("が"),
    options = listOf("は", "が", "を", "に"),
    feedback = mapOf(
      "を" to "Direct object marker を is used with transitive actions, not with intransitive verbs or states.",
      "は" to "は marks the global topic, whereas が marks the specific subject.",
      "に" to "に marks target or location, not the subject."
    ),
    ruleRef = "ja:mnn:29.1"
  )

  @Test
  fun testCorrectAnswerProducesValidEvaluation() {
    val result = ClozeGrader.grade(sampleBlank, "が")
    assertTrue(result.isCorrect)
    assertEquals("が", result.selectedOption)
    assertNull(result.feedback)
    assertEquals("ja:mnn:29.1", result.ruleRef)
  }

  @Test
  fun testWrongAnswerReturnsDetailedFeedback() {
    val result = ClozeGrader.grade(sampleBlank, "を")
    assertFalse(result.isCorrect)
    assertEquals("を", result.selectedOption)
    assertNotNull(result.feedback)
    assertTrue(result.feedback!!.contains("Direct object marker"))
    assertEquals("ja:mnn:29.1", result.ruleRef)
  }

  @Test
  fun testFallbackFeedbackWhenNotExplicitlyConfigured() {
    val result = ClozeGrader.grade(sampleBlank, "で")
    assertFalse(result.isCorrect)
    assertNotNull(result.feedback)
    assertTrue(result.feedback!!.contains("Correct particle: が"))
  }
}
