package tech.nikku.learningapp.core.data.content

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.nikku.learningapp.core.data.content.dto.*
import tech.nikku.learningapp.core.data.local.converter.ContentJson
import tech.nikku.learningapp.core.domain.model.PartOfSpeech
import java.io.File
import java.security.MessageDigest

class ContentValidationTest {

  private val json = ContentJson

  private lateinit var contentDir: File
  private lateinit var wordsMnn: List<WordDto>
  private lateinit var kanjiList: List<KanjiDto>
  private lateinit var compoundsList: List<KanjiCompoundDto>
  private lateinit var grammarJa: List<GrammarRuleDto>
  private lateinit var grammarZh: List<GrammarRuleDto>
  private lateinit var examplesZh: List<GrammarExampleDto>
  private lateinit var confusablesZh: List<ConfusablePairDto>
  private lateinit var dialoguesZh: List<DialogueDto>
  private lateinit var turnsZh: List<DialogueTurnDto>
  private lateinit var exercisesCloze: List<ExerciseDto>
  private lateinit var manifest: ManifestDto

  @Before
  fun setUp() {
    val candidates = listOf(
      File("src/main/assets/content"),
      File("core/data/src/main/assets/content"),
      File("../core/data/src/main/assets/content")
    )
    contentDir = candidates.firstOrNull { it.exists() && File(it, "manifest.json").exists() }
      ?: throw IllegalStateException("Could not find assets/content directory in ${candidates.map { it.absolutePath }}")

    wordsMnn = json.decodeFromString(File(contentDir, "ja/words_mnn_26_32.json").readText())
    kanjiList = json.decodeFromString(File(contentDir, "ja/kanji.json").readText())
    compoundsList = json.decodeFromString(File(contentDir, "ja/kanji_compounds.json").readText())
    grammarJa = json.decodeFromString(File(contentDir, "ja/grammar_26_32.json").readText())
    exercisesCloze = json.decodeFromString(File(contentDir, "ja/exercises_cloze_26_32.json").readText())

    grammarZh = json.decodeFromString(File(contentDir, "zh/grammar_rules.json").readText())
    examplesZh = json.decodeFromString(File(contentDir, "zh/grammar_examples.json").readText())
    confusablesZh = json.decodeFromString(File(contentDir, "zh/confusables.json").readText())
    dialoguesZh = json.decodeFromString(File(contentDir, "zh/dialogues.json").readText())
    turnsZh = json.decodeFromString(File(contentDir, "zh/dialogue_turns.json").readText())

    manifest = json.decodeFromString(File(contentDir, "manifest.json").readText())
  }

  // Invariant 1: All IDs unique, match regex, no '/'
  @Test
  fun validateIds() {
    val allIds = mutableSetOf<String>()

    fun checkId(id: String, prefix: String) {
      assertFalse("ID must not contain '/': $id", id.contains('/'))
      assertTrue("ID must match prefix $prefix: $id", id.startsWith(prefix))
      assertFalse("Duplicate ID found: $id", allIds.contains(id))
      allIds.add(id)
    }

    wordsMnn.forEach { checkId(it.id, "ja:mnn:") }
    kanjiList.forEach { checkId(it.id, "ja:kanji:") }
    grammarJa.forEach { checkId(it.id, "ja:mnn:") }
    grammarZh.forEach { checkId(it.id, "zh:g:") }
    confusablesZh.forEach { checkId(it.id, "zh:c:") }
    dialoguesZh.forEach { checkId(it.id, "zh:d:") }
    exercisesCloze.forEach { checkId(it.id, "ja:ex:cloze:") }

    // Dialogue turns must have unique (dialogueId, idx)
    val turnKeys = turnsZh.map { "${it.dialogueId}#${it.idx}" }.toSet()
    assertEquals(turnsZh.size, turnKeys.size)
  }

  // Invariant 2: MNN chapter counts: 26->45, 27->41, 28->54, 29->51, 30->48, 31->39, 32->57 (Sigma 335)
  @Test
  fun validateChapterCounts() {
    val counts = wordsMnn.groupBy { it.chapter }.mapValues { it.value.size }

    assertEquals(335, wordsMnn.size)
    assertEquals(45, counts[26])
    assertEquals(41, counts[27])
    assertEquals(54, counts[28])
    assertEquals(51, counts[29])
    assertEquals(48, counts[30])
    assertEquals(39, counts[31])
    assertEquals(57, counts[32])
  }

