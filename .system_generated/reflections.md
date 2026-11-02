# Reflexion Episodic Memory (`M_sr`)

When any Evaluator gate (`G0`–`G5`) fails, Gemini Flash MUST append a reflection entry below **before** modifying code or re-running the gate. See [`docs/02_HANDOFF_ROADMAP.md`](../docs/02_HANDOFF_ROADMAP.md) §1.2–1.3.

## Lessons (Cross-Ticket Rules Promoted from Past Reflections)
- **L-01**: Never use `@Insert(onConflict = OnConflictStrategy.REPLACE)` on parent content tables; SQLite executes `DELETE + INSERT`, triggering `ON DELETE CASCADE` on child tables (`grammar_examples`, `dialogue_turns`, `kanji_compounds`). Always use `@Upsert`.
- **L-02**: Never instantiate `hiltViewModel()` or Room databases inside `@Preview` functions; always preview the stateless `XxxScreen(state, onEvent)` composable with preview fixture data.
- **L-03**: Always pass `LocaleList("ja-JP")` for Japanese text and `LocaleList("zh-CN")` for Chinese text to prevent Han-unification glyph mismatches.
- **L-04**: Always quote workspace paths in PowerShell/CLI commands (`d:\.D Dokuments\Uni Stuff\Knowledge_App` contains spaces).
- **L-05**: Specify `@Config(sdk = [34])` on Robolectric test classes to run within Java 17 JBR compatibility limits when `compileSdk` is 36.

---

## Reflection Log

<!-- Append entries using template:
### [R-0001] T<id> · <Gate> · attempt <k>/3 · <ISO-8601 timestamp>
- **Signal (≤5 lines, verbatim):** `...`
- **(a) Root Cause:** ...
- **(b) Faulty Assumption:** ...
- **(c) Corrective Code Strategy:** ...
- **Outcome:** PASS | FAIL → R-0002
-->

### [R-0001] T1.1 · G1 · attempt 1/3 · 2026-11-02T14:48:00+01:00
- **Signal (≤5 lines, verbatim):**
```
Task 's' is ambiguous in root project 'Learning App' and its subprojects. Candidates are: 'sdkReleaseDependencyData', ...
BUILD FAILED in 2m 21s
Calculating task graph as no cached configuration is available for tasks: a s s e m b l e D e b u g
```
- **(a) Root Cause:** In `tools/agent/gate.ps1`, `& $gradlew @taskList` splats the variable `$taskList`. When `$taskList` evaluates to a scalar string (e.g. from `switch`), PowerShell treats `[string]` as `IEnumerable<char>` and splats each character as an argument (`a`, `s`, `s`, ...).
- **(b) Faulty Assumption:** Assumed `@taskList` would pass task names as discrete word arguments regardless of whether PowerShell unboxed the array into a scalar string.
- **(c) Corrective Code Strategy:** In `tools/agent/gate.ps1`, explicitly type `[string[]]$taskList = @(...)` and pass `$taskList` (not `@taskList`) to `$gradlew`.
- **Outcome:** FAIL → R-0002

### [R-0002] T1.1 · G1 · attempt 2/3 · 2026-11-02T14:51:00+01:00
- **Signal (≤5 lines, verbatim):**
```
Execution failed for task ':app:checkDebugAarMetadata'.
> 10 issues were found when checking AAR metadata:
  1. Dependency 'androidx.activity:activity-ktx:1.13.0' requires libraries and applications that
     depend on it to compile against version 36 or later of the Android APIs.
     :app is currently compiled against android-35.
```
- **(a) Root Cause:** The template dependencies (e.g. `androidx.activity:1.13.0`, `androidx.core:1.18.0`) require a `compileSdk` of at least 36. We had set `compileSdk = 35` after only installing `platforms/android-35`.
- **(b) Faulty Assumption:** Assumed `android-35` was sufficient for the template's AndroidX 2026 dependencies.
- **(c) Corrective Code Strategy:** Install `platforms/android-36` using `android sdk install platforms/android-36` and update `compileSdk = 36` in `app/build.gradle.kts`.
- **Outcome:** PASS (GATE G1 PASS)

