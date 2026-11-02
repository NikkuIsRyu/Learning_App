package tech.nikku.learningapp.core.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import tech.nikku.learningapp.core.domain.model.CharBreakdown
import tech.nikku.learningapp.core.domain.model.ConfusableSide
import tech.nikku.learningapp.core.domain.model.ExercisePayload
import tech.nikku.learningapp.core.domain.model.PatternToken
import tech.nikku.learningapp.core.domain.model.SpeakerInfo

/** Shared JSON config — also used by ContentSeeder and ETL contract tests. */
val ContentJson: Json = Json {
  ignoreUnknownKeys = true
  encodeDefaults = true
  classDiscriminator = "kind"
  explicitNulls = false
}

class Converters {
  private val strings = ListSerializer(String.serializer())
  private val tokens = ListSerializer(PatternToken.serializer())
  private val breakdown = ListSerializer(CharBreakdown.serializer())
  private val speakers = MapSerializer(String.serializer(), SpeakerInfo.serializer())

  @TypeConverter
  fun fromStrings(v: List<String>?): String? = v?.let { ContentJson.encodeToString(strings, it) }

  @TypeConverter
  fun toStrings(v: String?): List<String>? = v?.let { ContentJson.decodeFromString(strings, it) }

  @TypeConverter
  fun fromTokens(v: List<PatternToken>?): String? = v?.let { ContentJson.encodeToString(tokens, it) }

  @TypeConverter
  fun toTokens(v: String?): List<PatternToken>? = v?.let { ContentJson.decodeFromString(tokens, it) }

  @TypeConverter
  fun fromBreakdown(v: List<CharBreakdown>?): String? = v?.let { ContentJson.encodeToString(breakdown, it) }

  @TypeConverter
  fun toBreakdown(v: String?): List<CharBreakdown>? = v?.let { ContentJson.decodeFromString(breakdown, it) }

  @TypeConverter
  fun fromSpeakers(v: Map<String, SpeakerInfo>?): String? = v?.let { ContentJson.encodeToString(speakers, it) }

  @TypeConverter
  fun toSpeakers(v: String?): Map<String, SpeakerInfo>? = v?.let { ContentJson.decodeFromString(speakers, it) }

  @TypeConverter
  fun fromSide(v: ConfusableSide?): String? = v?.let { ContentJson.encodeToString(ConfusableSide.serializer(), it) }

  @TypeConverter
  fun toSide(v: String?): ConfusableSide? = v?.let { ContentJson.decodeFromString(ConfusableSide.serializer(), it) }

  @TypeConverter
  fun fromPayload(v: ExercisePayload?): String? = v?.let { ContentJson.encodeToString(ExercisePayload.serializer(), it) }

  @TypeConverter
  fun toPayload(v: String?): ExercisePayload? = v?.let { ContentJson.decodeFromString(ExercisePayload.serializer(), it) }
}