  // Invariant 3: pos == VERB => verbGroup != null && dictionaryForm != null
  @Test
  fun validateVerbInvariants() {
    val verbs = wordsMnn.filter { it.pos == PartOfSpeech.VERB }
    assertTrue("Must have conjugatable verbs", verbs.isNotEmpty())

    for (v in verbs) {
      assertNotNull("Verb ${v.id} (${v.headword}) must have verbGroup", v.verbGroup)
      assertNotNull("Verb ${v.id} (${v.headword}) must have dictionaryForm", v.dictionaryForm)
      assertNotNull("Verb ${v.id} (${v.headword}) must have dictionaryReading", v.dictionaryReading)
    }
  }

  // Invariant 4: Every pairWordId resolves and is symmetric
  @Test
  fun validateTransitivityPairs() {
    val wordsById = wordsMnn.associateBy { it.id }
    val pairedWords = wordsMnn.filter { it.pairWordId != null }
    assertTrue("Must have transitivity pairs", pairedWords.isNotEmpty())

    for (w in pairedWords) {
      val partnerId = w.pairWordId!!
      val partner = wordsById[partnerId]
      assertNotNull("Partner $partnerId for word ${w.id} must exist", partner)
      assertEquals(
        "Symmetric pairing failed: ${w.id} points to $partnerId, but partner points to ${partner?.pairWordId}",
        w.id,
        partner?.pairWordId
      )
    }
  }

  // Invariant 5: Ruby markup balanced and base non-empty
  @Test
  fun validateRubyMarkup() {
    val rubyRegex = Regex("""\{([^|{}]+)\|([^|{}]+)\}""")
    for (w in wordsMnn) {
      val markup = w.rubyMarkup ?: continue
      val matches = rubyRegex.findAll(markup).toList()
      for (m in matches) {
        val base = m.groupValues[1]
        val reading = m.groupValues[2]
        assertTrue("Ruby base must not be empty in '${markup}'", base.isNotEmpty())
        assertTrue("Ruby reading must not be empty in '${markup}'", reading.isNotEmpty())
      }
      // Check balanced braces
      val openCount = markup.count { it == '{' }
      val closeCount = markup.count { it == '}' }
      assertEquals("Unbalanced braces in ruby markup: $markup", openCount, closeCount)
    }
  }

  // Invariant 6: Every Chinese CharBreakdown.tone in 0..4
  @Test
  fun validateChineseTones() {
    for (ex in examplesZh) {
      for (b in ex.breakdown) {
        assertTrue("Tone must be in 0..4 for char ${b.char}: ${b.tone}", b.tone in 0..4)
      }
    }
  }

  // Invariant 7: Every dialogue turn: startMs < endMs, monotonic
  @Test
  fun validateDialogueTurns() {
    val turnsByDialogue = turnsZh.groupBy { it.dialogueId }
    for ((dialogueId, turns) in turnsByDialogue) {
      var prevStart = -1L
      for (t in turns) {
        assertNotNull("Turn must have startMs: ${t.dialogueId}#${t.idx}", t.startMs)
        assertNotNull("Turn must have endMs: ${t.dialogueId}#${t.idx}", t.endMs)
        assertTrue("startMs must be < endMs in ${t.dialogueId}#${t.idx}", t.startMs!! < t.endMs!!)
        assertTrue("Turns must be monotonic in ${t.dialogueId}", t.startMs!! >= prevStart)
        prevStart = t.startMs!!
      }
    }
  }

