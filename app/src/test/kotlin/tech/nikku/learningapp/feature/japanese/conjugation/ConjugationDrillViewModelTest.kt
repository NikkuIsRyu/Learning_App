package tech.nikku.learningapp.feature.japanese.conjugation

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
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.ReviewGrade
import tech.nikku.learningapp.core.domain.model.ReviewItem
import tech.nikku.learningapp.core.domain.model.SrsState
import tech.nikku.learningapp.core.domain.model.UserProgress
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository
import tech.nikku.learningapp.core.domain.repository.ProgressRepository
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import tech.nikku.learningapp.core.domain.usecase.GradeAnswerUseCase

class FakeProgressRepository : ProgressRepository {
  var recordedItemKey: String? = null
  var recordedGrade: ReviewGrade? = null
  var reviewCount: Int = 0

  override fun observeDueReviews(domain: Domain?, nowMs: Long): Flow<List<ReviewItem>> = flowOf(emptyList())
  override fun observeProgress(itemKey: String): Flow<UserProgress?> = flowOf(null)
  override suspend fun getDueCount(domain: Domain?, nowMs: Long): Int = 0

  override suspend fun recordReview(
    itemKey: String,
    itemType: ItemType,
    domain: Domain,
    grade: ReviewGrade,
    nowMs: Long,
    reviewDurationMs: Long
  ): UserProgress {
    recordedItemKey = itemKey
    recordedGrade = grade
    reviewCount++
    return UserProgress(
      itemKey = itemKey,
      itemType = itemType,
      domain = domain,
      state = SrsState.REVIEW
    )
  }
}

class FakeSettingsRepository : SettingsRepository {
  override fun observeToneColorsEnabled(): Flow<Boolean> = flowOf(true)
  override suspend fun setToneColorsEnabled(enabled: Boolean) {}
  override fun observeFuriganaMode(): Flow<String> = flowOf("ALWAYS")
  override suspend fun setFuriganaMode(mode: String) {}
  override fun observeDarkTheme(): Flow<Boolean?> = flowOf(null)
  override suspend fun setDarkTheme(darkTheme: Boolean?) {}
  override fun observeUiLanguage(): Flow<String> = flowOf("en")
  override suspend fun setUiLanguage(language: String) {}
  override fun observeDailyNewLimit(): Flow<Int> = flowOf(20)
  override suspend fun setDailyNewLimit(limit: Int) {}
  override fun observeActiveChapters(): Flow<Set<Int>> = flowOf(setOf(26))
  override suspend fun setActiveChapters(chapters: Set<Int>) {}
  override fun observePlaybackSpeed(): Flow<Float> = flowOf(1.0f)
  override suspend fun setPlaybackSpeed(speed: Float) {}
  override fun observeContentVersion(): Flow<Int> = flowOf(1)
  override suspend fun setContentVersion(version: Int) {}
  override fun observeResetAt(): Flow<Long> = flowOf(0L)
  override suspend fun setResetAt(timestamp: Long) {}
}

class FakeDrillLexiconRepository(val testWord: Word) : LexiconRepository {
  override fun observeWords(
    domain: Domain,
    chapters: Set<Int>,
    pos: PartOfSpeech?,
    verbGroup: VerbGroup?,
    jlpt: JlptLevel?,
    searchQuery: String
  ): Flow<List<Word>> = flowOf(listOf(testWord))

  override fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>> = flowOf(mapOf(26 to 1))
  override suspend fun getWordById(id: String): Word? = if (id == testWord.id) testWord else null
  override suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word> = listOf(testWord)
  override suspend fun searchWords(domain: Domain, query: String, limit: Int): List<Word> = listOf(testWord)
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConjugationDrillViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeProgressRepo: FakeProgressRepository
  private lateinit var fakeSettingsRepo: FakeSettingsRepository
  private lateinit var fakeLexiconRepo: FakeDrillLexiconRepository
  private lateinit var gradeAnswerUseCase: GradeAnswerUseCase

  private val sampleWord = Word(
    id = "w_kaku_01",
    domain = Domain.JAPANESE,
    headword = "書く",
    reading = "かく",
    meaningEn = "write",
    pos = PartOfSpeech.VERB,
    verbGroup = VerbGroup.GODAN,
    chapter = 26
  )

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeProgressRepo = FakeProgressRepository()
    fakeSettingsRepo = FakeSettingsRepository()
    fakeLexiconRepo = FakeDrillLexiconRepository(sampleWord)
    gradeAnswerUseCase = GradeAnswerUseCase(fakeProgressRepo)
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
  }

  @Test
  fun testCorrectAnswerRecordsGoodLog() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("wordId" to sampleWord.id, "targetForm" to "TA"))
    val viewModel = ConjugationDrillViewModel(fakeLexiconRepo, fakeSettingsRepo, gradeAnswerUseCase, handle)
    advanceUntilIdle()

    val card = viewModel.uiState.value.currentCard
    assertNotNull(card)
    assertEquals(VerbForm.TA, card!!.targetForm)

    // User inputs correct past form "書いた"
    viewModel.onEvent(ConjugationDrillEvent.UpdateInput("書いた"))
    viewModel.onEvent(ConjugationDrillEvent.Submit)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.result)
    assertTrue("Result should be correct", state.result!!.isCorrect)
    assertEquals(1, state.correctCount)
    assertEquals(ReviewGrade.GOOD, fakeProgressRepo.recordedGrade)
    assertEquals("conj:w_kaku_01:TA", fakeProgressRepo.recordedItemKey)
  }

  @Test
  fun testWrongAnswerRecordsAgainWithExplanation() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("wordId" to sampleWord.id, "targetForm" to "TA"))
    val viewModel = ConjugationDrillViewModel(fakeLexiconRepo, fakeSettingsRepo, gradeAnswerUseCase, handle)
    advanceUntilIdle()

    // User inputs wrong form "書いて" (te-form instead of ta-form)
    viewModel.onEvent(ConjugationDrillEvent.UpdateInput("書いて"))
    viewModel.onEvent(ConjugationDrillEvent.Submit)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.result)
    assertFalse("Result should be marked wrong", state.result!!.isCorrect)
    assertNotNull("Diff must be present", state.result!!.diff)
    assertEquals("て", state.result!!.diff!!.userEnding)
    assertEquals("た", state.result!!.diff!!.expectedEnding)
    assertNotNull("Explanation must be present", state.result!!.ruleExplanation)
    assertTrue(state.result!!.ruleExplanation!!.contains("past ta-form"))

    assertEquals(0, state.correctCount)
    assertEquals(ReviewGrade.AGAIN, fakeProgressRepo.recordedGrade)
  }

  @Test
  fun testHintedAnswerRecordsHardLog() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("wordId" to sampleWord.id, "targetForm" to "TA"))
    val viewModel = ConjugationDrillViewModel(fakeLexiconRepo, fakeSettingsRepo, gradeAnswerUseCase, handle)
    advanceUntilIdle()

    // User requests hint first
    viewModel.onEvent(ConjugationDrillEvent.RequestHint)
    assertTrue(viewModel.uiState.value.hintShown)

    // Then inputs correct answer
    viewModel.onEvent(ConjugationDrillEvent.UpdateInput("書いた"))
    viewModel.onEvent(ConjugationDrillEvent.Submit)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertNotNull(state.result)
    assertTrue("Result should be correct", state.result!!.isCorrect)
    assertEquals(ReviewGrade.HARD, fakeProgressRepo.recordedGrade)
  }
}
