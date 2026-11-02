package tech.nikku.learningapp.core.domain.engine.conjugation

data class Conjugation(
  val surface: String,
  val reading: String,
  val ruleExplanation: String,
  val acceptedOverrides: List<String> = emptyList(),
  val notes: List<String> = emptyList()
)
