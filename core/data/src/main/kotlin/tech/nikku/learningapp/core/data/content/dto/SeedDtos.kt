package tech.nikku.learningapp.core.data.content.dto

import kotlinx.serialization.Serializable
import tech.nikku.learningapp.core.data.local.entity.*
import tech.nikku.learningapp.core.domain.model.*

@Serializable
data class ManifestDto(
  val contentVersion: Int,
  val files: List<ManifestFileDto>
)

@Serializable
data class ManifestFileDto(
  val path: String,
  val entity: String,
  val sha256: String
)

@Serializable
data class WordDto(
  val id: String,
  val domain: Domain,
  val headword: String,
  val reading: String,
  val rubyMarkup: String? = null,
  val romanization: String? = null,
  val searchText: String,
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
  val source: ContentSource,
  val verified: Boolean = false,
  val errataNote: String? = null,
  val audioRef: String? = null,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = WordEntity(
    id = id,
    domain = domain,
    headword = headword,
    reading = reading,
    rubyMarkup = rubyMarkup,
    romanization = romanization,
    searchText = searchText,
    meaningEn = meaningEn,
    meaningDe = meaningDe,
    pos = pos,
    verbGroup = verbGroup,
    transitivity = transitivity,
    pairWordId = pairWordId,
    dictionaryForm = dictionaryForm,
    dictionaryReading = dictionaryReading,
    chapter = chapter,
    orderInChapter = orderInChapter,
    jlpt = jlpt,
    hskLevel = hskLevel,
    tags = tags,
    source = source,
    verified = verified,
    errataNote = errataNote,
    audioRef = audioRef,
    contentVersion = version
  )
}

@Serializable
data class KanjiDto(
  val literal: String,
  val id: String,
  val strokeCount: Int,
  val onReadings: List<String> = emptyList(),
  val kunReadings: List<String> = emptyList(),
  val meaningsEn: List<String> = emptyList(),
  val meaningsDe: List<String> = emptyList(),
  val radical: String,
  val radicalNumber: Int? = null,
  val components: List<String> = emptyList(),
  val strokePaths: List<String> = emptyList(),
  val introducedChapter: Int? = null,
  val jlpt: JlptLevel? = null,
  val grade: Int? = null,
  val frequencyRank: Int? = null,
  val mnemonicEn: String? = null,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = KanjiEntity(
    literal = literal,
    id = id,
    strokeCount = strokeCount,
    onReadings = onReadings,
    kunReadings = kunReadings,
    meaningsEn = meaningsEn,
    meaningsDe = meaningsDe,
    radical = radical,
    radicalNumber = radicalNumber,
    components = components,
    strokePaths = strokePaths,
    introducedChapter = introducedChapter,
    jlpt = jlpt,
    grade = grade,
    frequencyRank = frequencyRank,
    mnemonicEn = mnemonicEn,
    contentVersion = version
  )
}

@Serializable
data class KanjiCompoundDto(
  val kanji: String,
  val wordId: String,
  val verified: Boolean = true,
  val sortOrder: Int
) {
  fun toEntity() = KanjiCompoundCrossRef(
    kanji = kanji,
    wordId = wordId,
    verified = verified,
    sortOrder = sortOrder
  )
}

@Serializable
data class GrammarRuleDto(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val sectionLabel: String? = null,
  val category: String? = null,
  val part: String? = null,
  val titleEn: String,
  val titleDe: String? = null,
  val pattern: String,
  val patternTokens: List<PatternToken> = emptyList(),
  val summaryEn: String,
  val summaryDe: String? = null,
  val nuancesEn: List<String> = emptyList(),
  val nuancesDe: List<String> = emptyList(),
  val bodyMarkdown: String? = null,
  val tags: List<String> = emptyList(),
  val verified: Boolean = true,
  val sortOrder: Int,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = GrammarRuleEntity(
    id = id,
    domain = domain,
    chapter = chapter,
    sectionLabel = sectionLabel,
    category = category,
    part = part,
    titleEn = titleEn,
    titleDe = titleDe,
    pattern = pattern,
    patternTokens = patternTokens,
    summaryEn = summaryEn,
    summaryDe = summaryDe,
    nuancesEn = nuancesEn,
    nuancesDe = nuancesDe,
    bodyMarkdown = bodyMarkdown,
    tags = tags,
    verified = verified,
    sortOrder = sortOrder,
    contentVersion = version
  )
}

@Serializable
data class GrammarExampleDto(
  val id: String,
  val ruleId: String,
  val text: String,
  val rubyMarkup: String? = null,
  val romanization: String? = null,
  val translationEn: String,
  val translationDe: String? = null,
  val breakdown: List<CharBreakdown> = emptyList(),
  val note: String? = null,
  val sortOrder: Int
) {
  fun toEntity() = GrammarExampleEntity(
    id = id,
    ruleId = ruleId,
    text = text,
    rubyMarkup = rubyMarkup,
    romanization = romanization,
    translationEn = translationEn,
    translationDe = translationDe,
    breakdown = breakdown,
    note = note,
    sortOrder = sortOrder
  )
}

@Serializable
data class ConfusablePairDto(
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
  val sortOrder: Int,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = ConfusablePairEntity(
    id = id,
    domain = domain,
    chapter = chapter,
    pairLabel = pairLabel,
    topicEn = topicEn,
    topicDe = topicDe,
    left = left,
    right = right,
    mnemonicEn = mnemonicEn,
    mnemonicDe = mnemonicDe,
    sortOrder = sortOrder,
    contentVersion = version
  )
}

@Serializable
data class DialogueDto(
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
  val sortOrder: Int,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = DialogueEntity(
    id = id,
    domain = domain,
    chapter = chapter,
    titleEn = titleEn,
    titleDe = titleDe,
    nativeTitle = nativeTitle,
    topicEn = topicEn,
    topicDe = topicDe,
    descriptionEn = descriptionEn,
    descriptionDe = descriptionDe,
    audioRef = audioRef,
    durationMs = durationMs,
    speakers = speakers,
    sortOrder = sortOrder,
    contentVersion = version
  )
}

@Serializable
data class DialogueTurnDto(
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
) {
  fun toEntity() = DialogueTurnEntity(
    dialogueId = dialogueId,
    idx = idx,
    speaker = speaker,
    text = text,
    rubyMarkup = rubyMarkup,
    romanization = romanization,
    translationEn = translationEn,
    translationDe = translationDe,
    startMs = startMs,
    endMs = endMs
  )
}

@Serializable
data class ExerciseDto(
  val id: String,
  val domain: Domain,
  val chapter: Int? = null,
  val type: ExerciseType,
  val grammarRuleId: String? = null,
  val difficulty: Int = 1,
  val payload: ExercisePayload,
  val verified: Boolean = false,
  val sortOrder: Int,
  val contentVersion: Int = 1
) {
  fun toEntity(version: Int = contentVersion) = ExerciseEntity(
    id = id,
    domain = domain,
    chapter = chapter,
    type = type,
    grammarRuleId = grammarRuleId,
    difficulty = difficulty,
    payload = payload,
    verified = verified,
    sortOrder = sortOrder,
    contentVersion = version
  )
}
