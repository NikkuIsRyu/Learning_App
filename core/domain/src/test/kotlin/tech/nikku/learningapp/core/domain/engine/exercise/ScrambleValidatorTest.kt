package tech.nikku.learningapp.core.domain.engine.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.ExercisePayload

class ScrambleValidatorTest {

  @Test
  fun validate_exactOrder_matchesSuccessfully() {
    val payload = ExercisePayload.Scramble(
      chunks = listOf("明日", "病院で", "先生に", "喉を", "見てもらいます。"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3, 4),
        listOf(1, 0, 2, 3, 4)
      ),
      translationEn = "Tomorrow I will have the doctor examine my throat at the hospital."
    )

    val result = ScrambleValidator.validate(payload, listOf(0, 1, 2, 3, 4))
    assertTrue("Should be correct on exact canonical order", result.isCorrect)
    assertTrue("Should be complete", result.isComplete)
    assertEquals(0, result.matchedOrderIndex)
    assertEquals("明日病院で先生に喉を見てもらいます。", result.userSentence)
  }

  @Test
  fun validate_alternativeOrder_matchesSuccessfully() {
    val payload = ExercisePayload.Scramble(
      chunks = listOf("明日", "病院で", "先生に", "喉を", "見てもらいます。"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3, 4),
        listOf(1, 0, 2, 3, 4)
      ),
      translationEn = "Tomorrow I will have the doctor examine my throat at the hospital."
    )

    val result = ScrambleValidator.validate(payload, listOf(1, 0, 2, 3, 4))
    assertTrue("Should be correct on alternative accepted order", result.isCorrect)
    assertTrue("Should be complete", result.isComplete)
    assertEquals(1, result.matchedOrderIndex)
    assertEquals("病院で明日先生に喉を見てもらいます。", result.userSentence)
  }

  @Test
  fun validate_duplicateChunks_matchesViaStringEquivalence() {
    // Two identical "を" chunks at index 1 and 3
    val payload = ExercisePayload.Scramble(
      chunks = listOf("リンゴ", "を", "ミカン", "を", "買いました。"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3, 4) // "リンゴをミカンを買いました。"
      ),
      translationEn = "I bought apples and oranges."
    )

    // User picked index 3 first ("を") and index 1 second ("を")
    val userIndices = listOf(0, 3, 2, 1, 4)
    val result = ScrambleValidator.validate(payload, userIndices)

    assertTrue("Should match via string equivalence when identical chunks are swapped", result.isCorrect)
    assertTrue(result.isComplete)
    assertEquals("リンゴをミカンを買いました。", result.userSentence)
  }

  @Test
  fun validate_incompleteSentence_returnsIncompleteAndIncorrect() {
    val payload = ExercisePayload.Scramble(
      chunks = listOf("駅の近くで", "新しいアパートを", "探しています。"),
      acceptedOrders = listOf(listOf(0, 1, 2)),
      translationEn = "I am looking for a new apartment near the station."
    )

    val result = ScrambleValidator.validate(payload, listOf(0, 1))
    assertFalse("Incomplete sequence should not be marked correct", result.isCorrect)
    assertFalse("Should be marked incomplete", result.isComplete)
    assertEquals("駅の近くで新しいアパートを", result.userSentence)
  }

  @Test
  fun validate_wrongOrder_returnsIncorrect() {
    val payload = ExercisePayload.Scramble(
      chunks = listOf("駅の近くで", "新しいアパートを", "探しています。"),
      acceptedOrders = listOf(listOf(0, 1, 2)),
      translationEn = "I am looking for a new apartment near the station."
    )

    val result = ScrambleValidator.validate(payload, listOf(2, 1, 0))
    assertFalse("Wrong order should be incorrect", result.isCorrect)
    assertTrue("Length matches so isComplete is true", result.isComplete)
  }

  @Test
  fun validate_withDistractor_returnsIncorrect() {
    val payload = ExercisePayload.Scramble(
      chunks = listOf("駅の近くで", "新しいアパートを", "探しています。", "に"),
      acceptedOrders = listOf(listOf(0, 1, 2)),
      distractors = listOf("に"),
      translationEn = "I am looking for a new apartment near the station."
    )

    // User placed distractor index 3 instead of index 2
    val result = ScrambleValidator.validate(payload, listOf(0, 1, 3))
    assertFalse("Distractor selection must be incorrect", result.isCorrect)
  }
}
