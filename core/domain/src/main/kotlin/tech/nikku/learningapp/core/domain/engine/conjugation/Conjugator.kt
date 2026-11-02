package tech.nikku.learningapp.core.domain.engine.conjugation

import tech.nikku.learningapp.core.domain.model.VerbForm
import tech.nikku.learningapp.core.domain.model.VerbGroup

object Conjugator {

  fun conjugate(
    lemma: String,
    reading: String,
    group: VerbGroup,
    target: VerbForm,
    polite: Boolean = false
  ): Conjugation {
    val plainConjugation = when (group) {
      VerbGroup.KURU -> Exceptions.conjugateKuru(lemma, reading, target)
      VerbGroup.SURU -> {
        val lemmaPrefix = lemma.removeSuffix("する")
        val readingPrefix = reading.removeSuffix("する")
        Exceptions.conjugateSuru(lemmaPrefix, readingPrefix, target)
      }
      VerbGroup.ICHIDAN -> conjugateIchidan(lemma, reading, target)
      VerbGroup.GODAN -> conjugateGodan(lemma, reading, target)
    }

    if (!polite || target == VerbForm.MASU) {
      return plainConjugation
    }

    return makePolite(plainConjugation, target)
  }

  private fun conjugateIchidan(
    lemma: String,
    reading: String,
    target: VerbForm
  ): Conjugation {
    val stem = lemma.dropLast(1)
    val rStem = reading.dropLast(1)

    return when (target) {
      VerbForm.MASU -> Conjugation(
        surface = stem + "ます",
        reading = rStem + "ます",
        ruleExplanation = "Group II (Ichidan): drop る + ます"
      )
      VerbForm.DICTIONARY -> Conjugation(
        surface = lemma,
        reading = reading,
        ruleExplanation = "Dictionary form"
      )
      VerbForm.NAI -> Conjugation(
        surface = stem + "ない",
        reading = rStem + "ない",
        ruleExplanation = "Group II (Ichidan): drop る + ない"
      )
      VerbForm.TA -> Conjugation(
        surface = stem + "た",
        reading = rStem + "た",
        ruleExplanation = "Group II (Ichidan): drop る + た"
      )
      VerbForm.TE -> Conjugation(
        surface = stem + "て",
        reading = rStem + "て",
        ruleExplanation = "Group II (Ichidan): drop る + て"
      )
      VerbForm.POTENTIAL -> Conjugation(
        surface = stem + "られる",
        reading = rStem + "られる",
        ruleExplanation = "Group II (Ichidan): drop る + られる",
        acceptedOverrides = listOf(stem + "れる", rStem + "れる"),
        notes = listOf("ら抜き (colloquial): ${stem}れる")
      )
      VerbForm.VOLITIONAL -> Conjugation(
        surface = stem + "よう",
        reading = rStem + "よう",
        ruleExplanation = "Group II (Ichidan): drop る + よう"
      )
      VerbForm.PASSIVE -> Conjugation(
        surface = stem + "られる",
        reading = rStem + "られる",
        ruleExplanation = "Group II (Ichidan): drop る + られる"
      )
      VerbForm.CAUSATIVE -> Conjugation(
        surface = stem + "させる",
        reading = rStem + "させる",
        ruleExplanation = "Group II (Ichidan): drop る + させる"
      )
      VerbForm.CAUSATIVE_PASSIVE -> Conjugation(
        surface = stem + "させられる",
        reading = rStem + "させられる",
        ruleExplanation = "Group II (Ichidan): drop る + させられる"
      )
    }
  }

