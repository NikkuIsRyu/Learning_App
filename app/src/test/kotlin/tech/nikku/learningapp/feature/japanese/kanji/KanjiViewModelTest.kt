package tech.nikku.learningapp.feature.japanese.kanji

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.JlptLevel
import tech.nikku.learningapp.core.domain.model.Kanji
import tech.nikku.learningapp.core.domain.model.KanjiCompound
import tech.nikku.learningapp.core.domain.repository.KanjiRepository

class FakeKanjiRepository : KanjiRepository {
  val kanjiList = mutableListOf<Kanji>()

  override fun observeKanjiForChapter(chapter: Int): Flow<List<Kanji>> {
    return flowOf(kanjiList.filter { it.chapter == chapter })
  }

  override suspend fun getKanjiByLiteral(literal: String): Kanji? {
    return kanjiList.firstOrNull { it.literal == literal }
  }

  override suspend fun getAllKanji(): List<Kanji> {
    return kanjiList.toList()
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class KanjiViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepo: FakeKanjiRepository

  private val kanjiKai = Kanji(
    literal = "開",
    onReadings = listOf("カイ"),
    kunReadings = listOf("あ.く", "ひら.く"),
    meaningsEn = listOf("open", "unfold"),
    strokeCount = 12,
    chapter = 29,
    jlpt = JlptLevel.N4,
    compounds = listOf(
      KanjiCompound(
        kanjiLiteral = "開",
        compound = "開く",
        reading = "ひらく",
        meaningEn = "to open"
      )
    )
  )

  private val kanjiHei = Kanji(
    literal = "閉",
    onReadings = listOf("ヘイ"),
    kunReadings = listOf("し.まる", "と.じる"),
    meaningsEn = listOf("closed", "shut"),
    strokeCount = 11,
    chapter = 29,
    jlpt = JlptLevel.N4
  )

  private val kanjiShita = Kanji(
    literal = "下",
    onReadings = listOf("カ", "ゲ"),
    kunReadings = listOf("した", "しも"),
    meaningsEn = listOf("below", "down"),
    strokeCount = 3,
    chapter = 26,
    jlpt = JlptLevel.N5
  )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepo = FakeKanjiRepository().apply {
      kanjiList.addAll(listOf(kanjiKai, kanjiHei, kanjiShita))
    }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun chapterKanjiViewModel_loadsChapterKanji() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("chapter" to 29))
    val vm = ChapterKanjiViewModel(fakeRepo, handle)
    vm.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals(29, state.selectedChapter)
    assertEquals(2, state.kanjiList.size)
    assertTrue(state.kanjiList.any { it.literal == "開" })
    assertTrue(state.kanjiList.any { it.literal == "閉" })
  }

  @Test
  fun chapterKanjiViewModel_switchChapter_updatesList() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("chapter" to 29))
    val vm = ChapterKanjiViewModel(fakeRepo, handle)
    vm.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    vm.onEvent(ChapterKanjiEvent.SelectChapter(26))
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals(26, state.selectedChapter)
    assertEquals(1, state.kanjiList.size)
    assertEquals("下", state.kanjiList.first().literal)
  }

  @Test
  fun chapterKanjiViewModel_searchQuery_filtersCorrectly() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("chapter" to 29))
    val vm = ChapterKanjiViewModel(fakeRepo, handle)
    vm.uiState.launchIn(backgroundScope)
    advanceUntilIdle()

    vm.onEvent(ChapterKanjiEvent.UpdateSearch("open"))
    advanceUntilIdle()

    val state = vm.uiState.value
    assertEquals("open", state.searchQuery)
    assertEquals(1, state.kanjiList.size)
    assertEquals("開", state.kanjiList.first().literal)
  }

  @Test
  fun kanjiDetailViewModel_loadsKanjiSuccessfully() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("kanjiLiteral" to "開"))
    val vm = KanjiDetailViewModel(fakeRepo, handle)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertNotNull(state.kanji)
    assertEquals("開", state.kanji?.literal)
    assertEquals(1, state.kanji?.compounds?.size)
    assertEquals("開く", state.kanji?.compounds?.first()?.compound)
  }

  @Test
  fun kanjiDetailViewModel_notFound_setsErrorMessage() = runTest(testDispatcher) {
    val handle = SavedStateHandle(mapOf("kanjiLiteral" to "未知"))
    val vm = KanjiDetailViewModel(fakeRepo, handle)
    advanceUntilIdle()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.kanji)
    assertNotNull(state.errorMessage)
  }
}
