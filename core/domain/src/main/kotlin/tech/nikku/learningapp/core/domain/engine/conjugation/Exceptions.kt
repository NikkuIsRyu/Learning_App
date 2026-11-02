package tech.nikku.learningapp.core.domain.engine.conjugation

import tech.nikku.learningapp.core.domain.model.VerbForm

object Exceptions {

  val HONORIFIC_RU_VERBS = setOf(
    "いらっしゃる",
    "おっしゃる",
    "くださる",
    "なさる",
    "ござる"
  )

  fun handleSpecialVerb(
    lemma: String,
    reading: String,
    target: VerbForm
  ): Conjugation? {
    // 1. 行く / いく
    if (reading == "いく" || reading == "ゆく" || lemma == "行く" ||
        reading.endsWith("いく") || reading.endsWith("ゆく") || lemma.endsWith("行く")) {
      val prefix = lemma.dropLast(1)
      val rPrefix = reading.dropLast(1)
      when (target) {
        VerbForm.TE -> return Conjugation(
          surface = prefix + "って",
          reading = rPrefix + "って",
          ruleExplanation = "Exception: 行く te-form is 行って (not いて)"
        )
        VerbForm.TA -> return Conjugation(
          surface = prefix + "った",
          reading = rPrefix + "った",
          ruleExplanation = "Exception: 行く ta-form is 行った (not いた)"
        )
        else -> { /* regular Godan rules apply for other forms */ }
      }
    }

    // 2. ある
    if (lemma == "ある" && reading == "ある") {
      if (target == VerbForm.NAI) {
        return Conjugation(
          surface = "ない",
          reading = "ない",
          ruleExplanation = "Exception: ある negative form is ない (not あらない)"
        )
      }
    }

    // 3. Honorific -る verbs in MASU form
    if (HONORIFIC_RU_VERBS.contains(reading) || HONORIFIC_RU_VERBS.contains(lemma)) {
      if (target == VerbForm.MASU) {
        val stem = lemma.dropLast(1)
        val rStem = reading.dropLast(1)
        return Conjugation(
          surface = stem + "います",
          reading = rStem + "います",
          ruleExplanation = "Exception: Honorific -る verb mas-form changes る → い + ます"
        )
      }
    }

    return null
  }

  fun conjugateKuru(
    lemma: String,
    reading: String,
    target: VerbForm
  ): Conjugation {
    val kanjiPrefix = if (lemma.startsWith("来")) "来" else ""
    return when (target) {
      VerbForm.MASU -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来ます" else "きます",
        reading = "きます",
        ruleExplanation = "Kuru irregular: stem changes to き (来ます)"
      )
      VerbForm.DICTIONARY -> Conjugation(
        surface = lemma,
        reading = reading,
        ruleExplanation = "Dictionary form"
      )
      VerbForm.NAI -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来ない" else "こない",
        reading = "こない",
        ruleExplanation = "Kuru irregular: stem changes to こ (来ない)"
      )
      VerbForm.TA -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来た" else "きた",
        reading = "きた",
        ruleExplanation = "Kuru irregular: past form is 来た (きた)"
      )
      VerbForm.TE -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来て" else "きて",
        reading = "きて",
        ruleExplanation = "Kuru irregular: te-form is 来て (きて)"
      )
      VerbForm.POTENTIAL -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来られる" else "こられる",
        reading = "こられる",
        ruleExplanation = "Kuru irregular: potential is 来られる (こられる)",
        acceptedOverrides = listOf(
          if (kanjiPrefix.isNotEmpty()) "来れる" else "これる",
          "これる"
        ),
        notes = listOf("ら抜き (colloquial): 来れる")
      )
      VerbForm.VOLITIONAL -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来よう" else "こよう",
        reading = "こよう",
        ruleExplanation = "Kuru irregular: volitional is 来よう (こよう)"
      )
      VerbForm.PASSIVE -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来られる" else "こられる",
        reading = "こられる",
        ruleExplanation = "Kuru irregular: passive is 来られる (こられる)"
      )
      VerbForm.CAUSATIVE -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来させる" else "こさせる",
        reading = "こさせる",
        ruleExplanation = "Kuru irregular: causative is 来させる (こさせる)"
      )
      VerbForm.CAUSATIVE_PASSIVE -> Conjugation(
        surface = if (kanjiPrefix.isNotEmpty()) "来させられる" else "こさせられる",
        reading = "こさせられる",
        ruleExplanation = "Kuru irregular: causative-passive is 来させられる (こさせられる)"
      )
    }
  }

  fun conjugateSuru(
    lemmaPrefix: String,
    readingPrefix: String,
    target: VerbForm
  ): Conjugation {
    return when (target) {
      VerbForm.MASU -> Conjugation(
        surface = lemmaPrefix + "します",
        reading = readingPrefix + "します",
        ruleExplanation = "Suru irregular: する → します"
      )
      VerbForm.DICTIONARY -> Conjugation(
        surface = lemmaPrefix + "する",
        reading = readingPrefix + "する",
        ruleExplanation = "Dictionary form"
      )
      VerbForm.NAI -> Conjugation(
        surface = lemmaPrefix + "しない",
        reading = readingPrefix + "しない",
        ruleExplanation = "Suru irregular: する → しない"
      )
      VerbForm.TA -> Conjugation(
        surface = lemmaPrefix + "した",
        reading = readingPrefix + "した",
        ruleExplanation = "Suru irregular: past form is した"
      )
      VerbForm.TE -> Conjugation(
        surface = lemmaPrefix + "して",
        reading = readingPrefix + "して",
        ruleExplanation = "Suru irregular: te-form is して"
      )
      VerbForm.POTENTIAL -> Conjugation(
        surface = lemmaPrefix + "できる",
        reading = readingPrefix + "できる",
        ruleExplanation = "Suru irregular: potential is できる"
      )
      VerbForm.VOLITIONAL -> Conjugation(
        surface = lemmaPrefix + "しよう",
        reading = readingPrefix + "しよう",
        ruleExplanation = "Suru irregular: volitional is しよう"
      )
      VerbForm.PASSIVE -> Conjugation(
        surface = lemmaPrefix + "される",
        reading = readingPrefix + "される",
        ruleExplanation = "Suru irregular: passive is される"
      )
      VerbForm.CAUSATIVE -> Conjugation(
        surface = lemmaPrefix + "させる",
        reading = readingPrefix + "させる",
        ruleExplanation = "Suru irregular: causative is させる"
      )
      VerbForm.CAUSATIVE_PASSIVE -> Conjugation(
        surface = lemmaPrefix + "させられる",
        reading = readingPrefix + "させられる",
        ruleExplanation = "Suru irregular: causative-passive is させられる"
      )
    }
  }
}
