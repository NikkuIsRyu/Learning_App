package tech.nikku.learningapp.core.domain.engine.exercise

import tech.nikku.learningapp.core.domain.model.ExercisePayload

data class ScrambleValidationResult(
  val isCorrect: Boolean,
  val userSentence: String,
  val expectedSentence: String,
  val matchedOrderIndex: Int? = null,
  val isComplete: Boolean = false
)

object ScrambleValidator {

  /**
   * Evaluates a sequence of chosen chunk indices against the Scramble exercise payload.
   *
   * [selectedIndices] contains the indices into [payload.chunks] placed by the user.
   */
  fun validate(
    payload: ExercisePayload.Scramble,
    selectedIndices: List<Int>
  ): ScrambleValidationResult {
    val chunks = payload.chunks
    val userTokens = selectedIndices.mapNotNull { idx -> chunks.getOrNull(idx) }
    val userSentence = userTokens.joinToString("")

    val canonicalOrder = payload.acceptedOrders.firstOrNull() ?: chunks.indices.toList()
    val expectedSentence = canonicalOrder.mapNotNull { idx -> chunks.getOrNull(idx) }.joinToString("")

    val requiredCount = canonicalOrder.size
    val isComplete = selectedIndices.size == requiredCount

    // 1. Direct index list match against any accepted order
    val exactIndex = payload.acceptedOrders.indexOfFirst { order -> order == selectedIndices }
    if (exactIndex != -1) {
      return ScrambleValidationResult(
        isCorrect = true,
        userSentence = userSentence,
        expectedSentence = expectedSentence,
        matchedOrderIndex = exactIndex,
        isComplete = true
      )
    }

    // 2. Normalized string equivalence (handles identical chunks like two "を" or duplicate particles)
    if (isComplete) {
      val normalizedUser = normalize(userSentence)
      for ((orderIdx, order) in payload.acceptedOrders.withIndex()) {
        if (order.size == selectedIndices.size) {
          val orderSentence = order.mapNotNull { idx -> chunks.getOrNull(idx) }.joinToString("")
          if (normalizedUser == normalize(orderSentence)) {
            return ScrambleValidationResult(
              isCorrect = true,
              userSentence = userSentence,
              expectedSentence = expectedSentence,
              matchedOrderIndex = orderIdx,
              isComplete = true
            )
          }
        }
      }
    }

    return ScrambleValidationResult(
      isCorrect = false,
      userSentence = userSentence,
      expectedSentence = expectedSentence,
      matchedOrderIndex = null,
      isComplete = isComplete
    )
  }

  private fun normalize(s: String): String {
    return s.replace(Regex("""\{([^|]+)\|[^}]+\}"""), "$1")
      .replace(Regex("""\[[^\]]+\]"""), "")
      .replace(" ", "")
      .replace("　", "")
      .trim()
  }
}