  private fun conjugateGodan(
    lemma: String,
    reading: String,
    target: VerbForm
  ): Conjugation {
    // Check specific exceptions (e.g. 行く, ある, honorifics)
    val exception = Exceptions.handleSpecialVerb(lemma, reading, target)
    if (exception != null) {
      return exception
    }

    val lastKana = reading.last()
    val stem = lemma.dropLast(1)
    val rStem = reading.dropLast(1)

    return when (target) {
      VerbForm.MASU -> {
        val iRow = KanaRows.toIRow(lastKana)
        Conjugation(
          surface = stem + iRow + "ます",
          reading = rStem + iRow + "ます",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → i-row ($iRow) + ます"
        )
      }
      VerbForm.DICTIONARY -> Conjugation(
        surface = lemma,
        reading = reading,
        ruleExplanation = "Dictionary form"
      )
      VerbForm.NAI -> {
        val aRow = KanaRows.toARow(lastKana)
        Conjugation(
          surface = stem + aRow + "ない",
          reading = rStem + aRow + "ない",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → a-row ($aRow) + ない"
        )
      }
      VerbForm.TA -> {
        val suffix = KanaRows.teTaSuffix(lastKana, isTa = true)
        Conjugation(
          surface = stem + suffix,
          reading = rStem + suffix,
          ruleExplanation = "Group I (Godan): past ta-form ($lastKana → $suffix)"
        )
      }
      VerbForm.TE -> {
        val suffix = KanaRows.teTaSuffix(lastKana, isTa = false)
        Conjugation(
          surface = stem + suffix,
          reading = rStem + suffix,
          ruleExplanation = "Group I (Godan): te-form ($lastKana → $suffix)"
        )
      }
      VerbForm.POTENTIAL -> {
        val eRow = KanaRows.toERow(lastKana)
        Conjugation(
          surface = stem + eRow + "る",
          reading = rStem + eRow + "る",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → e-row ($eRow) + る"
        )
      }
      VerbForm.VOLITIONAL -> {
        val oRow = KanaRows.toORow(lastKana)
        Conjugation(
          surface = stem + oRow + "う",
          reading = rStem + oRow + "う",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → o-row ($oRow) + う"
        )
      }
      VerbForm.PASSIVE -> {
        val aRow = KanaRows.toARow(lastKana)
        Conjugation(
          surface = stem + aRow + "れる",
          reading = rStem + aRow + "れる",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → a-row ($aRow) + れる"
        )
      }
      VerbForm.CAUSATIVE -> {
        val aRow = KanaRows.toARow(lastKana)
        Conjugation(
          surface = stem + aRow + "せる",
          reading = rStem + aRow + "せる",
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → a-row ($aRow) + せる"
        )
      }
      VerbForm.CAUSATIVE_PASSIVE -> {
        val aRow = KanaRows.toARow(lastKana)
        val standardSurface = stem + aRow + "せられる"
        val standardReading = rStem + aRow + "せられる"

        // Short form allowed for all except verbs ending in す
        val shortOverrides = if (lastKana != 'す') {
          val shortSurface = stem + aRow + "される"
          val shortReading = rStem + aRow + "される"
          listOf(shortSurface, shortReading)
        } else {
          emptyList()
        }

        Conjugation(
          surface = standardSurface,
          reading = standardReading,
          ruleExplanation = "Group I (Godan): u-row ($lastKana) → a-row ($aRow) + せられる",
          acceptedOverrides = shortOverrides,
          notes = if (shortOverrides.isNotEmpty()) listOf("Short form accepted: ${stem}${aRow}される") else emptyList()
        )
      }
    }
  }

  private fun makePolite(plain: Conjugation, target: VerbForm): Conjugation {
    // When plain form ends in る (Ichidan-like inflection, e.g. POTENTIAL, PASSIVE, CAUSATIVE, CAUSATIVE_PASSIVE)
    if (plain.surface.endsWith("る") && plain.reading.endsWith("る")) {
      val politeSurface = plain.surface.dropLast(1) + "ます"
      val politeReading = plain.reading.dropLast(1) + "ます"
      val politeOverrides = plain.acceptedOverrides.map { override ->
        if (override.endsWith("る")) override.dropLast(1) + "ます" else override
      }
      return plain.copy(
        surface = politeSurface,
        reading = politeReading,
        ruleExplanation = plain.ruleExplanation + " (polite: る → ます)",
        acceptedOverrides = politeOverrides
      )
    }

    // When plain form ends in ない (NAI form)
    if (plain.surface.endsWith("ない") && plain.reading.endsWith("ない")) {
      return plain.copy(
        surface = plain.surface + "です",
        reading = plain.reading + "です",
        ruleExplanation = plain.ruleExplanation + " (polite: + です)"
      )
    }

    return plain
  }
}
