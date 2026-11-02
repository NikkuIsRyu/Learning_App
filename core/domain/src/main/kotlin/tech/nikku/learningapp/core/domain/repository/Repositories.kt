package tech.nikku.learningapp.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tech.nikku.learningapp.core.domain.model.ConfusablePair
import tech.nikku.learningapp.core.domain.model.Dialogue
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.Exercise
import tech.nikku.learningapp.core.domain.model.ExerciseType
import tech.nikku.learningapp.core.domain.model.GrammarRule
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.ReviewLog
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word

interface LexiconRepository {
  fun observeWords(
    domain: Domain,
    chapters: Set<Int> = emptySet(),
    pos: PartOfSpeech? = null,
    verbGroup: VerbGroup? = null,
    jlpt: JlptLevel? = null,
    searchQuery: String = ""
  ): Flow<List<Word>>

  fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>>
  suspend fun getWordById(id: String): Word?
  suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word>
  suspend fun searchWords(domain: Domain, query: String, limit: Int = 50): List<Word>
}

interface KanjiRepository {
  fun observeKanjiForChapter(chapter: Int): Flow<List<Kanji>>
  suspend fun getKanjiByLiteral(literal: String): Kanji?
  suspend fun getAllKanji(): List<Kanji>
}

interface GrammarRepository {
  fun observeRules(domain: Domain, category: String? = null): Flow<List<GrammarRule>>
  fun observeRulesForChapter(chapter: Int): Flow<List<GrammarRule>>
  suspend fun getRuleById(id: String): GrammarRule?
}

interface ExerciseRepository {
  suspend fun getExercisesForChapter(domain: Domain, chapter: Int, type: ExerciseType? = null): List<Exercise>
  suspend fun getExerciseById(id: String): Exercise?
}

interface DialogueRepository {
  fun observeDialogues(domain: Domain): Flow<List<Dialogue>>
  suspend fun getDialogueById(id: String): Dialogue?
}

interface ConfusableRepository {
  fun observeConfusables(domain: Domain): Flow<List<ConfusablePair>>
  suspend fun getConfusableById(id: String): ConfusablePair?
}

interface ProgressRepository {
  fun observeDueReviews(domain: Domain? = null, nowMs: Long): Flow<List<ReviewItem>>
  fun observeProgress(itemKey: String): Flow<UserProgress?>
  suspend fun getDueCount(domain: Domain? = null, nowMs: Long): Int
  suspend fun recordReview(
    itemKey: String,
    itemType: ItemType,
    domain: Domain,
    grade: ReviewGrade,
    nowMs: Long,
    reviewDurationMs: Long = 0L
  ): UserProgress
}

interface ContentRepository {
  suspend fun seedContentIfEmpty(): Boolean
  suspend fun getContentVersion(): Int
}

interface SettingsRepository {
  fun observeToneColorsEnabled(): Flow<Boolean>
  suspend fun setToneColorsEnabled(enabled: Boolean)

  fun observeFuriganaMode(): Flow<String>
  suspend fun setFuriganaMode(mode: String)

  fun observeDarkTheme(): Flow<Boolean?>
  suspend fun setDarkTheme(darkTheme: Boolean?)

  fun observeUiLanguage(): Flow<String>
  suspend fun setUiLanguage(language: String)

  fun observeDailyNewLimit(): Flow<Int>
  suspend fun setDailyNewLimit(limit: Int)

  fun observeActiveChapters(): Flow<Set<Int>>
  suspend fun setActiveChapters(chapters: Set<Int>)

  fun observePlaybackSpeed(): Flow<Float>
  suspend fun setPlaybackSpeed(speed: Float)

  fun observeContentVersion(): Flow<Int>
  suspend fun setContentVersion(version: Int)

  fun observeResetAt(): Flow<Long>
  suspend fun setResetAt(timestamp: Long)
}

sealed interface MediaResolution {
  data class FilePath(val absolutePath: String) : MediaResolution
  data class AssetPath(val assetPath: String) : MediaResolution
  data class NotDownloaded(val packId: String) : MediaResolution
  data object NotFound : MediaResolution
}

interface MediaRepository {
  suspend fun resolve(ref: String): MediaResolution
}
