package tech.nikku.learningapp.core.data.content

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tech.nikku.learningapp.core.data.content.dto.*
import tech.nikku.learningapp.core.data.local.LearningDatabase
import tech.nikku.learningapp.core.data.local.entity.UserProgressEntity
import tech.nikku.learningapp.core.domain.model.*
import tech.nikku.learningapp.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.InputStream

class FakeTestSettingsRepository(initialVersion: Int = 0) : SettingsRepository {
  private val _contentVersion = MutableStateFlow(initialVersion)
  private val _toneColors = MutableStateFlow(true)
  private val _furiganaMode = MutableStateFlow("FURIGANA")
  private val _darkTheme = MutableStateFlow<Boolean?>(null)
  private val _uiLanguage = MutableStateFlow("en")
  private val _dailyNewLimit = MutableStateFlow(15)
  private val _activeChapters = MutableStateFlow((26..32).toSet())
  private val _playbackSpeed = MutableStateFlow(1.0f)
  private val _resetAt = MutableStateFlow(0L)

  override fun observeToneColorsEnabled(): Flow<Boolean> = _toneColors.asStateFlow()
  override suspend fun setToneColorsEnabled(enabled: Boolean) { _toneColors.value = enabled }

  override fun observeFuriganaMode(): Flow<String> = _furiganaMode.asStateFlow()
  override suspend fun setFuriganaMode(mode: String) { _furiganaMode.value = mode }

  override fun observeDarkTheme(): Flow<Boolean?> = _darkTheme.asStateFlow()
  override suspend fun setDarkTheme(darkTheme: Boolean?) { _darkTheme.value = darkTheme }

  override fun observeUiLanguage(): Flow<String> = _uiLanguage.asStateFlow()
  override suspend fun setUiLanguage(language: String) { _uiLanguage.value = language }

  override fun observeDailyNewLimit(): Flow<Int> = _dailyNewLimit.asStateFlow()
  override suspend fun setDailyNewLimit(limit: Int) { _dailyNewLimit.value = limit }

  override fun observeActiveChapters(): Flow<Set<Int>> = _activeChapters.asStateFlow()
  override suspend fun setActiveChapters(chapters: Set<Int>) { _activeChapters.value = chapters }

  override fun observePlaybackSpeed(): Flow<Float> = _playbackSpeed.asStateFlow()
  override suspend fun setPlaybackSpeed(speed: Float) { _playbackSpeed.value = speed }

  override fun observeContentVersion(): Flow<Int> = _contentVersion.asStateFlow()
  override suspend fun setContentVersion(version: Int) { _contentVersion.value = version }

