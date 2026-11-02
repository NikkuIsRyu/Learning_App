package tech.nikku.learningapp.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.nikku.learningapp.core.data.local.dao.*
import tech.nikku.learningapp.core.data.local.entity.*
import tech.nikku.learningapp.core.domain.model.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DaoGuaranteesTest {

  private lateinit var db: LearningDatabase
  private lateinit var wordDao: WordDao
  private lateinit var grammarDao: GrammarDao
  private lateinit var dialogueDao: DialogueDao
  private lateinit var kanjiDao: KanjiDao
  private lateinit var progressDao: ProgressDao
  private lateinit var reviewLogDao: ReviewLogDao

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, LearningDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    wordDao = db.wordDao()
    grammarDao = db.grammarDao()
    dialogueDao = db.dialogueDao()
    kanjiDao = db.kanjiDao()
    progressDao = db.progressDao()
    reviewLogDao = db.reviewLogDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  // 1. upsertParent_keepsChildren
  @Test
  fun upsertParent_keepsChildren() = runBlocking {
    // 1a. Grammar rule upsert keeps examples
    val rule = createGrammarRule(id = "zh:g:01", titleEn = "Topic Prominence")
    grammarDao.upsertRules(listOf(rule))

    val example = GrammarExampleEntity(
      id = "zh:g:01#ex1",
      ruleId = "zh:g:01",
      text = "这个苹果很好吃",
      rubyMarkup = null,
      romanization = "Zhè ge píngguǒ hěn hǎochī",
      translationEn = "This apple is delicious",
      translationDe = "Dieser Apfel ist sehr lecker",
      breakdown = emptyList(),
      note = null,
      sortOrder = 1
    )
    grammarDao.upsertExamples(listOf(example))

    var ruleWithExamples = grammarDao.observeRuleWithExamples("zh:g:01").first()
    assertNotNull(ruleWithExamples)
    assertEquals(1, ruleWithExamples?.examples?.size)
    assertEquals("Topic Prominence", ruleWithExamples?.rule?.titleEn)

    // Update parent rule title via upsert
    val updatedRule = rule.copy(titleEn = "Topic Prominence (Updated)")
    grammarDao.upsertRules(listOf(updatedRule))

    ruleWithExamples = grammarDao.observeRuleWithExamples("zh:g:01").first()
    assertNotNull(ruleWithExamples)
    assertEquals("Topic Prominence (Updated)", ruleWithExamples?.rule?.titleEn)
    assertEquals("Child examples must be preserved across parent upsert", 1, ruleWithExamples?.examples?.size)
    assertEquals("zh:g:01#ex1", ruleWithExamples?.examples?.first()?.id)

    // 1b. Dialogue upsert keeps turns
    val dialogue = DialogueEntity(
      id = "zh:d:01",
      domain = Domain.CHINESE,
      chapter = 1,
      titleEn = "Greeting",
      titleDe = "Begrüßung",
      nativeTitle = "第一课 问候",
      topicEn = null,
      topicDe = null,
      descriptionEn = null,
      descriptionDe = null,
      audioRef = null,
      durationMs = null,
      speakers = emptyMap(),
      sortOrder = 1,
      contentVersion = 1
    )
    dialogueDao.upsertDialogues(listOf(dialogue))

    val turn = DialogueTurnEntity(
      dialogueId = "zh:d:01",
      idx = 0,
      speaker = "A",
      text = "你好！",
      rubyMarkup = null,
      romanization = "Nǐ hǎo!",
      translationEn = "Hello!",
      translationDe = "Hallo!",
      startMs = 0L,
      endMs = 1200L
    )
    dialogueDao.upsertTurns(listOf(turn))

    var dialogueWithTurns = dialogueDao.observeWithTurns("zh:d:01").first()
    assertNotNull(dialogueWithTurns)
    assertEquals(1, dialogueWithTurns?.turns?.size)

    // Update parent dialogue title via upsert
    val updatedDialogue = dialogue.copy(titleEn = "Greeting (Updated)")
    dialogueDao.upsertDialogues(listOf(updatedDialogue))

    dialogueWithTurns = dialogueDao.observeWithTurns("zh:d:01").first()
    assertNotNull(dialogueWithTurns)
    assertEquals("Greeting (Updated)", dialogueWithTurns?.dialogue?.titleEn)
    assertEquals("Child turns must be preserved across dialogue upsert", 1, dialogueWithTurns?.turns?.size)
    assertEquals("你好！", dialogueWithTurns?.turns?.first()?.text)
  }

  // 2. lexicon_ftsMatchesEnglishPrefixAndPinyinWithoutTones
  @Test
  fun lexicon_ftsMatchesEnglishPrefixAndPinyinWithoutTones() = runBlocking {
    val wordJa = createWord(
      id = "ja:w:tabemasu",
      domain = Domain.JAPANESE,
      headword = "食べます",
      reading = "たべます",
      meaningEn = "to eat, dine",
      meaningDe = "essen",
      searchText = "tabemasu taberu"
    )
    val wordZh = createWord(
      id = "zh:w:hao",
      domain = Domain.CHINESE,
      headword = "好",
      reading = "hǎo",
      meaningEn = "good, well",
      meaningDe = "gut",
      searchText = "hao hao3"
    )
    wordDao.upsertAll(listOf(wordJa, wordZh))

    // FTS matches English prefix "eat"
    val queryEat = LexiconQueryBuilder.build(LexiconFilter(query = "eat", domain = Domain.JAPANESE))
    val resultEat = wordDao.searchList(queryEat)
    assertEquals(1, resultEat.size)
    assertEquals("ja:w:tabemasu", resultEat[0].id)
    assertEquals("食べます", resultEat[0].headword)

    // FTS matches pinyin without tones "hao" matching "hǎo"
    val queryHao = LexiconQueryBuilder.build(LexiconFilter(query = "hao", domain = Domain.CHINESE))
    val resultHao = wordDao.searchList(queryHao)
    assertEquals(1, resultHao.size)
    assertEquals("zh:w:hao", resultHao[0].id)
    assertEquals("好", resultHao[0].headword)
  }

  // 3. lexicon_kanaLikeAndChapterChips
  @Test
  fun lexicon_kanaLikeAndChapterChips() = runBlocking {
    val w1 = createWord("w1", chapter = 26, pos = PartOfSpeech.VERB, verbGroup = VerbGroup.GODAN, headword = "行きます", reading = "いきます")
    val w2 = createWord("w2", chapter = 27, pos = PartOfSpeech.VERB, verbGroup = VerbGroup.GODAN, headword = "書きます", reading = "かきます")
    val w3 = createWord("w3", chapter = 27, pos = PartOfSpeech.VERB, verbGroup = VerbGroup.ICHIDAN, headword = "見ます", reading = "みます")
    val w4 = createWord("w4", chapter = 28, pos = PartOfSpeech.VERB, verbGroup = VerbGroup.GODAN, headword = "話します", reading = "はなします")
    val w5 = createWord("w5", chapter = 26, pos = PartOfSpeech.NOUN, verbGroup = null, headword = "本", reading = "ほん")

    wordDao.upsertAll(listOf(w1, w2, w3, w4, w5))

    // Filter by chapters {26, 27} + VERB + GODAN
    val filter = LexiconFilter(
      domain = Domain.JAPANESE,
      chapters = setOf(26, 27),
      pos = setOf(PartOfSpeech.VERB),
      verbGroups = setOf(VerbGroup.GODAN)
    )
    val query = LexiconQueryBuilder.build(filter)
    val results = wordDao.searchList(query)

    assertEquals(2, results.size)
    val ids = results.map { it.id }.toSet()
    assertEquals(setOf("w1", "w2"), ids)

    // Kana LIKE query test: "いき" matches 行きます (reading "いきます")
    val kanaQuery = LexiconQueryBuilder.build(LexiconFilter(query = "いき", domain = Domain.JAPANESE))
    val kanaResults = wordDao.searchList(kanaQuery)
    assertEquals(1, kanaResults.size)
    assertEquals("w1", kanaResults[0].id)
  }

  // 4. dueItems_excludesSuspendedAndNew
  @Test
  fun dueItems_excludesSuspendedAndNew() = runBlocking {
    val now = 2000L

    // Due and active (should be returned)
    val p1 = createProgress("item:1", state = SrsState.REVIEW, dueAt = 1000L, suspended = false, deleted = false)
    // NEW state (must be excluded)
    val p2 = createProgress("item:2", state = SrsState.NEW, dueAt = 1000L, suspended = false, deleted = false)
    // Suspended (must be excluded)
    val p3 = createProgress("item:3", state = SrsState.REVIEW, dueAt = 1000L, suspended = true, deleted = false)
    // Deleted (must be excluded)
    val p4 = createProgress("item:4", state = SrsState.REVIEW, dueAt = 1000L, suspended = false, deleted = true)
    // Future due > now (must be excluded)
    val p5 = createProgress("item:5", state = SrsState.REVIEW, dueAt = 3000L, suspended = false, deleted = false)
    // LEARNING state due <= now (should be returned)
    val p6 = createProgress("item:6", state = SrsState.LEARNING, dueAt = 1500L, suspended = false, deleted = false)

    progressDao.upsertAll(listOf(p1, p2, p3, p4, p5, p6))

    val due = progressDao.dueItems(domain = Domain.JAPANESE, now = now, limit = 10)
    assertEquals(2, due.size)
    val dueKeys = due.map { it.itemKey }
    assertEquals(listOf("item:1", "item:6"), dueKeys)

    val dueCount = progressDao.observeDueCount(domain = Domain.JAPANESE, now = now).first()
    assertEquals(2, dueCount)
  }

  // 5. markClean_isCompareAndSet
  @Test
  fun markClean_isCompareAndSet() = runBlocking {
    val key = "item:cas"
    val initial = createProgress(key, updatedAt = 1000L, dirty = true)
    progressDao.upsert(initial)

    // Simulate concurrent update before markClean is called
    val updated = initial.copy(updatedAt = 2000L)
    progressDao.upsert(updated)

    // Attempt CAS with stale expectedUpdatedAt = 1000L -> must fail (return 0)
    val rowsUpdatedStale = progressDao.markClean(key, expectedUpdatedAt = 1000L)
    assertEquals(0, rowsUpdatedStale)

    var current = progressDao.get(key)
    assertNotNull(current)
    assertTrue("Item must stay dirty after stale markClean", current!!.dirty)
    assertEquals(2000L, current.updatedAt)

    // Attempt CAS with current expectedUpdatedAt = 2000L -> must succeed (return 1)
    val rowsUpdatedCurrent = progressDao.markClean(key, expectedUpdatedAt = 2000L)
    assertEquals(1, rowsUpdatedCurrent)

    current = progressDao.get(key)
    assertNotNull(current)
    assertFalse("Item must be clean after valid markClean", current!!.dirty)
  }

  // 6. reviewLogs_insertIgnoreIsIdempotent
  @Test
  fun reviewLogs_insertIgnoreIsIdempotent() = runBlocking {
    val log = ReviewLogEntity(
      id = "log:uuid-1234",
      itemKey = "item:vocab:1",
      itemType = ItemType.WORD,
      domain = Domain.JAPANESE,
      reviewedAt = 1700000000000L,
      grade = ReviewGrade.GOOD,
      responseMs = 1500L,
      exerciseType = null,
      userAnswer = null,
      wasCorrect = true,
      prevState = SrsState.LEARNING,
      newState = SrsState.REVIEW,
      prevIntervalDays = 0.0,
      newIntervalDays = 1.0,
      prevEase = 2.5,
      newEase = 2.5,
      deviceId = "device-1",
      synced = false
    )

    // First insert succeeds
    val firstResult = reviewLogDao.insertAll(listOf(log))
    assertEquals(1, firstResult.size)
    assertTrue(firstResult[0] > 0)

    // Second insert with same primary key is ignored
    val secondResult = reviewLogDao.insertAll(listOf(log))
    assertEquals(1, secondResult.size)
    assertEquals(-1L, secondResult[0]) // OnConflictStrategy.IGNORE returns -1 for ignored inserts

    val logs = reviewLogDao.logsFor("item:vocab:1")
    assertEquals(1, logs.size)
    assertEquals("log:uuid-1234", logs[0].id)
  }

  // 7. kanjiWithCompounds_junctionResolves
  @Test
  fun kanjiWithCompounds_junctionResolves() = runBlocking {
    val kanji = KanjiEntity(
      literal = "日",
      id = "ja:kanji:日",
      strokeCount = 4,
      onReadings = listOf("ニチ", "ジツ"),
      kunReadings = listOf("ひ", "-び", "-か"),
      meaningsEn = listOf("day", "sun", "Japan"),
      meaningsDe = listOf("Tag", "Sonne", "Japan"),
      radical = "日",
      radicalNumber = 72,
      components = listOf("日"),
      strokePaths = listOf("M...", "M...", "M...", "M..."),
      introducedChapter = 1,
      jlpt = JlptLevel.N5,
      grade = 1,
      frequencyRank = 1,
      mnemonicEn = "Sun shining in the sky",
      contentVersion = 1
    )
    kanjiDao.upsertAll(listOf(kanji))

    val compoundWord1 = createWord("ja:w:nichiyoubi", headword = "日曜日", reading = "にちようび", meaningEn = "Sunday")
    val compoundWord2 = createWord("ja:w:nihon", headword = "日本", reading = "にほん", meaningEn = "Japan")
    wordDao.upsertAll(listOf(compoundWord1, compoundWord2))

    val crossRefs = listOf(
      KanjiCompoundCrossRef(kanji = "日", wordId = "ja:w:nichiyoubi", verified = true, sortOrder = 1),
      KanjiCompoundCrossRef(kanji = "日", wordId = "ja:w:nihon", verified = true, sortOrder = 2)
    )
    kanjiDao.upsertCompounds(crossRefs)

    val kanjiDetail = kanjiDao.observeDetail("日").first()
    assertNotNull(kanjiDetail)
    assertEquals("日", kanjiDetail?.kanji?.literal)
    assertEquals(2, kanjiDetail?.compounds?.size)
    val compoundIds = kanjiDetail?.compounds?.map { it.id }
    assertEquals(listOf("ja:w:nichiyoubi", "ja:w:nihon"), compoundIds)
  }

  // Helper factory methods
  private fun createWord(
    id: String,
    domain: Domain = Domain.JAPANESE,
    headword: String = "言葉",
    reading: String = "ことば",
    rubyMarkup: String? = null,
    meaningEn: String = "word",
    meaningDe: String? = "Wort",
    pos: PartOfSpeech = PartOfSpeech.NOUN,
    verbGroup: VerbGroup? = null,
    chapter: Int? = 1,
    searchText: String = reading
  ) = WordEntity(
    id = id,
    domain = domain,
    headword = headword,
    reading = reading,
    rubyMarkup = rubyMarkup,
    romanization = null,
    searchText = searchText,
    meaningEn = meaningEn,
    meaningDe = meaningDe,
    pos = pos,
    verbGroup = verbGroup,
    transitivity = null,
    pairWordId = null,
    dictionaryForm = null,
    dictionaryReading = null,
    chapter = chapter,
    orderInChapter = 1,
    jlpt = JlptLevel.N5,
    hskLevel = null,
    tags = emptyList(),
    source = ContentSource.MINNA,
    verified = true,
    errataNote = null,
    audioRef = null,
    contentVersion = 1
  )

  private fun createGrammarRule(
    id: String,
    domain: Domain = Domain.CHINESE,
    titleEn: String = "Rule Title"
  ) = GrammarRuleEntity(
    id = id,
    domain = domain,
    chapter = 1,
    sectionLabel = "1.1",
    category = "syntax",
    part = null,
    titleEn = titleEn,
    titleDe = "Regeltitel",
    pattern = "S + V + O",
    patternTokens = emptyList(),
    summaryEn = "Summary",
    summaryDe = null,
    nuancesEn = emptyList(),
    nuancesDe = emptyList(),
    bodyMarkdown = null,
    tags = emptyList(),
    verified = true,
    sortOrder = 1,
    contentVersion = 1
  )

  private fun createProgress(
    itemKey: String,
    domain: Domain = Domain.JAPANESE,
    state: SrsState = SrsState.REVIEW,
    dueAt: Long = 1000L,
    suspended: Boolean = false,
    deleted: Boolean = false,
    updatedAt: Long = 1000L,
    dirty: Boolean = true
  ) = UserProgressEntity(
    itemKey = itemKey,
    itemType = ItemType.WORD,
    domain = domain,
    state = state,
    dueAt = dueAt,
    suspended = suspended,
    deleted = deleted,
    updatedAt = updatedAt,
    updatedByDevice = "test-device",
    dirty = dirty
  )
}
