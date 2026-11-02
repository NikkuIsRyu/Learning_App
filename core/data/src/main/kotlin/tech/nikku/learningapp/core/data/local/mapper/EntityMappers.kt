package tech.nikku.learningapp.core.data.local.mapper

import tech.nikku.learningapp.core.data.local.entity.*
import tech.nikku.learningapp.core.domain.model.*

fun WordEntity.toDomain(): Word = Word(
  id = id,
  domain = domain,
  headword = headword,
  reading = reading,
  rubyMarkup = rubyMarkup,
  romanization = romanization,
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
  contentVersion = contentVersion
)

fun KanjiEntity.toDomain(compounds: List<KanjiCompound> = emptyList()): Kanji = Kanji(
  literal = literal,
  onReadings = onReadings,
  kunReadings = kunReadings,
  meaningsEn = meaningsEn,
  meaningsDe = meaningsDe,
  strokeCount = strokeCount,
  strokePathsKanjiVg = strokePaths,
  radical = radical,
  components = components,
  chapter = introducedChapter,
  jlpt = jlpt,
  grade = grade,
  frequency = frequencyRank,
  compounds = compounds,
  contentVersion = contentVersion
)

fun GrammarExampleEntity.toDomain(): GrammarExample = GrammarExample(
  ruleId = ruleId,
  idx = sortOrder,
  japanese = text,
  rubyMarkup = rubyMarkup,
  hanzi = text,
  pinyin = romanization,
  breakdown = breakdown,
  translationEn = translationEn,
  translationDe = translationDe,
  audioRef = null
)

fun GrammarRuleEntity.toDomain(examples: List<GrammarExample> = emptyList()): GrammarRule = GrammarRule(
  id = id,
  domain = domain,
  chapter = chapter,
  titleEn = titleEn,
  titleDe = titleDe,
  titleNative = null,
  formula = pattern,
  formulaPills = patternTokens,
  explanationEn = summaryEn,
  explanationDe = summaryDe,
  category = category,
  sortOrder = sortOrder,
  examples = examples,
  contentVersion = contentVersion
)

fun ConfusablePairEntity.toDomain(): ConfusablePair = ConfusablePair(
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
  contentVersion = contentVersion
)

fun DialogueTurnEntity.toDomain(): DialogueTurn = DialogueTurn(
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

fun DialogueEntity.toDomain(turns: List<DialogueTurn> = emptyList()): Dialogue = Dialogue(
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
  turns = turns,
  sortOrder = sortOrder,
  contentVersion = contentVersion
)

fun tech.nikku.learningapp.core.data.local.relation.DialogueWithTurns.toDomain(): Dialogue =
  dialogue.toDomain(turns = turns.map { it.toDomain() })

fun UserProgressEntity.toDomain(): UserProgress = UserProgress(
  itemKey = itemKey,
  itemType = itemType,
  domain = domain,
  state = state,
  intervalDays = intervalDays,
  easeFactor = easeFactor,
  totalReviews = totalReviews,
  lapseCount = lapses,
  dueTimestampMs = dueAt,
  lastReviewTimestampMs = lastReviewedAt,
  dirty = dirty
)

fun ReviewLogEntity.toDomain(): ReviewLog = ReviewLog(
  id = id,
  itemKey = itemKey,
  grade = grade,
  stateBefore = prevState,
  stateAfter = newState,
  intervalDays = newIntervalDays,
  timestampMs = reviewedAt,
  reviewDurationMs = responseMs
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
  id = id,
  domain = domain,
  chapter = chapter,
  type = type,
  grammarRuleId = grammarRuleId,
  difficulty = difficulty,
  payload = payload,
  verified = verified,
  sortOrder = sortOrder,
  contentVersion = contentVersion
)
