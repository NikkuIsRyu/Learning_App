# Reflexion Episodic Memory (`M_sr`)

When any Evaluator gate (`G0`–`G5`) fails, Gemini Flash MUST append a reflection entry below **before** modifying code or re-running the gate. See [`docs/02_HANDOFF_ROADMAP.md`](../docs/02_HANDOFF_ROADMAP.md) §1.2–1.3.

## Lessons (Cross-Ticket Rules Promoted from Past Reflections)
- **L-01**: Never use `@Insert(onConflict = OnConflictStrategy.REPLACE)` on parent content tables; SQLite executes `DELETE + INSERT`, triggering `ON DELETE CASCADE` on child tables (`grammar_examples`, `dialogue_turns`, `kanji_compounds`). Always use `@Upsert`.
- **L-02**: Never instantiate `hiltViewModel()` or Room databases inside `@Preview` functions; always preview the stateless `XxxScreen(state, onEvent)` composable with preview fixture data.
- **L-03**: Always pass `LocaleList("ja-JP")` for Japanese text and `LocaleList("zh-CN")` for Chinese text to prevent Han-unification glyph mismatches.
- **L-04**: Always quote workspace paths in PowerShell/CLI commands (`d:\.D Dokuments\Uni Stuff\Knowledge_App` contains spaces).

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
