package tech.nikku.learningapp.core.domain.engine.conjugation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup

class ConjugatorGoldenTest {

  data class GoldenCase(
    val lemma: String,
    val reading: String,
    val group: VerbGroup,
    val target: VerbForm,
    val expectedSurface: String,
    val expectedReading: String,
    val expectedOverride: String? = null
  )

  @Test
  fun testGodanVerbsAcrossAllForms() {
    val cases = listOf(
      // 書く (GODAN - ku)
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.MASU, "書きます", "かきます"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.DICTIONARY, "書く", "かく"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.NAI, "書かない", "かかない"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.TA, "書いた", "かいた"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.TE, "書いて", "かいて"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.POTENTIAL, "書ける", "かける"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.VOLITIONAL, "書こう", "かこう"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.PASSIVE, "書かれる", "かかれる"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.CAUSATIVE, "書かせる", "かかせる"),
      GoldenCase("書く", "かく", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE, "書かせられる", "かかせられる", "書かされる"),

      // 話す (GODAN - su)
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.MASU, "話します", "はなします"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.DICTIONARY, "話す", "はなす"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.NAI, "話さない", "はなさない"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.TA, "話した", "はなした"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.TE, "話して", "はなして"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.POTENTIAL, "話せる", "はなせる"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.VOLITIONAL, "話そう", "はなそう"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.PASSIVE, "話される", "はなされる"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.CAUSATIVE, "話させる", "はなさせる"),
      GoldenCase("話す", "はなす", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE, "話させられる", "はなさせられる"),