  override fun observeResetAt(): Flow<Long> = _resetAt.asStateFlow()
  override suspend fun setResetAt(timestamp: Long) { _resetAt.value = timestamp }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ContentSeederTest {

  private lateinit var context: Context
  private lateinit var db: LearningDatabase
  private lateinit var fakeSettings: FakeTestSettingsRepository
  private lateinit var seeder: ContentSeeder

  private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
  }

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, LearningDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    fakeSettings = FakeTestSettingsRepository(initialVersion = 0)
    seeder = ContentSeeder(context, db, fakeSettings)
  }

  @After
  fun teardown() {
    db.close()
  }

  private fun buildFixtureAssets(
    words: List<WordDto>,
    kanji: List<KanjiDto>,
    compounds: List<KanjiCompoundDto>,
    rules: List<GrammarRuleDto>,
    examples: List<GrammarExampleDto>,
    dialogues: List<DialogueDto>,
    turns: List<DialogueTurnDto>,
    confusables: List<ConfusablePairDto>,
    version: Int = 1
  ): Map<String, ByteArray> {
    val map = mutableMapOf<String, ByteArray>()

    val wordsBytes = json.encodeToString(words).toByteArray(Charsets.UTF_8)
    val kanjiBytes = json.encodeToString(kanji).toByteArray(Charsets.UTF_8)
    val compoundsBytes = json.encodeToString(compounds).toByteArray(Charsets.UTF_8)
    val rulesBytes = json.encodeToString(rules).toByteArray(Charsets.UTF_8)
    val examplesBytes = json.encodeToString(examples).toByteArray(Charsets.UTF_8)
    val dialoguesBytes = json.encodeToString(dialogues).toByteArray(Charsets.UTF_8)
    val turnsBytes = json.encodeToString(turns).toByteArray(Charsets.UTF_8)
    val confusablesBytes = json.encodeToString(confusables).toByteArray(Charsets.UTF_8)

    map["fixture/words.json"] = wordsBytes
    map["fixture/kanji.json"] = kanjiBytes
    map["fixture/compounds.json"] = compoundsBytes
    map["fixture/rules.json"] = rulesBytes
    map["fixture/examples.json"] = examplesBytes
    map["fixture/dialogues.json"] = dialoguesBytes
    map["fixture/turns.json"] = turnsBytes
    map["fixture/confusables.json"] = confusablesBytes

    val manifest = ManifestDto(
      contentVersion = version,
      files = listOf(
        ManifestFileDto("words.json", "word", ContentSeeder.computeSha256(wordsBytes)),
        ManifestFileDto("kanji.json", "kanji", ContentSeeder.computeSha256(kanjiBytes)),
        ManifestFileDto("compounds.json", "kanji_compound", ContentSeeder.computeSha256(compoundsBytes)),
        ManifestFileDto("rules.json", "grammar_rule", ContentSeeder.computeSha256(rulesBytes)),
        ManifestFileDto("examples.json", "grammar_example", ContentSeeder.computeSha256(examplesBytes)),
        ManifestFileDto("dialogues.json", "dialogue", ContentSeeder.computeSha256(dialoguesBytes)),
        ManifestFileDto("turns.json", "dialogue_turn", ContentSeeder.computeSha256(turnsBytes)),
        ManifestFileDto("confusables.json", "confusable_pair", ContentSeeder.computeSha256(confusablesBytes))
      )
    )

    map["fixture/manifest.json"] = json.encodeToString(manifest).toByteArray(Charsets.UTF_8)
    return map
  }

  private val sampleWords = listOf(
    WordDto(
      id = "ja:w:taberu",
      domain = Domain.JAPANESE,
      headword = "食べる",
      reading = "たべる",
      searchText = "taberu",
      meaningEn = "to eat",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.ICHIDAN,
      chapter = 26,
      source = ContentSource.MINNA,
      verified = true
    ),
    WordDto(
      id = "ja:w:nomu",
      domain = Domain.JAPANESE,
      headword = "飲む",
      reading = "のむ",
      searchText = "nomu",
      meaningEn = "to drink",
      pos = PartOfSpeech.VERB,
      verbGroup = VerbGroup.GODAN,
      chapter = 26,
      source = ContentSource.MINNA,
      verified = true
    )
  )

  private val sampleKanji = listOf(
    KanjiDto(
      literal = "食",
      id = "ja:kanji:食",
      strokeCount = 9,
      radical = "飠",
      meaningsEn = listOf("eat", "food")
    )
  )

  private val sampleCompounds = listOf(
    KanjiCompoundDto(
      kanji = "食",
      wordId = "ja:w:taberu",
      verified = true,
      sortOrder = 1
    )
  )

  private val sampleRules = listOf(
    GrammarRuleDto(
      id = "ja:mnn:26.1",
      domain = Domain.JAPANESE,
      chapter = 26,
      titleEn = "~んです",
      pattern = "V-plain + んです",
      summaryEn = "explanation",
      sortOrder = 1
    )
  )

  private val sampleExamples = listOf(
    GrammarExampleDto(
      id = "ja:mnn:26.1#ex1",
      ruleId = "ja:mnn:26.1",
      text = "食べるんです",
      translationEn = "I am eating (explanation)",
      sortOrder = 1
    )
  )

  private val sampleDialogues = listOf(
    DialogueDto(
      id = "zh:dia:1.1",
      domain = Domain.CHINESE,
      chapter = 1,
      titleEn = "Hello",
      nativeTitle = "问候",
      sortOrder = 1
    )
  )

  private val sampleTurns = listOf(
    DialogueTurnDto(
      dialogueId = "zh:dia:1.1",
      idx = 0,
      speaker = "A",
      text = "你好"
    ),
    DialogueTurnDto(
      dialogueId = "zh:dia:1.1",
      idx = 1,
      speaker = "B",
      text = "你好！"
    )
  )

  private val sampleConfusables = listOf(
    ConfusablePairDto(
      id = "zh:conf:er_liang",
      domain = Domain.CHINESE,
      chapter = 1,
      pairLabel = "二 vs 两",
      topicEn = "numbers",
      left = ConfusableSide(
        term = "二",
        romanization = "èr",
        conceptEn = "counting",
        formula = "二 + N",
        ruleEn = "Used for counting",
        correct = SentenceSample("二楼", "èr lóu", "second floor"),
        incorrect = WrongSample("两楼", "liǎng lóu", "Don't use 两 here")
      ),
      right = ConfusableSide(
        term = "两",
        romanization = "liǎng",
        conceptEn = "quantity",
        formula = "两 + MW + N",
        ruleEn = "Used for quantity",
        correct = SentenceSample("两个人", "liǎng gè rén", "two people"),
        incorrect = WrongSample("二个人", "èr gè rén", "Don't use 二 here")
      ),
      mnemonicEn = "two",
      sortOrder = 1
    )
  )

  @Test
  fun seedFromFixture_success() = runTest {
    val fixture = buildFixtureAssets(
      words = sampleWords,
      kanji = sampleKanji,
      compounds = sampleCompounds,
      rules = sampleRules,
      examples = sampleExamples,
      dialogues = sampleDialogues,
      turns = sampleTurns,
      confusables = sampleConfusables,
      version = 1
    )

    val result = seeder.seed("fixture/manifest.json") { path ->
      ByteArrayInputStream(fixture[path] ?: error("Asset not found: $path"))
    }

    assertTrue(result is SeedResult.Success)
    val success = result as SeedResult.Success
    assertEquals(1, success.version)
    assertEquals(2, success.wordsCount)
    assertEquals(1, success.kanjiCount)
    assertEquals(1, success.kanjiCompoundsCount)
    assertEquals(1, success.grammarRulesCount)
    assertEquals(1, success.grammarExamplesCount)
    assertEquals(1, success.dialoguesCount)
    assertEquals(2, success.dialogueTurnsCount)
    assertEquals(1, success.confusablePairsCount)

    // Verify database entities
    assertEquals(2, db.wordDao().observeChapter(Domain.JAPANESE, 26).first().size)
    assertNotNull(db.kanjiDao().observeDetail("食").first())
    assertEquals(1, db.kanjiDao().observeDetail("食").first()!!.compounds.size)
    assertNotNull(db.grammarDao().observeRuleWithExamples("ja:mnn:26.1").first())
    assertEquals(1, db.grammarDao().observeRuleWithExamples("ja:mnn:26.1").first()!!.examples.size)
    assertNotNull(db.dialogueDao().observeWithTurns("zh:dia:1.1").first())
    assertEquals(2, db.dialogueDao().observeWithTurns("zh:dia:1.1").first()!!.turns.size)
    assertEquals(1, db.confusableDao().observeAll(Domain.CHINESE).first().size)

    // Verify stored content version
    assertEquals(1, fakeSettings.observeContentVersion().first())
  }

  @Test
  fun secondRun_sameVersion_isNoOp() = runTest {
    val fixture = buildFixtureAssets(
      words = sampleWords,
      kanji = sampleKanji,
      compounds = sampleCompounds,
      rules = sampleRules,
      examples = sampleExamples,
      dialogues = sampleDialogues,
      turns = sampleTurns,
      confusables = sampleConfusables,
      version = 1
    )

    val openAsset: (String) -> InputStream = { path ->
      ByteArrayInputStream(fixture[path] ?: error("Asset not found: $path"))
    }

    val firstResult = seeder.seed("fixture/manifest.json", openAsset = openAsset)
    assertTrue(firstResult is SeedResult.Success)

    // Second run with same version
    val secondResult = seeder.seed("fixture/manifest.json", openAsset = openAsset)
    assertTrue(secondResult is SeedResult.AlreadyUpToDate)
    assertEquals(1, (secondResult as SeedResult.AlreadyUpToDate).version)
  }

  @Test
  fun version2_removesWord_preservesUserProgress() = runTest {
    val fixtureV1 = buildFixtureAssets(
      words = sampleWords,
      kanji = sampleKanji,
      compounds = sampleCompounds,
      rules = sampleRules,
      examples = sampleExamples,
      dialogues = sampleDialogues,
      turns = sampleTurns,
      confusables = sampleConfusables,
      version = 1
    )

    seeder.seed("fixture/manifest.json") { path ->
      ByteArrayInputStream(fixtureV1[path] ?: error("Asset not found: $path"))
    }

    // Insert user progress for word "nomu"
    val progressEntity = UserProgressEntity(
      itemKey = "ja:word:ja:w:nomu",
      itemType = ItemType.WORD,
      domain = Domain.JAPANESE,
      state = SrsState.REVIEW,
      dueAt = 100_000L,
      updatedAt = 50_000L,
      updatedByDevice = "device_test"
    )
    db.progressDao().upsert(progressEntity)

    // In version 2, "nomu" is removed, only "taberu" remains
    val wordsV2 = listOf(sampleWords.first()) // only taberu
    val fixtureV2 = buildFixtureAssets(
      words = wordsV2,
      kanji = sampleKanji,
      compounds = sampleCompounds,
      rules = sampleRules,
      examples = sampleExamples,
      dialogues = sampleDialogues,
      turns = sampleTurns,
      confusables = sampleConfusables,
      version = 2
    )

    val resultV2 = seeder.seed("fixture/manifest.json") { path ->
      ByteArrayInputStream(fixtureV2[path] ?: error("Asset not found: $path"))
    }

    assertTrue(resultV2 is SeedResult.Success)
    assertEquals(2, (resultV2 as SeedResult.Success).version)
    assertEquals(1, resultV2.wordsCount)

    // Stale word "nomu" was pruned
    assertNull(db.wordDao().observe("ja:w:nomu").first())
    assertNotNull(db.wordDao().observe("ja:w:taberu").first())

    // Crucial: UserProgressEntity for "nomu" was untouched by deleteStale!
    val preservedProgress = db.progressDao().observe("ja:word:ja:w:nomu").first()
    assertNotNull("User progress must be preserved even if content word is withdrawn", preservedProgress)
    assertEquals(SrsState.REVIEW, preservedProgress!!.state)
    assertEquals(100_000L, preservedProgress.dueAt)
  }

  @Test
  fun checksumMismatch_failsSeeding() = runTest {
    val fixture = buildFixtureAssets(
      words = sampleWords,
      kanji = sampleKanji,
      compounds = sampleCompounds,
      rules = sampleRules,
      examples = sampleExamples,
      dialogues = sampleDialogues,
      turns = sampleTurns,
      confusables = sampleConfusables,
      version = 1
    ).toMutableMap()

    // Tamper with the words file
    fixture["fixture/words.json"] = "[]".toByteArray(Charsets.UTF_8)

    val result = seeder.seed("fixture/manifest.json") { path ->
      ByteArrayInputStream(fixture[path] ?: error("Asset not found: $path"))
    }

    assertTrue(result is SeedResult.Error)
    val error = result as SeedResult.Error
    assertTrue(error.message.contains("Checksum mismatch"))
  }

  @Test
  fun seedRealAssets_allCountsMatchAndNoFkViolations() = runTest {
    // Seeds the bundled assets/content/manifest.json
    val result = seeder.seed(manifestPath = "content/manifest.json", force = true)

    assertTrue("Seeding real assets should succeed, was: $result", result is SeedResult.Success)
    val success = result as SeedResult.Success

    assertEquals(335, success.wordsCount)
    assertEquals(287, success.kanjiCount)
    assertEquals(437, success.kanjiCompoundsCount)
    assertEquals(53, success.grammarRulesCount) // 27 JA + 26 ZH
    assertEquals(31, success.grammarExamplesCount)
    assertEquals(18, success.dialoguesCount)
    assertEquals(162, success.dialogueTurnsCount)
    assertEquals(6, success.confusablePairsCount)

    // Verify Room database has all 335 Japanese words
    assertEquals(335, db.wordDao().count(Domain.JAPANESE))
  }
}