### [R-0003] T1.2 · G1 · attempt 1/3 · 2026-11-02T15:00:00+01:00
- **Signal (≤5 lines, verbatim):**
```
Execution failed for task ':app:checkDebugAarMetadata'.
> 10 issues were found when checking AAR metadata:
  1. Dependency 'androidx.hilt:hilt-navigation-compose:1.4.0' requires libraries and applications that depend on it to compile against version 37 or later of the Android APIs.
  2. Dependency 'androidx.hilt:hilt-navigation-compose:1.4.0' requires Android Gradle plugin 9.1.0 or higher.
  3. Dependency 'androidx.navigation:navigation-compose-android:2.10.2' requires version 37 or later and AGP 9.1.0.
```
- **(a) Root Cause:** `hiltNavigation = "1.4.0"` and `navigation = "2.10.2"` in `gradle/libs.versions.toml` are preview releases compiled with `minCompileSdk = 37` and `minAndroidGradlePluginVersion = 9.1.0`, which transitively pulled `androidx.lifecycle:2.11.0` (also requiring API 37 and AGP 9.1.0). The project is using AGP 9.0.1 and compileSdk 36.
- **(b) Faulty Assumption:** Assumed `navigation:2.10.2` and `hiltNavigation:1.4.0` were compatible with compileSdk 36 and AGP 9.0.1 without verifying their `META-INF/.../aar-metadata.properties`.
- **(c) Corrective Code Strategy:** Align versions in `gradle/libs.versions.toml`: set `hiltNavigation = "1.3.0"`, `navigation = "2.9.8"`, and keep `androidxLifecycle = "2.10.0"`. Verified via unpacked AAR metadata that both `navigation-compose-android:2.9.8`, `hilt-navigation-compose:1.3.0`, and `lifecycle:2.10.0` have `minCompileSdk = 35` and `minAndroidGradlePluginVersion = 8.6.0`, fully compatible with compileSdk 36 and AGP 9.0.1.
- **Outcome:** FAIL → R-0004

### [R-0004] T1.2 · G1 · attempt 2/3 · 2026-11-02T15:04:00+01:00
- **Signal (≤5 lines, verbatim):**
```
> Task :app:compileDebugKotlin FAILED
e: .../Navigation.kt:8:17 Unresolved reference 'navigation3'.
e: .../Navigation.kt:15:19 Unresolved reference 'rememberNavBackStack'.
e: .../Navigation.kt:17:3 Unresolved reference 'NavDisplay'.
e: .../ui/main/MainScreen.kt:25:17 Unresolved reference 'navigation3'.
```
- **(a) Root Cause:** The initial scaffold generated by `android create empty-activity` contained placeholder files (`Navigation.kt`, `NavigationKeys.kt`, `ui/main/MainScreen.kt`, etc.) using experimental `androidx.navigation3.*`. Our system architecture explicitly specifies standard `androidx.navigation:navigation-compose` with type-safe `@Serializable` routes and `NavigationSuiteScaffold` (to be implemented in T1.4).
- **(b) Faulty Assumption:** Assumed the template's dummy navigation code could remain untouched during T1.2 module split without compiling against Navigation 3 dependencies.
- **(c) Corrective Code Strategy:** Remove the unused template placeholder files (`Navigation.kt`, `NavigationKeys.kt`, `ui/main/*`, `data/*`), update `MainActivity.kt` with a clean `@AndroidEntryPoint` Compose entry point, and clean up unused `nav3` catalog references in `libs.versions.toml`.
- **Outcome:** PASS (GATE G1 PASS, GATE G2 PASS)

