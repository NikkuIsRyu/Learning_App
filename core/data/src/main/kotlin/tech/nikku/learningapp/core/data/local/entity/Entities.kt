package tech.nikku.learningapp.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey
import tech.nikku.learningapp.core.domain.model.*

// ───────────────────────────── CONTENT (read-mostly, re-seedable) ─────────────────────────────

@Entity(
  tableName = "words",
  indices = [
    Index(value = ["domain", "chapter", "orderInChapter"]),
    Index(value = ["domain", "jlpt"]),
    Index(value = ["domain", "pos"]),
    Index(value = ["reading"]),
    Index(value = ["pairWordId"])
  ]
)
data class WordEntity(
  @PrimaryKey val id: String,
  val domain: Domain,
  val headword: String,            // 見ます（診ます） / 比
  val reading: String,             // みます / bǐ (kana for JA, tone-marked pinyin for ZH)
  val rubyMarkup: String?,         // {見|み}ます (canonical ruby markup, §7.4)
  val romanization: String?,       // mimasu / bi3
  val searchText: String,          // precomputed: romaji, compact pinyin, kana variants (FTS input)
  val meaningEn: String,
  val meaningDe: String?,
  val pos: PartOfSpeech,
  val verbGroup: VerbGroup?,       // REQUIRED when pos == VERB (validated)
  val transitivity: Transitivity?,
  val pairWordId: String?,         // 自動詞/他動詞 partner (開きます ↔ 開けます)
  val dictionaryForm: String?,     // 見る — conjugation engine lemma
  val dictionaryReading: String?,  // みる
  val chapter: Int?,               // Minna 1–50 / Chinese lesson
  val orderInChapter: Int?,
  val jlpt: JlptLevel?,
  val hskLevel: Int?,
  val tags: List<String>,
  val source: ContentSource,
  val verified: Boolean,           // true for MNN 26–32 (errata-audited)
  val errataNote: String?,         // e.g. "Anki: ✕ ふさます → ふきます"
  val audioRef: String?,
  val contentVersion: Int
)

/** External-content FTS4 over latin-script fields (meanings, romaji, pinyin w/o diacritics). */
@Fts4(contentEntity = WordEntity::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61, tokenizerArgs = ["remove_diacritics=1"])
@Entity(tableName = "words_fts")
data class WordFtsEntity(
  val headword: String,
  val reading: String,
  val searchText: String,
  val meaningEn: String,
  val meaningDe: String?
)

@Entity(
  tableName = "kanji",
  indices = [Index(value = ["introducedChapter"]), Index(value = ["jlpt"])]
)
data class KanjiEntity(
  @PrimaryKey val literal: String,     // "探"
  val id: String,                      // "ja:kanji:探" (SRS itemKey)
  val strokeCount: Int,
  val onReadings: List<String>,        // ["タン"]
  val kunReadings: List<String>,       // ["さが.す", "さぐ.る"]
  val meaningsEn: List<String>,
  val meaningsDe: List<String>,
  val radical: String,                 // classical (Kangxi) radical, e.g. "扌"
  val radicalNumber: Int?,
  val components: List<String>,        // KRADFILE decomposition
  val strokePaths: List<String>,       // ordered SVG path "d" strings (KanjiVG, 109×109 viewBox)
  val introducedChapter: Int?,         // first Minna chapter whose vocab uses it
  val jlpt: JlptLevel?,
  val grade: Int?,
  val frequencyRank: Int?,
  val mnemonicEn: String?,
  val contentVersion: Int
)

