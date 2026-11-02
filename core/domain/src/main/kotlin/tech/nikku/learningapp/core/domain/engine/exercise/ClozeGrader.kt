package tech.nikku.learningapp.core.domain.engine.exercise

import tech.nikku.learningapp.core.domain.model.ClozeSegment

data class ClozeEvaluation(
  val isCorrect: Boolean,
  val selectedOption: String,
  val correctAnswers: List<String>,
  val feedback: String?,
  val ruleRef: String?
)

object ClozeGrader {

  fun grade(
    blank: ClozeSegment.Blank,
    selectedOption: String
  ): ClozeEvaluation {
    val cleanOption = selectedOption.trim()
    val isCorrect = blank.answers.any { it.trim() == cleanOption }

    val feedback = if (!isCorrect) {
      blank.feedback[cleanOption]
        ?: "Incorrect particle choice. Correct particle: ${blank.answers.joinToString(" or ")}"
    } else {
      null
    }

    return ClozeEvaluation(
      isCorrect = isCorrect,
      selectedOption = cleanOption,
      correctAnswers = blank.answers,
      feedback = feedback,
      ruleRef = blank.ruleRef
    )
  }
}