### [R-0005] T1.3 · G2 · attempt 1/3 · 2026-11-02T15:14:00+01:00
- **Signal (≤5 lines, verbatim):**
```
RubyParserTest > fromAnkiFormat_convertsCorrectly FAILED
org.junit.ComparisonFailure: expected:<{窓|まど}が[ ]{開|あ}きます> but was:<{窓|まど}が[]{開|あ}きます>
```
- **(a) Root Cause:** In `RubyParser.fromAnkiFormat`, the regex `(?: )?([\u4e00-\u9faf...]+)\[([^\]\r\n]+)\]` matched and consumed the optional leading whitespace before the kanji block without outputting it, thereby stripping desired inter-word spaces (e.g. between `が` and `{開|あ}きます`).
- **(b) Faulty Assumption:** Assumed consuming the optional leading space was necessary to handle Anki's leading space convention.
- **(c) Corrective Code Strategy:** Remove `(?: )?` from the regex match so whitespace outside the kanji-bracket pair is naturally preserved by `substring(lastIndex, match.range.first)`.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0016] T2.5 · G2 · attempt 1/3 · 2026-11-02T16:34:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ContentSeederTest.kt:158:30 Unresolved reference 'MINNA_SHOKYU'.
ContentSeederTest.kt:169:29 Unresolved reference 'GODAN_MU'.
ContentSeederTest.kt:253:9 No parameter with name 'explanation' found.
ContentSeederTest.kt:357:27 Unresolved reference 'VOCAB'.
ContentSeederTest.kt:393:46 Unresolved reference 'observeProgress'.
```
- **(a) Root Cause:** `ContentSeederTest.kt` constructed test fixtures with out-of-sync identifiers: `ContentSource.MINNA_SHOKYU` instead of `ContentSource.MINNA`, `VerbGroup.GODAN_MU` instead of `VerbGroup.GODAN`, `ItemType.VOCAB` instead of `ItemType.WORD`, `ProgressDao.observeProgress` instead of `ProgressDao.observe`, and incomplete `ConfusableSide` constructor parameters (`term`, `romanization`, `conceptEn`, `formula`, `ruleEn`, `correct`, `incorrect`).
- **(b) Faulty Assumption:** Assumed domain enums and DAO method names matched legacy draft naming without checking the exact signatures in `Enums.kt`, `ExercisePayload.kt`, and `ProgressDao.kt`.
- **(c) Corrective Code Strategy:** Fix `ContentSeederTest.kt` to use `ContentSource.MINNA`, `VerbGroup.GODAN`, `ItemType.WORD`, `ProgressDao.observe`, and proper `ConfusableSide` instantiation with `SentenceSample` and `WrongSample`. Then re-run Gate G2.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0006] T1.4 · G1 · attempt 1/3 · 2026-11-02T15:26:00+01:00
- **Signal (≤5 lines, verbatim):**
```
TopLevelDestination.kt:4:34 Unresolved reference 'icons'.
TopLevelDestination.kt:22:12 Unresolved reference 'Icons'.
> Task :app:compileDebugKotlin FAILED
```
- **(a) Root Cause:** `androidx.compose.material:material-icons-core` was not declared in the dependencies of `:app` or `:core:designsystem`, so `androidx.compose.material.icons.Icons` is not visible on the classpath.
- **(b) Faulty Assumption:** Assumed `material3` re-exported `material-icons-core` transitively on the compile classpath for application modules.
- **(c) Corrective Code Strategy:** Add `androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }` to `gradle/libs.versions.toml` (managed by compose-bom) and add `implementation(libs.androidx.compose.material.icons.core)` to `app/build.gradle.kts`.
- **Outcome:** PASS (GATE G1 PASS)

### [R-0007] T1.5 · G2 · attempt 1/3 · 2026-11-02T15:33:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.loader.VaultRoadmapLoaderTest.classMethod:
java.lang.UnsupportedOperationException: Failed to create a Robolectric sandbox: Android SDK 36 requires Java 21 (have Java 17)
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:** Robolectric defaults its simulation environment to `targetSdk` / `compileSdk` (36), whose Robolectric android-all jar requires Java 21 runtime bytecode. The project environment runs on Android Studio JBR (Java 17).
- **(b) Faulty Assumption:** Assumed Robolectric without `@Config(sdk = ...)` would run on Java 17 for `compileSdk = 36`.
- **(c) Corrective Code Strategy:** Add `@Config(sdk = [34])` to Robolectric test classes (and promote as lesson **L-05**: all Robolectric tests must specify `@Config(sdk = [34])` to run within Java 17 JBR sandbox limits).
- **Outcome:** PASS (GATE G2 PASS)

### [R-0016] T2.5 · G2 · attempt 1/3 · 2026-11-02T16:34:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ContentSeederTest.kt:158:30 Unresolved reference 'MINNA_SHOKYU'.
ContentSeederTest.kt:169:29 Unresolved reference 'GODAN_MU'.
ContentSeederTest.kt:253:9 No parameter with name 'explanation' found.
ContentSeederTest.kt:357:27 Unresolved reference 'VOCAB'.
ContentSeederTest.kt:393:46 Unresolved reference 'observeProgress'.
```
- **(a) Root Cause:** `ContentSeederTest.kt` constructed test fixtures with out-of-sync identifiers: `ContentSource.MINNA_SHOKYU` instead of `ContentSource.MINNA`, `VerbGroup.GODAN_MU` instead of `VerbGroup.GODAN`, `ItemType.VOCAB` instead of `ItemType.WORD`, `ProgressDao.observeProgress` instead of `ProgressDao.observe`, and incomplete `ConfusableSide` constructor parameters (`term`, `romanization`, `conceptEn`, `formula`, `ruleEn`, `correct`, `incorrect`).
- **(b) Faulty Assumption:** Assumed domain enums and DAO method names matched legacy draft naming without checking the exact signatures in `Enums.kt`, `ExercisePayload.kt`, and `ProgressDao.kt`.
- **(c) Corrective Code Strategy:** Fix `ContentSeederTest.kt` to use `ContentSource.MINNA`, `VerbGroup.GODAN`, `ItemType.WORD`, `ProgressDao.observe`, and proper `ConfusableSide` instantiation with `SentenceSample` and `WrongSample`. Then re-run Gate G2.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0008] T2.1 · G1 · attempt 1/3 · 2026-11-02T15:38:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ExercisePayload.kt:98:42 Unresolved reference 'compareTo'.
ExercisePayload.kt:98:43 Syntax error: Expecting an element.
> Task :core:domain:compileKotlin FAILED
```
- **(a) Root Cause:** Typo in `ExercisePayload.Listening` default argument: `val options: List<String> = emptyList>,` had an accidental `>` instead of `()`.
- **(b) Faulty Assumption:** Typographical error during manual transcription of ExercisePayload fields.
- **(c) Corrective Code Strategy:** Correct default argument to `emptyList()`.
- **Outcome:** PASS (GATE G1 PASS)

### [R-0009] T2.2 · G1 · attempt 1/3 · 2026-11-02T15:45:00+01:00
- **Signal (≤5 lines, verbatim):**
```
e: [ksp] .../Entities.kt:242: [MissingType]: Element '...UserProgressEntity' references a type that is not present
e: [ksp] .../Entities.kt:270: [MissingType]: Element '...ReviewLogEntity' references a type that is not present
> Task :core:data:kspDebugKotlin FAILED
```
- **(a) Root Cause:** `import tech.nikku.learningapp.core.domain.model.ItemType` was omitted from individual imports in `Entities.kt`, causing KSP Room annotation processing to fail on `UserProgressEntity` and `ReviewLogEntity`.
- **(b) Faulty Assumption:** Assumed all referenced enums had been explicitly imported when listing them individually.
- **(c) Corrective Code Strategy:** Use wildcard `import tech.nikku.learningapp.core.domain.model.*` in `Entities.kt` (and all DAO/Entity files) to guarantee all domain enums and types are visible to Room KSP.
- **Outcome:** FAIL → R-0010

### [R-0010] T2.2 · G1 · attempt 2/3 · 2026-11-02T15:47:00+01:00
- **Signal (≤5 lines, verbatim):**
```
e: .../LexiconQueryBuilder.kt:29:7 'val' cannot be reassigned.
e: .../LexiconQueryBuilder.kt:29:12 Assignment type mismatch: actual type is 'List<Any>', but 'MutableList<Any>' was expected.
> Task :core:data:compileDebugKotlin FAILED
```
- **(a) Root Cause:** In `LexiconQueryBuilder`, `args += values.map(map)` resolved as `args = args.plus(...)` reassignment instead of in-place mutation because `args` is a `val MutableList<Any>`.
- **(b) Faulty Assumption:** Assumed Kotlin's `+=` operator on `val MutableList<T>` would unambiguously invoke `addAll(elements)`.
- **(c) Corrective Code Strategy:** Replace `args += values.map(map)` with explicit `args.addAll(values.map(map))`.
- **Outcome:** PASS (GATE G1 PASS)


### [R-0011] T2.4 · G2 · attempt 1/3 · 2026-11-02T16:13:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateManifestChecksums:
kotlinx.serialization.SerializationException: tech.nikku.learningapp.core.domain.model.PartOfSpeech does not contain element with name 'ADJECTIVE_I' at path $[41].pos
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:** `mdx_vocab.py` mapped i-adjectives to `"ADJECTIVE_I"`, whereas `PartOfSpeech` enum in `:core:domain` defines `I_ADJECTIVE` and `NA_ADJECTIVE`. During `ContentValidationTest`, `Json.decodeFromString` failed when deserializing `WordDto.pos`.
- **(b) Faulty Assumption:** Assumed the enum constant name was `ADJECTIVE_I` without cross-referencing `tech.nikku.learningapp.core.domain.model.PartOfSpeech` in `Enums.kt`.
- **(c) Corrective Code Strategy:** Update `map_pos` in `tools/content-etl/mdx_vocab.py` to emit `"I_ADJECTIVE"` and `"NA_ADJECTIVE"`. Re-generate `words_mnn_26_32.json` and `manifest.json`, then re-execute Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0012] T2.4 · G2 · attempt 2/3 · 2026-11-02T16:15:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateManifestChecksums:
kotlinx.serialization.MissingFieldException: Fields [text, romanization] are required for type with serial name 'tech.nikku.learningapp.core.domain.model.SentenceSample', but they were missing at path: $[0].left.correct
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:** `export-zh.mts` serialized raw `ConfusableSide` objects directly from TypeScript data which uses `pinyin` and `hanzi`. Kotlin domain models `ConfusableSide`, `SentenceSample`, and `WrongSample` require `romanization` and `text`.
- **(b) Faulty Assumption:** Assumed TypeScript Astro model field names (`hanzi`, `pinyin`) matched Kotlin domain model properties (`text`, `romanization`) verbatim.
- **(c) Corrective Code Strategy:** In `export-zh.mts`, explicitly map `ConfusableSide` mapping `side.pinyin -> romanization`, `side.correct.hanzi -> text`, `side.correct.pinyin -> romanization`, `side.incorrect.hanzi -> text`, `side.incorrect.pinyin -> romanization`. Re-run `export-zh.mts`, `generate_manifest.py`, and re-execute Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0013] T2.4 · G2 · attempt 3/3 · 2026-11-02T16:16:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateManifestChecksums:
kotlinx.serialization.MissingFieldException: Field 'colorHex' is required for type with serial name 'tech.nikku.learningapp.core.domain.model.SpeakerInfo', but it was missing at path: $[0].speakers['A']
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:** `chineseDialogues.ts` defines speaker colors under property `color`, whereas Kotlin domain model `SpeakerInfo` requires `colorHex`.
- **(b) Faulty Assumption:** Assumed speaker object keys in Astro matched Kotlin domain model property names verbatim without checking `colorHex` vs `color`.
- **(c) Corrective Code Strategy:** In `export-zh.mts`, map `speakers` entries to ensure `colorHex: sp.color || sp.colorHex`. Re-run `export-zh.mts`, `generate_manifest.py`, and Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0014] T2.4 · G2 · attempt 4/3 · 2026-11-02T16:18:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateIds: java.lang.AssertionError: Duplicate ID found: ja:mnn:1.1
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateDialogueTurns: java.lang.AssertionError: startMs must be < endMs in zh:d:Dialogue10_2#9
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateVerbInvariants: java.lang.AssertionError: Verb ja:mnn:26:w021 (ずいぶん) must have verbGroup
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:**
  1. Regex `###\s+` matched `#### 1.1` in MDX without `^` anchor, generating duplicate ID `ja:mnn:1.1`.
  2. `Dialogue10_2` in `chineseDialogues.ts` had a corrupt 9th turn with `startS: 0, endS: 0`; the audio actually has 8 turns documented in `Dialogue10_2_timestamps.json`.
  3. `map_pos` in `mdx_vocab.py` checked `"verb" in rp` before `"adverb"`, matching `"adverb"` as a verb.
