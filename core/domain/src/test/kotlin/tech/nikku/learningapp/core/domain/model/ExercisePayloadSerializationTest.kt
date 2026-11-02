package tech.nikku.learningapp.core.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExercisePayloadSerializationTest {

  private val json = Json {
    classDiscriminator = "kind"
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
  }

  @Test
  fun roundTrip_conjugationPayload() {
    val payload: ExercisePayload = ExercisePayload.Conjugation(
      wordId = "ja:mnn:27:w003",
      from = VerbForm.DICTIONARY,
      to = VerbForm.POTENTIAL,
      polite = false,
      acceptedOverrides = listOf("みられる", "みれる")
    )

    val encoded = json.encodeToString(payload)
    assertTrue("JSON must contain kind: $encoded", encoded.contains(""""kind":"conjugation""""))

    val decoded = json.decodeFromString<ExercisePayload>(encoded)
    assertEquals(payload, decoded)
  }

  @Test
  fun roundTrip_particleClozePayload() {
    val payload: ExercisePayload = ExercisePayload.ParticleCloze(
      segments = listOf(
        ClozeSegment.Text(ruby = "{窓|まど}"),
        ClozeSegment.Blank(
          answers = listOf("が"),
          options = listOf("は", "が", "を", "に"),
          feedback = mapOf("を" to "Use が with intransitive verbs like 開きます"),
          ruleRef = "ja:mnn:29.1"
        ),
        ClozeSegment.Text(ruby = "{開|あ}きます。")
      ),
      translationEn = "The door opens.",
      translationDe = "Die Tür öffnet sich."
    )

    val encoded = json.encodeToString(payload)
    assertTrue("JSON must contain kind: $encoded", encoded.contains(""""kind":"particle_cloze""""))

    val decoded = json.decodeFromString<ExercisePayload>(encoded)
    assertEquals(payload, decoded)
  }

  @Test
  fun roundTrip_scramblePayload() {
    val payload: ExercisePayload = ExercisePayload.Scramble(
      chunks = listOf("日曜日", "友達と", "映画を", "見に行きます"),
      acceptedOrders = listOf(
        listOf(0, 1, 2, 3),
        listOf(1, 0, 2, 3)
      ),
      distractors = listOf("映画に"),
      translationEn = "On Sunday, I will go watch a movie with my friend.",
      translationDe = "Am Sonntag gehe ich mit einem Freund ins Kino."
    )

    val encoded = json.encodeToString(payload)
    assertTrue("JSON must contain kind: $encoded", encoded.contains(""""kind":"scramble""""))

    val decoded = json.decodeFromString<ExercisePayload>(encoded)
    assertEquals(payload, decoded)
  }

  @Test
  fun roundTrip_listeningPayload() {
    val payload: ExercisePayload = ExercisePayload.Listening(
      dialogueId = "zh:d:Dialogue2_1",
      turnIdx = 2,
      task = ListeningTask.COMPREHENSION_MCQ,
      options = listOf("北京", "上海", "广州"),
      answers = listOf("北京")
    )

    val encoded = json.encodeToString(payload)
    assertTrue("JSON must contain kind: $encoded", encoded.contains(""""kind":"listening""""))

    val decoded = json.decodeFromString<ExercisePayload>(encoded)
    assertEquals(payload, decoded)
  }
}
