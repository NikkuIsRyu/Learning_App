package tech.nikku.learningapp.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
  val id: String,
  val domain: Domain,
  val headword: String,
  val reading: String,
  val rubyMarkup: String? = null,
  val romanization: String? = null,
  val meaningEn: String,
  val meaningDe: String? = null,
  val pos: PartOfSpeech,
  val verbGroup: VerbGroup? = null,
  val transitivity: Transitivity? = null,
  val pairWordId: String? = null,
  val dictionaryForm: String? = null,
  val dictionaryReading: String? = null,
  val chapter: Int? = null,
  val orderInChapter: Int? = null,
  val jlpt: JlptLevel? = null,
  val hskLevel: Int? = null,
  val tags: List<String> = emptyList(),
  val source: ContentSource = ContentSource.MINNA,
  val verified: Boolean = false,
  val errataNote: String? = null,
  val audioRef: String? = null,
  val contentVersion: Int = 1
)

@Serializable
data class KanjiCompound(
  val kanjiLiteral: String,
  val compound: String,
  val reading: String,
  val rubyMarkup: String? = null,
  val meaningEn: String,
  val meaningDe: String? = null,
  val wordId: String? = null,
  val sortOrder: Int = 0
)

@Serializable
data class Kanji(
  val literal: String,
  val onReadings: List<String> = emptyList(),
  val kunReadings: List<String> = emptyList(),
  val meaningsEn: List<String> = emptyList(),
  val meaningsDe: List<String> = emptyList(),
  val strokeCount: Int,
  val strokePathsKanjiVg: List<String> = emptyList(),
  val radical: String? = null,
  val components: List<String> = emptyList(),
  val chapter: Int? = null,
  val jlpt: JlptLevel? = null,
  val grade: Int? = null,
  val frequency: Int? = null,
  val compounds: List<KanjiCompound> = emptyList(),
  val contentVersion: Int = 1
)

@Serializable
data class GrammarExample(
  val ruleId: String,
  val idx: Int,
  val japanese: String? = null,
  val rubyMarkup: String? = null,
  val hanzi: String? = null,
  val pinyin: String? = null,
  val breakdown: List<CharBreakdown> = emptyList(),
  val translationEn: String,
  val translationDe: String? = null,
  val audioRef: String? = null
)

@Serializable
data class GrammarRule(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val titleEn: String,
  val titleDe: String? = null,
  val titleNative: String? = null,
  val formula: String,
  val formulaPills: List<PatternToken> = emptyList(),
  val explanationEn: String,
  val explanationDe: String? = null,
  val category: String? = null,
  val sortOrder: Int = 0,
  val examples: List<GrammarExample> = emptyList(),
  val contentVersion: Int = 1
)

@Serializable
data class DialogueTurn(
  val dialogueId: String,
  val idx: Int,
  val speaker: String,
  val text: String,
  val rubyMarkup: String? = null,
  val romanization: String? = null,
  val translationEn: String? = null,
  val translationDe: String? = null,
  val startMs: Long? = null,
  val endMs: Long? = null
)

@Serializable
data class Dialogue(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val titleEn: String,
  val titleDe: String? = null,
  val nativeTitle: String,
  val topicEn: String? = null,
  val topicDe: String? = null,
  val descriptionEn: String? = null,
  val descriptionDe: String? = null,
  val audioRef: String? = null,
  val durationMs: Long? = null,
  val speakers: Map<String, SpeakerInfo> = emptyMap(),
  val turns: List<DialogueTurn> = emptyList(),
  val sortOrder: Int = 0,
  val contentVersion: Int = 1
)

@Serializable
data class ConfusablePair(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val pairLabel: String,
  val topicEn: String,
  val topicDe: String? = null,
  val left: ConfusableSide,
  val right: ConfusableSide,
  val mnemonicEn: String,
  val mnemonicDe: String? = null,
  val sortOrder: Int = 0,
  val contentVersion: Int = 1
)

@Serializable
data class Exercise(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val type: ExerciseType,
  val grammarRuleId: String? = null,
  val difficulty: Int = 1,
  val payload: ExercisePayload,
  val verified: Boolean = true,
  val sortOrder: Int = 0,
  val contentVersion: Int = 1
)

@Serializable
data class UserProgress(
  val itemKey: String,
  val itemType: ItemType,
  val domain: Domain,
  val state: SrsState = SrsState.NEW,
  val intervalDays: Double = 0.0,
  val easeFactor: Double = 2.5,
  val totalReviews: Int = 0,
  val lapseCount: Int = 0,
  val dueTimestampMs: Long = 0L,
  val lastReviewTimestampMs: Long? = null,
  val dirty: Boolean = true
)

@Serializable
data class ReviewLog(
  val id: String,
  val itemKey: String,
  val grade: ReviewGrade,
  val stateBefore: SrsState,
  val stateAfter: SrsState,
  val intervalDays: Double,
  val timestampMs: Long,
  val reviewDurationMs: Long = 0L
)

@Serializable
data class ReviewItem(
  val progress: UserProgress,
  val word: Word? = null,
  val kanji: Kanji? = null,
  val grammar: GrammarRule? = null,
  val exercise: Exercise? = null,
  val confusable: ConfusablePair? = null
)