@Entity(
  tableName = "kanji_compounds",
  primaryKeys = ["kanji", "wordId"],
  foreignKeys = [
    ForeignKey(entity = KanjiEntity::class, parentColumns = ["literal"], childColumns = ["kanji"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = WordEntity::class, parentColumns = ["id"], childColumns = ["wordId"], onDelete = ForeignKey.CASCADE)
  ],
  indices = [Index(value = ["wordId"])]
)
data class KanjiCompoundCrossRef(
  val kanji: String,
  val wordId: String,
  val verified: Boolean,               // compound comes from verified MNN vocabulary
  val sortOrder: Int
)

@Entity(
  tableName = "grammar_rules",
  indices = [Index(value = ["domain", "chapter", "sortOrder"]), Index(value = ["domain", "category"])]
)
data class GrammarRuleEntity(
  @PrimaryKey val id: String,
  val domain: Domain,
  val chapter: Int?,
  val sectionLabel: String?,           // "26.1"
  val category: String?,               // ZH: syntax | questions | locatives | verbs-modals | numbers-commerce | comparisons | aspect-particles
  val part: String?,                   // ZH: "Teil I: Grundlegende Syntax"
  val titleEn: String,
  val titleDe: String?,
  val pattern: String,                 // raw formula: "Subjekt + Zeitpunkt + Ort + … + Verb + Objekt"
  val patternTokens: List<PatternToken>, // colour-coded pills (ETL-classified)
  val summaryEn: String,
  val summaryDe: String?,
  val nuancesEn: List<String>,
  val nuancesDe: List<String>,
  val bodyMarkdown: String?,           // JA: section body from MDX (rendered as markdown)
  val tags: List<String>,
  val verified: Boolean,
  val sortOrder: Int,
  val contentVersion: Int
)

@Entity(
  tableName = "grammar_examples",
  foreignKeys = [ForeignKey(entity = GrammarRuleEntity::class, parentColumns = ["id"], childColumns = ["ruleId"], onDelete = ForeignKey.CASCADE)],
  indices = [Index(value = ["ruleId", "sortOrder"])]
)
data class GrammarExampleEntity(
  @PrimaryKey val id: String,          // "{ruleId}#ex{n}"
  val ruleId: String,
  val text: String,                    // hanzi / Japanese surface
  val rubyMarkup: String?,
  val romanization: String?,           // pinyin / romaji
  val translationEn: String,
  val translationDe: String?,
  val breakdown: List<CharBreakdown>,  // may be empty
  val note: String?,
  val sortOrder: Int
)

@Entity(
  tableName = "exercises",
  foreignKeys = [ForeignKey(entity = GrammarRuleEntity::class, parentColumns = ["id"], childColumns = ["grammarRuleId"], onDelete = ForeignKey.SET_NULL)],
  indices = [Index(value = ["domain", "chapter", "type"]), Index(value = ["grammarRuleId"])]
)
data class ExerciseEntity(
  @PrimaryKey val id: String,
  val domain: Domain,
  val chapter: Int?,
  val type: ExerciseType,
  val grammarRuleId: String?,
  val difficulty: Int,                 // 1..5
  val payload: ExercisePayload,        // JSON via TypeConverter
  val verified: Boolean,               // false ⇒ UI shows "unverified" badge
  val sortOrder: Int,
  val contentVersion: Int
)

@Entity(tableName = "dialogues", indices = [Index(value = ["domain", "chapter", "sortOrder"])])
data class DialogueEntity(
  @PrimaryKey val id: String,
  val domain: Domain,
  val chapter: Int?,
  val titleEn: String,
  val titleDe: String?,
  val nativeTitle: String,             // "第2课 对话一：…"
  val topicEn: String?,
  val topicDe: String?,
  val descriptionEn: String?,
  val descriptionDe: String?,
  val audioRef: String?,               // "media/zh/dialogues/Dialogue2_1.mp3"; null ⇒ TTS fallback
  val durationMs: Long?,
  val speakers: Map<String, SpeakerInfo>,
  val sortOrder: Int,
  val contentVersion: Int
)

@Entity(
  tableName = "dialogue_turns",
  primaryKeys = ["dialogueId", "idx"],
  foreignKeys = [ForeignKey(entity = DialogueEntity::class, parentColumns = ["id"], childColumns = ["dialogueId"], onDelete = ForeignKey.CASCADE)]
)
data class DialogueTurnEntity(
  val dialogueId: String,
  val idx: Int,
  val speaker: String,                 // key into DialogueEntity.speakers
  val text: String,
  val rubyMarkup: String?,
  val romanization: String?,
  val translationEn: String?,
  val translationDe: String?,
  val startMs: Long?,
  val endMs: Long?
)

@Entity(tableName = "confusable_pairs", indices = [Index(value = ["domain", "chapter", "sortOrder"])])
data class ConfusablePairEntity(
  @PrimaryKey val id: String,
  val domain: Domain,
  val chapter: Int?,
  val pairLabel: String,               // "二 (èr) vs. 两 (liǎng)"
  val topicEn: String,
  val topicDe: String?,
  val left: ConfusableSide,
  val right: ConfusableSide,
  val mnemonicEn: String,
  val mnemonicDe: String?,
  val sortOrder: Int,
  val contentVersion: Int
)

// ───────────────────────────── USER STATE (synced, never cascaded) ─────────────────────────────

@Entity(
  tableName = "user_progress",
  indices = [
    Index(value = ["domain", "state", "dueAt"]),
    Index(value = ["itemType"]),
    Index(value = ["dirty"])
  ]
)
data class UserProgressEntity(
  @PrimaryKey val itemKey: String,     // globally unique, see §4
  val itemType: ItemType,
  val domain: Domain,
  val state: SrsState,
  val easeFactor: Double = 2.5,        // SM-2 EF, floor 1.3
  val intervalDays: Double = 0.0,
  val repetitions: Int = 0,
  val lapses: Int = 0,
  val learningStep: Int = 0,
  val dueAt: Long,                     // epoch ms (UTC)
  val lastReviewedAt: Long? = null,
  val totalReviews: Int = 0,
  val correctReviews: Int = 0,
  val stability: Double? = null,       // reserved for future FSRS migration
  val difficulty: Double? = null,      // reserved for future FSRS migration
  val suspended: Boolean = false,      // user flag (LWW-synced, not derivable from logs)
  // ── sync metadata ──
  val updatedAt: Long,                 // client epoch ms of last local mutation
  val updatedByDevice: String,
  val dirty: Boolean = true,
  val deleted: Boolean = false         // tombstone
)

@Entity(
  tableName = "review_logs",
  indices = [Index(value = ["itemKey", "reviewedAt"]), Index(value = ["reviewedAt"]), Index(value = ["synced"])]
)
data class ReviewLogEntity(
  @PrimaryKey val id: String,          // UUID v4 — idempotency key across devices
  val itemKey: String,
  val itemType: ItemType,
  val domain: Domain,
  val reviewedAt: Long,
  val grade: ReviewGrade,
  val responseMs: Long,
  val exerciseType: ExerciseType?,
  val userAnswer: String?,
  val wasCorrect: Boolean,
  val prevState: SrsState,
  val newState: SrsState,
  val prevIntervalDays: Double,
  val newIntervalDays: Double,
  val prevEase: Double,
  val newEase: Double,
  val deviceId: String,
  val synced: Boolean = false
)

@Entity(tableName = "feedback_queue", indices = [Index(value = ["synced"])])
data class FeedbackEntity(
  @PrimaryKey val id: String,          // UUID
  val createdAt: Long,
  val kind: FeedbackKind,
  val domain: Domain?,
  val itemKey: String?,                // the word/exercise the report is about
  val message: String,
  val appVersion: String,
  val deviceModel: String,
  val synced: Boolean = false
)
