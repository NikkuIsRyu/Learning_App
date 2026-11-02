package tech.nikku.learningapp.feature.japanese.hub

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.*
import tech.nikku.learningapp.core.domain.repository.LexiconRepository

class FakeLexiconRepository : LexiconRepository {
  val chapterCountsFlow = MutableStateFlow<Map<Int, Int>>(emptyMap())

  override fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>> = chapterCountsFlow

  override fun observeWords(
    domain: Domain,
    chapters: Set<Int>,
    pos: PartOfSpeech?,
    verbGroup: VerbGroup?,
    jlpt: JlptLevel?,
    searchQuery: String
  ): Flow<List<Word>> = flowOf(emptyList())

  override suspend fun getWordById(id: String): Word? = null
  override suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word> = emptyList()
  override suspend fun searchWords(domain: Domain, query: String, limit: Int): List<Word> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class JapaneseHubViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepo: FakeLexiconRepository
  private lateinit var viewModel: JapaneseHubViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeRepo = FakeLexiconRepository()
  }

  @After
  fun teardown() {
    Dispatchers.resetMain()
  }

  @Test
  fun chaptersOneToFiftyGeneratedWithVerifiedFlagsAndCounts() = runTest(testDispatcher) {
    fakeRepo.chapterCountsFlow.value = mapOf(
      26 to 45,
      27 to 41,
      28 to 54,
      29 to 51,
      30 to 48,
      31 to 39,
      32 to 57
    )

    viewModel = JapaneseHubViewModel(fakeRepo)

    viewModel.uiState.test {
      var state = awaitItem()
      if (state.isLoading) {
        state = awaitItem()
      }
      assertFalse(state.isLoading)
      assertEquals(50, state.chapters.size)

      // Chapter 1 is unverified with 0 words
      val ch1 = state.chapters.first { it.chapter == 1 }
      assertFalse(ch1.isVerified)
      assertEquals(0, ch1.wordCount)

      // Chapters 26..32 are verified
      for (c in 26..32) {
        val item = state.chapters.first { it.chapter == c }
        assertTrue("Chapter $c must be verified", item.isVerified)
        assertTrue("Chapter $c must have words > 0", item.wordCount > 0)
      }

      assertEquals(45, state.chapters.first { it.chapter == 26 }.wordCount)
      assertEquals(57, state.chapters.first { it.chapter == 32 }.wordCount)

      // Chapter 33 is unverified
      val ch33 = state.chapters.first { it.chapter == 33 }
      assertFalse(ch33.isVerified)
      assertEquals(0, ch33.wordCount)
    }
  }
}