- **(b) Faulty Assumption:** Assumed `###` would not match inside `####`, assumed raw TS dialogue turns were uncorrupted without cross-referencing timestamps JSON, and assumed string `"verb"` check wouldn't conflict with `"adverb"`.
- **(c) Corrective Code Strategy:**
  1. Anchor grammar header regex to `r'^###\s+((?:2[6-9]|3[0-2])\.\d+)\s+([^📖\n]+)'` with `re.MULTILINE`.
  2. In `export-zh.mts`, load turns and timestamps from authoritative `_timestamps.json` when present.
  3. In `mdx_vocab.py`, check `adverb` / `副詞` before `verb`.
  Re-run ETL, regenerate manifest, and re-execute Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0015] T2.4 · G2 · attempt 5/3 · 2026-11-02T16:21:00+01:00
- **Signal (≤5 lines, verbatim):**
```
tech.nikku.learningapp.core.data.content.ContentValidationTest.validateVerbInvariants:
java.lang.AssertionError: Verb ja:mnn:28:w046 (うーん) must have verbGroup
> Task :core:data:testDebugUnitTest FAILED
```
- **(a) Root Cause:** In `map_pos`, `"動詞" in rp` evaluated to true for `Interjection (感動詞)` because `"感動詞"` contains `"動詞"`. This caused interjections like `うーん` (L28 #46) to be categorized as `VERB`.
- **(b) Faulty Assumption:** Assumed substring `"動詞"` uniquely identified verbs, overlooking compound kanji terms like `感動詞` (interjection).
- **(c) Corrective Code Strategy:** Define `VERB` strictly via `(lesson, item) in VERB_META` and check `interjection` / `感動詞` before any fallback verb check. Re-run `mdx_vocab.py`, `generate_manifest.py`, and Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0016] T2.5 · G2 · attempt 1/3 · 2026-11-02T16:34:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ContentSeederTest.kt:158:30 Unresolved reference 'MINNA_SHOKYU'.
ContentSeederTest.kt:169:29 Unresolved reference 'GODAN_MU'.
ContentSeederTest.kt:253:9 No parameter with name 'explanation' found.
ContentSeederTest.kt:357:27 Unresolved reference 'VOCAB'.
ContentSeederTest.kt:393:46 Unresolved reference 'observeProgress'.
```
- **(a) Root Cause:** `ContentSeederTest.kt` constructed test fixtures with out-of-sync identifiers: `ContentSource.MINNA_SHOKYU` instead of `ContentSource.MINNA`, `VerbGroup.GODAN_MU` instead of `VerbGroup.GODAN`, `ItemType.VOCAB` instead of `ItemType.WORD`, `ProgressDao.observeProgress` instead of `ProgressDao.observe`, and incomplete `ConfusableSide` constructor parameters (`term`, `romanization`, `conceptEn`, `formula`, `ruleEn`, `correct`, `incorrect`).
- **(b) Faulty Assumption:** Assumed domain enums and DAO method names matched legacy draft naming without checking the exact signatures in `Enums.kt`, `ExercisePayload.kt`, and `ProgressDao.kt`.
- **(c) Corrective Code Strategy:** Fix `ContentSeederTest.kt` to use `ContentSource.MINNA`, `VerbGroup.GODAN`, `ItemType.WORD`, `ProgressDao.observe`, and proper `ConfusableSide` instantiation with `SentenceSample` and `WrongSample`. Then re-run Gate G2.
- **Outcome:** PASS (GATE G2 PASS)



### [R-0017] T3.1 · G2 · attempt 1/3 · 2026-11-02T16:50:00+01:00
- **Signal (≤5 lines, verbatim):**
```
LexiconRepositoryImpl.kt:33:9 No parameter with name 'domain' found.
LexiconRepositoryImpl.kt:38:9 No value passed for parameter 'f'.
LexiconRepositoryImpl.kt:59:39 No parameter with name 'domain' found.
```
- **(a) Root Cause:** `LexiconQueryBuilder.build(f: LexiconFilter)` takes a single `LexiconFilter` parameter, but `LexiconRepositoryImpl.kt` called it with discrete named arguments (`domain`, `chapters`, `pos`, etc.).
- **(b) Faulty Assumption:** Assumed `LexiconQueryBuilder.build` had discrete defaulted arguments rather than accepting the `LexiconFilter` data class.
- **(c) Corrective Code Strategy:** Construct a `LexiconFilter` instance and pass it to `LexiconQueryBuilder.build(f)`. Re-run Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0018] T3.1 · G2 · attempt 2/3 · 2026-11-02T16:52:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ChapterScreen.kt:464:33 Unresolved reference 'SyntaxColorGroup'.
ChapterScreen.kt:465:27 Unresolved reference 'SyntaxColorGroup'.
ChapterScreen.kt:466:29 Unresolved reference 'SyntaxColorGroup'.
```
- **(a) Root Cause:** `ChapterScreen.kt` referenced `SyntaxColorGroup` which does not exist; the domain enum defining pattern token roles is `SyntaxRole`.
- **(b) Faulty Assumption:** Assumed the syntax enum was named `SyntaxColorGroup` rather than `SyntaxRole`.
- **(c) Corrective Code Strategy:** Replace `SyntaxColorGroup` with `SyntaxRole` (`SyntaxRole.VERB`, `SyntaxRole.CONNECTOR`, `SyntaxRole.PARTICLE`) in `ChapterScreen.kt`. Re-run Gate G2.
- **Outcome:** PASS (GATE G2 PASS)

