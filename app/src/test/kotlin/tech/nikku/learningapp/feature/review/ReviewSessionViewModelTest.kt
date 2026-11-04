package tech.nikku.learningapp.feature.review

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.ItemType
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.KanjiRepository
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.core.domain.usecase.StartReviewSessionUseCase

class FakeReviewProgressRepository : ProgressRepository {
  val dueItems = mutableListOf<ReviewItem>()
  val recordedReviews = mutableListOf<ReviewGrade>()

  override fun observeDueReviews(domain: Domain?, nowMs: Long): Flow<List<ReviewItem>> {
    return flowOf(dueItems.filter { domain == null || it.progress.domain == domain })
  }

  override fun observeProgress(itemKey: String): Flow<UserProgress?> = flowOf(null)

  override suspend fun getDueCount(domain: Domain?, nowMs: Long): Int =
    dueItems.count { domain == null || it.progress.domain == domain }

  override suspend fun recordReview(
    itemKey: String,
    itemType: ItemType,
    domain: Domain,
    grade: ReviewGrade,
    nowMs: Long,
    reviewDurationMs: Long
  ): UserProgress {
    recordedReviews.add(grade)
    return UserProgress(
      itemKey = itemKey,
      itemType = itemType,
      domain = domain,
      state = if (grade == ReviewGrade.AGAIN) SrsState.LEARNING else SrsState.REVIEW
    )
  }
}

class FakeReviewSettingsRepository : SettingsRepository {
  var activeChapters = setOf(26, 27, 28, 29, 30, 31, 32)
  var dailyNewLimit = 5

  override fun observeToneColorsEnabled(): Flow<Boolean> = flowOf(true)
  override suspend fun setToneColorsEnabled(enabled: Boolean) {}
  override fun observeFuriganaMode(): Flow<String> = flowOf("FURIGANA")
  override suspend fun setFuriganaMode(mode: String) {}
  override fun observeDarkTheme(): Flow<Boolean?> = flowOf(null)
  override suspend fun setDarkTheme(darkTheme: Boolean?) {}
  override fun observeUiLanguage(): Flow<String> = flowOf("en")
  override suspend fun setUiLanguage(language: String) {}
  override fun observeDailyNewLimit(): Flow<Int> = flowOf(dailyNewLimit)
  override suspend fun setDailyNewLimit(limit: Int) { dailyNewLimit = limit }
  override fun observeActiveChapters(): Flow<Set<Int>> = flowOf(activeChapters)
  override suspend fun setActiveChapters(chapters: Set<Int>) { activeChapters = chapters }
  override fun observePlaybackSpeed(): Flow<Float> = flowOf(1.0f)
  override suspend fun setPlaybackSpeed(speed: Float) {}
  override fun observeContentVersion(): Flow<Int> = flowOf(1)
  override suspend fun setContentVersion(version: Int) {}
  override fun observeResetAt(): Flow<Long> = flowOf(0L)
  override suspend fun setResetAt(timestamp: Long) {}
}

class FakeReviewLexiconRepository : LexiconRepository {
  override fun observeWords(
    domain: Domain,
    chapters: Set<Int>,
    pos: PartOfSpeech?,
    verbGroup: VerbGroup?,
    jlpt: JlptLevel?,
    searchQuery: String
  ): Flow<List<Word>> = flowOf(emptyList())

  override fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>> = flowOf(emptyMap())
  override suspend fun getWordById(id: String): Word? = null
  override suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word> = emptyList()
  override suspend fun searchWords(domain: Domain, query: String, limit: Int): List<Word> = emptyList()
}

class FakeReviewKanjiRepository : KanjiRepository {
  override fun observeKanjiForChapter(chapter: Int): Flow<List<Kanji>> = flowOf(emptyList())
  override suspend fun getKanjiByLiteral(literal: String): Kanji? = null
  override suspend fun getAllKanji(): List<Kanji> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewSessionViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var progressRepo: FakeReviewProgressRepository
  private lateinit var settingsRepo: FakeReviewSettingsRepository
  private lateinit var lexiconRepo: FakeReviewLexiconRepository
  private lateinit var kanjiRepo: FakeReviewKanjiRepository

