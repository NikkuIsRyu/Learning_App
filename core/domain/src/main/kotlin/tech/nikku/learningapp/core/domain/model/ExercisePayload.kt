package tech.nikku.learningapp.core.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class PatternToken(
  val text: String,
  val role: SyntaxRole,
  val tooltip: String? = null
)

/** One Hanzi with tone (0 = neutral) — ported from web `breakdown`. */
@Serializable
data class CharBreakdown(
  val char: String,
  val pinyin: String,
  val tone: Int
)

@Serializable
data class SpeakerInfo(
  val label: String,
  val labelEn: String? = null,
  val role: String,
  val roleEn: String? = null,
  val voice: String? = null,
  val gender: String,
  val colorHex: String
)

@Serializable
data class SentenceSample(
  val text: String,
  val romanization: String,
  val translationEn: String,
  val translationDe: String? = null
)

@Serializable
data class WrongSample(
  val text: String,
  val romanization: String,
  val explanationEn: String,
  val explanationDe: String? = null
)

@Serializable
data class ConfusableSide(
  val term: String,
  val romanization: String,
  val conceptEn: String,
  val conceptDe: String? = null,
  val formula: String,
  val ruleEn: String,
  val ruleDe: String? = null,
  val correct: SentenceSample,
  val incorrect: WrongSample
)

/** Polymorphic exercise payloads. JSON discriminator key = "kind". */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("kind")
@Serializable
sealed interface ExercisePayload {
  @Serializable
  @SerialName("conjugation")
  data class Conjugation(
    val wordId: String,
    val from: VerbForm,
    val to: VerbForm,
    val polite: Boolean = false,
    val acceptedOverrides: List<String> = emptyList()
  ) : ExercisePayload

  @Serializable
  @SerialName("particle_cloze")
  data class ParticleCloze(
    val segments: List<ClozeSegment>,
    val translationEn: String,
    val translationDe: String? = null
  ) : ExercisePayload

  @Serializable
  @SerialName("scramble")
  data class Scramble(
    val chunks: List<String>,
    val acceptedOrders: List<List<Int>>,
    val distractors: List<String> = emptyList(),
    val translationEn: String,
    val translationDe: String? = null
  ) : ExercisePayload

  @Serializable
  @SerialName("listening")
  data class Listening(
    val dialogueId: String,
    val turnIdx: Int,
    val task: ListeningTask,
    val options: List<String> = emptyList(),
    val answers: List<String>
  ) : ExercisePayload
}

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("kind")
@Serializable
sealed interface ClozeSegment {
  /** `ruby` uses canonical ruby markup, e.g. "{窓|まど}" */
  @Serializable
  @SerialName("text")
  data class Text(val ruby: String) : ClozeSegment

  @Serializable
  @SerialName("blank")
  data class Blank(
    val answers: List<String>, // accepted particles, e.g. ["が"]
    val options: List<String>, // chips shown, e.g. ["は","が","を","に"]
    val feedback: Map<String, String> = emptyMap(), // wrong option → explanation
    val ruleRef: String? = null // grammar rule id for "Learn why"
  ) : ClozeSegment
}