### [R-0019] T3.1 · G2 · attempt 3/3 · 2026-11-02T16:53:00+01:00
- **Signal (≤5 lines, verbatim):**
```
JapaneseHubViewModelTest > chaptersOneToFiftyGeneratedWithVerifiedFlagsAndCounts FAILED
java.lang.AssertionError: Chapter 26 must have words > 0
```
- **(a) Root Cause:** `stateIn(initialValue = JapaneseHubUiState(isLoading = true))` emits its initial value first upon collection. Turbine's `awaitItem()` consumed the initial loading state (where `wordCount = 0` for all chapters) rather than advancing until the repository flow value was emitted.
- **(b) Faulty Assumption:** Assumed `stateIn` with an already-populated `StateFlow` would immediately emit the transformed upstream value on the first `awaitItem()` call without emitting `initialValue`.
- **(c) Corrective Code Strategy:** In `JapaneseHubViewModelTest`, skip or check the initial loading state (`if (state.isLoading) state = awaitItem()`) or use `SharingStarted.Eagerly` / `advanceUntilIdle()` so Turbine asserts on the active state.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0020] T3.2 · G2 · attempt 1/3 · 2026-11-02T17:03:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ConjugatorGoldenTest > testIchidanVerbsAcrossAllForms FAILED
tech.nikku.learningapp.core.domain.engine.conjugation.ConjugatorGoldenTest.testIchidanVerbsAcrossAllForms: org.junit.ComparisonFailure: expected:<食[]ます> but was:<食[べ]ます>
```
- **(a) Root Cause:** In `ConjugatorGoldenTest.kt`, the test helper constructed expected ichidan surfaces using a hardcoded kanji prefix (`"食"`) instead of the true lemma stem `lemma.dropLast(1)` (`"食べ"`).
- **(b) Faulty Assumption:** Assumed the test prefix for `食べる` was just the kanji `食`, omitting the okurigana `べ`.
- **(c) Corrective Code Strategy:** In `ConjugatorGoldenTest.kt`, use `lemma.dropLast(1)` as `stem` (matching `rStem = reading.dropLast(1)`) so `食べる` conjugates to `食べます`, `食べた`, etc.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0021] T3.3 · G1 · attempt 1/3 · 2026-11-02T17:10:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ProgressRepositoryImpl.kt:126:34 Unresolved reference 'reps'.
ProgressRepositoryImpl.kt:128:35 Unresolved reference 'stepIndex'.
```
- **(a) Root Cause:** `ProgressRepositoryImpl.kt` referenced `reps` and `stepIndex` on `SrsSnapshot` instead of `repetitions` and `learningStep`.
- **(b) Faulty Assumption:** Assumed `SrsSnapshot` field names matched shorthand `reps` and `stepIndex`.
- **(c) Corrective Code Strategy:** In `ProgressRepositoryImpl.kt`, use `nextSnapshot.repetitions` and `nextSnapshot.learningStep` (and use `SrsSnapshot.initial(itemKey, dueAt)` for new items).
- **Outcome:** PASS (GATE G1 PASS)