  private fun makeReviewItem(id: String, state: SrsState): ReviewItem {
    return ReviewItem(
      progress = UserProgress(
        itemKey = id,
        itemType = ItemType.WORD,
        domain = Domain.JAPANESE,
        state = state
      ),
      word = Word(
        id = id,
        domain = Domain.JAPANESE,
        headword = "単語$id",
        reading = "たんご",
        meaningEn = "word $id",
        pos = PartOfSpeech.NOUN,
        chapter = 26
      )
    )
  }

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    progressRepo = FakeReviewProgressRepository()
    settingsRepo = FakeReviewSettingsRepository()
    lexiconRepo = FakeReviewLexiconRepository()
    kanjiRepo = FakeReviewKanjiRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun interleaving_oneNewPerFourDue_andRespectsDailyNewLimit() = runTest(testDispatcher) {
    // 8 due items, 6 new items. Daily new limit is set to 2.
    settingsRepo.dailyNewLimit = 2

    val due1 = (1..8).map { makeReviewItem("due$it", SrsState.REVIEW) }
    val new1 = (1..6).map { makeReviewItem("new$it", SrsState.NEW) }
    progressRepo.dueItems.addAll(due1 + new1)

    val vm = ReviewSessionViewModel(progressRepo, settingsRepo, lexiconRepo, kanjiRepo, SavedStateHandle())
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertFalse(state.isFinished)

    // Expected: 8 due + 2 new (limited from 6 to 2) = 10 items total
    assertEquals(10, state.items.size)

    // Check 4-due to 1-new interleaving pattern:
    // Positions 0..3: due, Position 4: new1
    // Positions 5..8: due, Position 9: new2
    assertEquals("due1", state.items[0].progress.itemKey)
    assertEquals("due4", state.items[3].progress.itemKey)
    assertEquals("new1", state.items[4].progress.itemKey)
    assertEquals("due5", state.items[5].progress.itemKey)
    assertEquals("new2", state.items[9].progress.itemKey)
  }

  @Test
  fun revealAnswer_updatesState() = runTest(testDispatcher) {
    progressRepo.dueItems.add(makeReviewItem("w1", SrsState.REVIEW))
    val vm = ReviewSessionViewModel(progressRepo, settingsRepo, lexiconRepo, kanjiRepo, SavedStateHandle())
    advanceUntilIdle()

    assertFalse(vm.uiState.value.isAnswerRevealed)

    vm.onEvent(ReviewSessionEvent.RevealAnswer)
    assertTrue(vm.uiState.value.isAnswerRevealed)
  }

  @Test
  fun submitGradeGood_advancesCardAndRecordsReview() = runTest(testDispatcher) {
    progressRepo.dueItems.add(makeReviewItem("w1", SrsState.REVIEW))
    progressRepo.dueItems.add(makeReviewItem("w2", SrsState.REVIEW))

    val vm = ReviewSessionViewModel(progressRepo, settingsRepo, lexiconRepo, kanjiRepo, SavedStateHandle())
    advanceUntilIdle()

    vm.onEvent(ReviewSessionEvent.RevealAnswer)
    vm.onEvent(ReviewSessionEvent.SubmitGrade(ReviewGrade.GOOD))
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(1, state.currentIndex)
    assertEquals(1, state.stats.goodCount)
    assertEquals(1, progressRepo.recordedReviews.size)
    assertEquals(ReviewGrade.GOOD, progressRepo.recordedReviews.first())
    assertFalse(state.isAnswerRevealed)
    assertFalse(state.isFinished)
  }

  @Test
  fun submitGradeAgain_requeuesCardAtEndOfSession() = runTest(testDispatcher) {
    progressRepo.dueItems.add(makeReviewItem("w1", SrsState.REVIEW))
    progressRepo.dueItems.add(makeReviewItem("w2", SrsState.REVIEW))

    val vm = ReviewSessionViewModel(progressRepo, settingsRepo, lexiconRepo, kanjiRepo, SavedStateHandle())
    advanceUntilIdle()

    // 2 items initially
    assertEquals(2, vm.uiState.value.items.size)

    vm.onEvent(ReviewSessionEvent.RevealAnswer)
    vm.onEvent(ReviewSessionEvent.SubmitGrade(ReviewGrade.AGAIN))
    advanceUntilIdle()

    val state = vm.uiState.value
    // w1 was re-queued at the end, so total items is now 3
    assertEquals(3, state.items.size)
    assertEquals("w1", state.items[2].progress.itemKey)
    assertEquals(1, state.stats.againCount)
  }

  @Test
  fun allCardsGraded_marksSessionFinished() = runTest(testDispatcher) {
    progressRepo.dueItems.add(makeReviewItem("w1", SrsState.REVIEW))

    val vm = ReviewSessionViewModel(progressRepo, settingsRepo, lexiconRepo, kanjiRepo, SavedStateHandle())
    advanceUntilIdle()

    vm.onEvent(ReviewSessionEvent.RevealAnswer)
    vm.onEvent(ReviewSessionEvent.SubmitGrade(ReviewGrade.EASY))
    advanceUntilIdle()

    val state = vm.uiState.value
    assertTrue(state.isFinished)
    assertEquals(1, state.stats.easyCount)
  }
}