      // 買う (GODAN - u -> wa)
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.MASU, "買います", "かいます"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.DICTIONARY, "買う", "かう"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.NAI, "買わない", "かわない"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.TA, "買った", "かった"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.TE, "買って", "かって"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.POTENTIAL, "買える", "かえる"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.VOLITIONAL, "買おう", "かおう"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.PASSIVE, "買われる", "かわれる"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.CAUSATIVE, "買わせる", "かわせる"),
      GoldenCase("買う", "かう", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE, "買わせられる", "かわせられる", "買わされる"),

      // 待つ (GODAN - tsu)
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.MASU, "待ちます", "まちます"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.DICTIONARY, "待つ", "まつ"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.NAI, "待たない", "またない"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.TA, "待った", "まった"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.TE, "待って", "まって"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.POTENTIAL, "待てる", "まてる"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.VOLITIONAL, "待とう", "まとう"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.PASSIVE, "待たれる", "またれる"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.CAUSATIVE, "待たせる", "またせる"),
      GoldenCase("待つ", "まつ", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE, "待たせられる", "またせられる", "待たされる"),

      // 飲む (GODAN - mu)
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.MASU, "飲みます", "のみます"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.DICTIONARY, "飲む", "のむ"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.NAI, "飲まない", "のまない"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.TA, "飲んだ", "のんだ"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.TE, "飲んで", "のんで"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.POTENTIAL, "飲める", "のめる"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.VOLITIONAL, "飲もう", "のもう"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.PASSIVE, "飲まれる", "のまれる"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.CAUSATIVE, "飲ませる", "のませる"),
      GoldenCase("飲む", "のむ", VerbGroup.GODAN, VerbForm.CAUSATIVE_PASSIVE, "飲ませられる", "のませられる", "飲まされる")
    )

    for (c in cases) {
      val result = Conjugator.conjugate(c.lemma, c.reading, c.group, c.target)
      assertEquals("Surface mismatch for ${c.lemma} ${c.target}", c.expectedSurface, result.surface)
      assertEquals("Reading mismatch for ${c.lemma} ${c.target}", c.expectedReading, result.reading)
      if (c.expectedOverride != null) {
        assertTrue(
          "Expected override ${c.expectedOverride} missing for ${c.lemma} ${c.target}",
          result.acceptedOverrides.contains(c.expectedOverride)
        )
      }
    }
  }

  @Test
  fun testIchidanVerbsAcrossAllForms() {
    val verbs = listOf(
      "食べる" to "たべる",
      "見る" to "みる",
      "寝る" to "ねる"
    )

    for ((lemma, reading) in verbs) {
      val stem = lemma.dropLast(1)
      val rStem = reading.dropLast(1)

      // MASU
      val masu = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.MASU)
      assertEquals(stem + "ます", masu.surface)
      assertEquals(rStem + "ます", masu.reading)

      // DICTIONARY
      val dict = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.DICTIONARY)
      assertEquals(lemma, dict.surface)
      assertEquals(reading, dict.reading)

      // NAI
      val nai = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.NAI)
      assertEquals(stem + "ない", nai.surface)
      assertEquals(rStem + "ない", nai.reading)

      // TA
      val ta = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.TA)
      assertEquals(stem + "た", ta.surface)
      assertEquals(rStem + "た", ta.reading)

      // TE
      val te = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.TE)
      assertEquals(stem + "て", te.surface)
      assertEquals(rStem + "て", te.reading)

      // POTENTIAL (with ra-nuki)
      val pot = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.POTENTIAL)
      assertEquals(stem + "られる", pot.surface)
      assertEquals(rStem + "られる", pot.reading)
      assertTrue("Should accept ra-nuki for $lemma", pot.acceptedOverrides.contains(stem + "れる"))

      // VOLITIONAL
      val vol = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.VOLITIONAL)
      assertEquals(stem + "よう", vol.surface)
      assertEquals(rStem + "よう", vol.reading)

      // PASSIVE
      val pas = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.PASSIVE)
      assertEquals(stem + "られる", pas.surface)
      assertEquals(rStem + "られる", pas.reading)

      // CAUSATIVE
      val cau = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.CAUSATIVE)
      assertEquals(stem + "させる", cau.surface)
      assertEquals(rStem + "させる", cau.reading)

      // CAUSATIVE_PASSIVE
      val cp = Conjugator.conjugate(lemma, reading, VerbGroup.ICHIDAN, VerbForm.CAUSATIVE_PASSIVE)
      assertEquals(stem + "させられる", cp.surface)
      assertEquals(rStem + "させられる", cp.reading)
    }
  }

  @Test
  fun testSuruAndCompounds() {
    val targets = listOf(
      VerbForm.MASU to ("します" to "します"),
      VerbForm.DICTIONARY to ("する" to "する"),
      VerbForm.NAI to ("しない" to "しない"),
      VerbForm.TA to ("した" to "した"),
      VerbForm.TE to ("して" to "して"),
      VerbForm.POTENTIAL to ("できる" to "できる"),
      VerbForm.VOLITIONAL to ("しよう" to "しよう"),
      VerbForm.PASSIVE to ("される" to "される"),
      VerbForm.CAUSATIVE to ("させる" to "させる"),
      VerbForm.CAUSATIVE_PASSIVE to ("させられる" to "させられる")
    )

    // Base する
    for ((form, expected) in targets) {
      val res = Conjugator.conjugate("する", "する", VerbGroup.SURU, form)
      assertEquals(expected.first, res.surface)
      assertEquals(expected.second, res.reading)
    }

    // Compound 勉強する
    for ((form, expected) in targets) {
      val res = Conjugator.conjugate("勉強する", "べんきょうする", VerbGroup.SURU, form)
      assertEquals("勉強" + expected.first, res.surface)
      assertEquals("べんきょう" + expected.second, res.reading)
    }
  }

  @Test
  fun testKuruAllFormsAndReadingShifts() {
    // Coming: 来る (くる)
    // stem changes: き (masu), くる (dict), こ (nai, volitional, passive, causative), き (ta, te)
    val cases = listOf(
      VerbForm.MASU to ("来ます" to "きます"),
      VerbForm.DICTIONARY to ("来る" to "くる"),
      VerbForm.NAI to ("来ない" to "こない"),
      VerbForm.TA to ("来た" to "きた"),
      VerbForm.TE to ("来て" to "きて"),
      VerbForm.POTENTIAL to ("来られる" to "こられる"),
      VerbForm.VOLITIONAL to ("来よう" to "こよう"),
      VerbForm.PASSIVE to ("来られる" to "こられる"),
      VerbForm.CAUSATIVE to ("来させる" to "こさせる"),
      VerbForm.CAUSATIVE_PASSIVE to ("来させられる" to "こさせられる")
    )

    for ((form, expected) in cases) {
      val res = Conjugator.conjugate("来る", "くる", VerbGroup.KURU, form)
      assertEquals("Kuru surface mismatch for $form", expected.first, res.surface)
      assertEquals("Kuru reading mismatch for $form", expected.second, res.reading)
    }

    // Also check potential override (来れる / これる)
    val pot = Conjugator.conjugate("来る", "くる", VerbGroup.KURU, VerbForm.POTENTIAL)
    assertTrue("Should accept 来れる", pot.acceptedOverrides.contains("来れる"))
    assertTrue("Should accept これる", pot.acceptedOverrides.contains("これる"))
  }

  @Test
  fun testExceptionsIkuAruIrassharu() {
    // 行く (いく)
    val ikuTe = Conjugator.conjugate("行く", "いく", VerbGroup.GODAN, VerbForm.TE)
    assertEquals("行って", ikuTe.surface)
    assertEquals("いって", ikuTe.reading)

    val ikuTa = Conjugator.conjugate("行く", "いく", VerbGroup.GODAN, VerbForm.TA)
    assertEquals("行った", ikuTa.surface)
    assertEquals("いった", ikuTa.reading)

    val ikuMasu = Conjugator.conjugate("行く", "いく", VerbGroup.GODAN, VerbForm.MASU)
    assertEquals("行きます", ikuMasu.surface)
    assertEquals("いきます", ikuMasu.reading)

    // ある
    val aruNai = Conjugator.conjugate("ある", "ある", VerbGroup.GODAN, VerbForm.NAI)
    assertEquals("ない", aruNai.surface)
    assertEquals("ない", aruNai.reading)

    val aruMasu = Conjugator.conjugate("ある", "ある", VerbGroup.GODAN, VerbForm.MASU)
    assertEquals("あります", aruMasu.surface)
    assertEquals("あります", aruMasu.reading)

    // いらっしゃる
    val irassharuMasu = Conjugator.conjugate("いらっしゃる", "いらっしゃる", VerbGroup.GODAN, VerbForm.MASU)
    assertEquals("いらっしゃいます", irassharuMasu.surface)
    assertEquals("いらっしゃいます", irassharuMasu.reading)
  }
}
