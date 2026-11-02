package tech.nikku.learningapp.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Domain {
  JAPANESE,
  CHINESE,
  ACADEMIC
}

@Serializable
enum class ItemType {
  WORD,
  KANJI,
  GRAMMAR,
  EXERCISE,
  CONJUGATION_CELL,
  CONFUSABLE
}

@Serializable
enum class PartOfSpeech {
  NOUN,
  PRONOUN,
  VERB,
  I_ADJECTIVE,
  NA_ADJECTIVE,
  ADVERB,
  PARTICLE,
  CONJUNCTION,
  COUNTER,
  MEASURE_WORD,
  EXPRESSION,
  OTHER
}

@Serializable
enum class VerbGroup {
  GODAN,    // Minna Group I
  ICHIDAN,  // Minna Group II
  SURU,     // Minna Group III (する)
  KURU      // Minna Group III (来る)
}

@Serializable
enum class Transitivity {
  TRANSITIVE,    // 他動詞
  INTRANSITIVE,  // 自動詞
  BOTH
}

@Serializable
enum class JlptLevel {
  N5,
  N4,
  N3,
  N2,
  N1
}

@Serializable
enum class ContentSource {
  MINNA,
  COURSE,
  EXTENDED,
  USER
}

@Serializable
enum class ExerciseType {
  CONJUGATION,
  PARTICLE_CLOZE,
  SCRAMBLE,
  LISTENING,
  MEANING_RECALL,
  KANJI_READING,
  GRAMMAR_CARD,
  CONFUSABLE_CHOICE
}

@Serializable
enum class VerbForm {
  MASU,
  DICTIONARY,
  NAI,
  TA,
  TE,
  POTENTIAL,
  VOLITIONAL,
  PASSIVE,
  CAUSATIVE,
  CAUSATIVE_PASSIVE
}

@Serializable
enum class SrsState {
  NEW,
  LEARNING,
  REVIEW,
  RELEARNING,
  SUSPENDED
}

@Serializable
enum class ReviewGrade {
  AGAIN,
  HARD,
  GOOD,
  EASY
}

@Serializable
enum class FeedbackKind {
  BUG,
  CONTENT_ERRATUM,
  SUGGESTION
}

@Serializable
enum class ListeningTask {
  DICTATION,
  COMPREHENSION_MCQ,
  SHADOWING
}

/** Role used for syntactic colour-coding of grammar formulas ("formula pills"). */
@Serializable
enum class SyntaxRole(val label: String) {
  SUBJECT("Subject"),
  TOPIC("Topic"),
  TIME("Time"),
  PLACE("Place"),
  MODAL("Modal"),
  NEGATION("Negation"),
  VERB("Verb"),
  OBJECT("Object"),
  COMPLEMENT("Complement"),
  PARTICLE("Particle"),
  MEASURE("Measure"),
  CONNECTOR("Connector"),
  LITERAL("Literal")
}