### [R-0022] T3.3 · G1 · attempt 2/3 · 2026-11-02T17:13:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ProgressRepositoryImpl.kt:76:7 No parameter with name 'current' found.
ProgressRepositoryImpl.kt:79:7 No parameter with name 'zoneId' found.
ProgressRepositoryImpl.kt:79:7 No value passed for parameter 'state'.
```
- **(a) Root Cause:** `ProgressRepositoryImpl.kt` called `SrsScheduler.schedule(current = ..., zoneId = ...)` with incorrect parameter names. The method parameter is `state: SrsSnapshot`, `grade: ReviewGrade`, `now: Instant`, with default `cfg` and `seed`.
- **(b) Faulty Assumption:** Assumed `SrsScheduler.schedule` had a `current` and `zoneId` parameter name.
- **(c) Corrective Code Strategy:** Call `SrsScheduler.schedule(state = snapshot, grade = grade, now = Instant.ofEpochMilli(nowMs))` in `ProgressRepositoryImpl.kt`.
- **Outcome:** PASS (GATE G1 PASS)


### [R-0023] T3.3 · G1 · attempt 3/3 · 2026-11-02T17:14:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ConjugationDrillScreen.kt:25:47 Unresolved reference 'Lightbulb'.
ConjugationDrillScreen.kt:295:32 Unresolved reference 'Lightbulb'.
```
- **(a) Root Cause:** `Icons.Default.Lightbulb` is an extended material icon not present in core `material.icons.filled`.
- **(b) Faulty Assumption:** Assumed `Lightbulb` was available in core Material 3 icons.
- **(c) Corrective Code Strategy:** Replace `Icons.Default.Lightbulb` with `Icons.Default.Info` in `ConjugationDrillScreen.kt`.
- **Outcome:** PASS (GATE G1 PASS)


