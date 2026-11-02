package tech.nikku.learningapp.feature.japanese.lexicon

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.ContentSource
import tech.nikku.learningapp.core.domain.model.Domain
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import tech.nikku.learningapp.core.domain.model.Transitivity
import tech.nikku.learningapp.core.domain.model.VerbGroup
import tech.nikku.learningapp.core.domain.model.Word
import tech.nikku.learningapp.core.domain.repository.LexiconRepository

class FakeLexiconRepository : LexiconRepository {
  var wordsList = mutableListOf<Word>()

  override fun observeWords(
    domain: Domain,
    chapters: Set<Int>,
    pos: PartOfSpeech?,
    verbGroup: VerbGroup?,
    jlpt: JlptLevel?,
    searchQuery: String
  ): Flow<List<Word>> {
    var result = wordsList.filter { it.domain == domain }
    if (chapters.isNotEmpty()) {
      result = result.filter { it.chapter != null && chapters.contains(it.chapter) }
    }
    if (pos != null) {
      result = result.filter { it.pos == pos }
    }
    if (verbGroup != null) {
      result = result.filter { it.verbGroup == verbGroup }
    }
    if (jlpt != null) {
      result = result.filter { it.jlpt == jlpt }
    }
    if (searchQuery.isNotBlank()) {
      result = result.filter {
        it.headword.contains(searchQuery) || it.reading.contains(searchQuery) || (it.meaningEn?.contains(searchQuery) == true)
      }
    }
    return flowOf(result)
  }

  override fun observeChapterCounts(domain: Domain): Flow<Map<Int, Int>> = flowOf(emptyMap())
  override suspend fun getWordById(id: String): Word? = wordsList.firstOrNull { it.id == id }
  override suspend fun getWordsForChapter(domain: Domain, chapter: Int): List<Word> =
    wordsList.filter { it.domain == domain && it.chapter == chapter }
  override suspend fun searchWords(domain: Domain, query: String, limit: Int): List<Word> = wordsList
}

@OptIn(ExperimentalCoroutinesApi::class)
class LexiconViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepo: FakeLexiconRepository

  private val word1 = Word(
    id = "ja:mnn:29:w001",
    domain = Domain.JAPANESE,
    headword = "開きます",
    reading = "あきます",
    rubyMarkup = "{開|あ}きます",
    meaningEn = "to open (intransitive)",
    pos = PartOfSpeech.VERB,
    verbGroup = VerbGroup.GODAN,
    transitivity = Transitivity.INTRANSITIVE,
    chapter = 29,
    jlpt = JlptLevel.N4,
    source = ContentSource.MINNA,
    verified = true
  )

  private val word2 = Word(
    id = "ja:mnn:29:w002",
    domain = Domain.JAPANESE,
    headword = "開けます",
    reading = "あけます",
    rubyMarkup = "{開|あ}けます",
    meaningEn = "to open (transitive)",
    pos = PartOfSpeech.VERB,
    verbGroup = VerbGroup.ICHIDAN,
    transitivity = Transitivity.TRANSITIVE,
    chapter = 29,
    jlpt = JlptLevel.N5,
    source = ContentSource.MINNA,
    verified = true
  )

  private val word3 = Word(
    id = "ja:mnn:26:w001",
    domain = Domain.JAPANESE,
    headword = "見ます",
    reading = "みます",
    meaningEn = "to look, examine",
    pos = PartOfSpeech.VERB,
    verbGroup = VerbGroup.ICHIDAN,
    transitivity = Transitivity.TRANSITIVE,
    chapter = 26,
    jlpt = JlptLevel.N4,
    source = ContentSource.MINNA,
    verified = false
  )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepo = FakeLexiconRepository().apply {
      wordsList.addAll(listOf(word1, word2, word3))
    }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialLoad_returnsAllWords() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(3, state.words.size)
  }

  @Test
  fun toggleChapter_filtersWordsToChapter() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.ToggleChapter(29))
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.selectedChapters.contains(29))
    assertEquals(2, state.words.size)
    assertTrue(state.words.all { it.chapter == 29 })
  }

  @Test
  fun filterTransitivity_filtersIntransitiveOrTransitive() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.SelectTransitivity(Transitivity.INTRANSITIVE))
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(1, state.words.size)
    assertEquals("ja:mnn:29:w001", state.words.first().id)
  }

  @Test
  fun filterVerifiedOnly_excludesUnverifiedWords() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.ToggleVerifiedOnly)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.verifiedOnly)
    assertEquals(2, state.words.size)
    assertTrue(state.words.none { it.id == "ja:mnn:26:w001" })
  }

  @Test
  fun updateSearchQuery_debouncesAndFilters() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.UpdateSearchQuery("開"))
    // Before debounce completes
    advanceTimeBy(100L)
    // After debounce (250 ms total)
    advanceTimeBy(200L)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals("開", state.searchQuery)
    assertEquals(2, state.words.size)
  }

  @Test
  fun clearFilters_resetsAllFilterStates() = runTest(testDispatcher) {
    val viewModel = LexiconViewModel(fakeRepo)
    viewModel.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.ToggleChapter(29))
    viewModel.onEvent(LexiconEvent.SelectTransitivity(Transitivity.TRANSITIVE))
    viewModel.onEvent(LexiconEvent.ToggleVerifiedOnly)
    advanceUntilIdle()

    viewModel.onEvent(LexiconEvent.ClearFilters)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.selectedChapters.isEmpty())
    assertNull(state.selectedTransitivity)
    assertFalse(state.verifiedOnly)
    assertEquals(3, state.words.size)
  }
}