  // Invariant 8: Errata golden set
  @Test
  fun validateErrataGoldenSet() {
    val wordsById = wordsMnn.associateBy { it.id }

    // 1. L29 開きます［ドアが〜］: rubyMarkup reading = あ (not ひら)
    val l29w1 = wordsById["ja:mnn:29:w001"]
    assertNotNull(l29w1)
    assertTrue("L29 w001 rubyMarkup must contain {開|あ}: ${l29w1?.rubyMarkup}", l29w1?.rubyMarkup?.contains("{開|あ}") == true)
    assertFalse("L29 w001 rubyMarkup must not contain ひら: ${l29w1?.rubyMarkup}", l29w1?.rubyMarkup?.contains("ひら") == true)

    // 2. L29 #16 "to wipe": headword/reading = 拭きます / ふきます (not ふさます)
    val l29w16 = wordsById["ja:mnn:29:w016"]
    assertNotNull(l29w16)
    assertEquals("拭きます", l29w16?.headword)
    assertEquals("ふきます", l29w16?.reading)

    // 3. L29 #30 "After you": お先に どうぞ。 (not どうそ)
    val l29w30 = wordsById["ja:mnn:29:w030"]
    assertNotNull(l29w30)
    assertTrue("L29 w030 must start with お先に どうぞ: ${l29w30?.headword}", l29w30?.headword?.startsWith("お先に どうぞ") == true)

    // 4. L27 #23 "other": ほかの（他の） (not はかの)
    val l27w23 = wordsById["ja:mnn:27:w023"]
    assertNotNull(l27w23)
    assertTrue("L27 w023 must contain ほかの: ${l27w23?.headword}", l27w23?.headword?.contains("ほかの") == true)
    assertEquals("ほかの", l27w23?.reading)

    // 5. L32 #46: それは いけませんね。 (not いけませんえ)
    val l32w46 = wordsById["ja:mnn:32:w046"]
    assertNotNull(l32w46)
    assertEquals("それは いけませんね。", l32w46?.headword)

    // 6. L26 #30 片づきます: meaningEn contains "get tidied up"
    val l26w30 = wordsById["ja:mnn:26:w030"]
    assertNotNull(l26w30)
    assertTrue("L26 w030 meaningEn must contain 'get tidied up': ${l26w30?.meaningEn}", l26w30?.meaningEn?.contains("get tidied up") == true)

    // 7. L28 #35 娘: meaningEn = "daughter"
    val l28w35 = wordsById["ja:mnn:28:w035"]
    assertNotNull(l28w35)
    assertEquals("daughter", l28w35?.meaningEn)

    // 8. Exercise / grammar corpus forbidden strings
    val forbidden = listOf("しめって あります", "つかたら", "とかいの しゅうり", "あたらしー")
    for (rule in grammarJa) {
      val text = "${rule.titleEn} ${rule.bodyMarkdown}"
      for (f in forbidden) {
        assertFalse("Forbidden pattern '$f' found in grammar rule ${rule.id}", text.contains(f))
      }
    }
  }

  // Invariant 9: Manifest checksums match files on disk
  @Test
  fun validateManifestChecksums() {
    assertEquals(1, manifest.contentVersion)
    assertTrue(manifest.files.isNotEmpty())

    val digest = MessageDigest.getInstance("SHA-256")
    for (f in manifest.files) {
      val fileOnDisk = File(contentDir, f.path)
      assertTrue("Manifest file ${f.path} must exist on disk", fileOnDisk.exists())

      val bytes = fileOnDisk.readBytes()
      val hashBytes = digest.digest(bytes)
      val computedHash = hashBytes.joinToString("") { "%02x".format(it) }

      assertEquals("SHA-256 mismatch for ${f.path}", f.sha256, computedHash)
    }
  }

  // Invariant 10: Particle cloze exercises validation (>= 70 exercises, >= 10 per chapter, no errata)
  @Test
  fun validateClozeExercises() {
    assertTrue("Cloze exercises count must be >= 70: ${exercisesCloze.size}", exercisesCloze.size >= 70)
    val byChapter = exercisesCloze.groupBy { it.chapter }
    for (ch in 26..32) {
      val chList = byChapter[ch] ?: emptyList()
      assertTrue("Chapter $ch must have >= 10 exercises: ${chList.size}", chList.size >= 10)
    }

    val forbidden = listOf("しめって あります", "つかたら", "とかいの しゅうり", "あたらしー")
    for (ex in exercisesCloze) {
      assertEquals(tech.nikku.learningapp.core.domain.model.Domain.JAPANESE, ex.domain)
      assertEquals(tech.nikku.learningapp.core.domain.model.ExerciseType.PARTICLE_CLOZE, ex.type)
      val payload = ex.payload as? tech.nikku.learningapp.core.domain.model.ExercisePayload.ParticleCloze
      assertNotNull("Payload must be ParticleCloze for ${ex.id}", payload)
      val blanks = payload!!.segments.filterIsInstance<tech.nikku.learningapp.core.domain.model.ClozeSegment.Blank>()
      assertTrue("Must contain at least 1 blank in ${ex.id}", blanks.isNotEmpty())
      for (b in blanks) {
        assertTrue("Blank must have answers: ${ex.id}", b.answers.isNotEmpty())
        assertEquals(4, b.options.size)
        assertTrue("Answers must be in options: ${ex.id}", b.options.containsAll(b.answers))
      }
      val jsonStr = json.encodeToString(ExerciseDto.serializer(), ex)
      for (f in forbidden) {
        assertFalse("Forbidden errata '$f' found in ${ex.id}", jsonStr.contains(f))
      }
    }
  }
}