### [R-0024] T3.3 · G1 · attempt 4/3 (sub-step) · 2026-11-02T17:15:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ConjugationDrillScreen.kt: No parameter with name 'rubyText' found.
ConjugationMatrixScreen.kt: No parameter with name 'rubyText' found.
```
- **(a) Root Cause:** `FuriganaText` takes `text: String`, `style: TextStyle`, `rubyStyle: TextStyle`, but `ConjugationMatrixScreen.kt` and `ConjugationDrillScreen.kt` called it with `rubyText`, `baseFontSize`, and `rubyFontSize`.
- **(b) Faulty Assumption:** Assumed `FuriganaText` accepted discrete font size arguments rather than `style: TextStyle`.
- **(c) Corrective Code Strategy:** Pass `text = rubyMarkup, style = TextStyle(fontSize = ..., fontWeight = ...), rubyStyle = TextStyle(fontSize = ...)` to `FuriganaText` in both screens.
- **Outcome:** PASS (GATE G1 PASS)


### [R-0025] T3.3 · G2 · attempt 1/3 · 2026-11-02T17:18:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ConjugationDrillViewModelTest.kt:65:1 Class 'FakeSettingsRepository' is not abstract and does not implement abstract members:
fun observePlaybackSpeed(): Flow<Float>
suspend fun setPlaybackSpeed(speed: Float): Unit
```
- **(a) Root Cause:** `FakeSettingsRepository` in `ConjugationDrillViewModelTest.kt` omitted `observePlaybackSpeed` and `setPlaybackSpeed` from `SettingsRepository`.
- **(b) Faulty Assumption:** Assumed `SettingsRepository` did not contain audio playback speed methods.
- **(c) Corrective Code Strategy:** Implement `observePlaybackSpeed(): Flow<Float> = flowOf(1.0f)` and `suspend fun setPlaybackSpeed(speed: Float) {}` in `FakeSettingsRepository`.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0026] T3.4 · G2 · attempt 1/3 · 2026-11-02T17:30:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ContentSeederTest.seedRealAssets_allCountsMatchAndNoFkViolations: java.lang.AssertionError: Seeding real assets should succeed, was: Error(message=Class discriminator was missing and no default serializers were registered in the polymorphic scope of 'ExercisePayload' at path: $[0].payload
ContentValidationTest.validateClozeExercises: kotlinx.serialization.json.JsonDecodingException: Class discriminator was missing and no default serializers were registered in the polymorphic scope of 'ExercisePayload' at path: $[0].payload
```
- **(a) Root Cause:** `ExercisePayload` and `ClozeSegment` are sealed polymorphic interfaces serialized with discriminator key `"kind"`, but lacked the `@JsonClassDiscriminator("kind")` annotation, and `ContentSeeder.kt` and `ContentValidationTest.kt` created `Json` instances without setting `classDiscriminator = "kind"`.
- **(b) Faulty Assumption:** Assumed `kotlinx.serialization` would use `"kind"` as discriminator without explicit annotation or configuration.
- **(c) Corrective Code Strategy:** Add `@OptIn(ExperimentalSerializationApi::class) @JsonClassDiscriminator("kind")` to `ExercisePayload` and `ClozeSegment` in `ExercisePayload.kt`, and use `ContentJson` (which sets `classDiscriminator = "kind"`) in `ContentSeeder.kt` and `ContentValidationTest.kt`.
- **Outcome:** PASS (GATE G2 PASS)


### [R-0027] T3.4 · G1 · attempt 1/3 · 2026-11-02T17:36:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ParticleClozeScreen.kt:24:60 Unresolved reference 'MenuBook'.
ParticleClozeScreen.kt:399:42 Unresolved reference 'MenuBook'.
```
- **(a) Root Cause:** `Icons.AutoMirrored.Filled.MenuBook` is an extended material icon not present in core material icons.
- **(b) Faulty Assumption:** Assumed `MenuBook` was available in core Material 3 icons.
- **(c) Corrective Code Strategy:** Replace `Icons.AutoMirrored.Filled.MenuBook` with `Icons.Default.Info` in `ParticleClozeScreen.kt`.
- **Outcome:** PASS (GATE G1 PASS)


### [R-0028] T3.5 · G2 · attempt 1/3 · 2026-11-02T17:47:00+01:00
- **Signal (≤5 lines, verbatim):**
```
ContentValidationTest.kt:294:74 Unresolved reference 'SENTENCE_SCRAMBLE'.
```
- **(a) Root Cause:** `ContentValidationTest.kt` referenced `ExerciseType.SENTENCE_SCRAMBLE`, but the enum defined in `Enums.kt` is `ExerciseType.SCRAMBLE`.
- **(b) Faulty Assumption:** Assumed the enum value was `SENTENCE_SCRAMBLE` based on the ticket name without checking `Enums.kt`.
- **(c) Corrective Code Strategy:** Update `ContentValidationTest.kt` and `gen_scramble.py` to use `ExerciseType.SCRAMBLE` (matching `"type": "SCRAMBLE"`).
- **Outcome:** PASS (GATE G2 PASS)
