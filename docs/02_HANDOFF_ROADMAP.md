# 02 — Gemini Flash Handoff Roadmap
## ReAct + Reflexion Implementation Plan

| Field | Value |
| :--- | :--- |
| Document | `docs/02_HANDOFF_ROADMAP.md` (spec: [`01_SYSTEM_ARCHITECTURE.md`](01_SYSTEM_ARCHITECTURE.md)) |
| Version | 1.0 — 2026-11-01 |
| Executor | **Gemini Flash** in Google Antigravity (Actor M_a) |
| Ledger | [`docs/PROGRESS.md`](PROGRESS.md) · Episodic memory: [`.system_generated/reflections.md`](../.system_generated/reflections.md) |

> [!IMPORTANT]
> Execute tickets **strictly in order**. A ticket is DONE only when every gate listed for it passes, the evidence is logged in `docs/PROGRESS.md`, and a commit exists. Never start ticket *n+1* with a red gate on ticket *n*.

---

## 1. Agent Cognition Protocol

### 1.1 ReAct Loop (Yao et al., ICLR 2023)

Each ticket is solved as an interleaved trajectory

$$\tau = (t_1, a_1, o_1, v_1,\; t_2, a_2, o_2, v_2,\; \dots)$$

| Symbol | Meaning | Concrete form in Antigravity |
| :--- | :--- | :--- |
| t_i — **Thought** | Short plan: what will change, which spec section it implements, which gate will prove it | 2–6 sentences in the response before tool calls |
| a_i — **Action** | One tool primitive | `write_file` (new file), `edit_file` (targeted change), `run_command` (build/test/CLI), `read_file` (inspect before editing) |
| o_i — **Observation** | The tool result, read critically | Gate verdict line, first compiler error, failing assertion, rendered PNG |
| v_i — **Verification** | Explicit check that o_i satisfies the ticket's DoD | `GATE G1 PASS`, `PREVIEW STATUS: completed`, assertion counts |

Rules:
1. **Read before write.** Before `edit_file`, `read_file` the exact region. Never edit from memory.
2. **One concern per Action batch.** Independent file writes may be batched; a gate command must never run in the same parallel batch as the edits it verifies.
3. **Small diffs.** Prefer several `edit_file` calls over rewriting large files.
4. **Observation discipline.** Quote at most 5 lines of the most specific error signal; the full log lives in `build/agent-logs/`.

### 1.2 Reflexion Triad (Shinn et al., NeurIPS 2023)

```mermaid
flowchart LR
    T["Ticket + Spec §"] --> MA["Actor M_a<br/>Gemini Flash<br/>(Kotlin · Compose · Room)"]
    MEM[("Episodic memory<br/>reflections.md<br/>(ticket window + Lessons)")] --> MA
    MA -->|"trajectory τ_k"| ME{"Evaluator M_e<br/>G0 static · G1 compile · G2 tests<br/>G3 preview · G4 device · G5 lint"}
    ME -->|"PASS"| DONE["Commit + PROGRESS.md"]
    ME -->|"FAIL + signal"| MSR["Self-Reflector M_sr<br/>(a) Root Cause<br/>(b) Faulty Assumption<br/>(c) Corrective Strategy"]
    MSR -->|"append entry"| MEM
    MSR -->|"k ≥ 3"| ESC["STOP → escalate to user"]
```

Formally, at trial k the Actor acts with policy π(a | ticket, mem_k). The Evaluator returns r_k = M_e(τ_k) ∈ {PASS, FAIL(signal)}. On FAIL the Self-Reflector produces sr_k = M_sr(τ_k, r_k) and

$$\text{mem}_{k+1} = \text{mem}_k \cup \{sr_k\}, \qquad |\text{window}| \le 3 \text{ most recent entries for this ticket} + \text{Lessons}$$

**Retry budget:** at most **3 reflections per ticket gate**. On the 3rd failure: stop, mark the ticket `BLOCKED` in `PROGRESS.md`, and report to the user with the three reflections summarised.

### 1.3 The Self-Reflection Routine (MUST, on every gate failure)

When any gate fails, the Actor MUST NOT re-run or blindly retry. It runs this routine:

1. **Recall:** `Select-String -LiteralPath .system_generated/reflections.md -Pattern "T<id>" -Context 0,10` and read the `## Lessons` section.
2. **Diagnose:** open the log path printed by the gate; locate the first causal error (not the cascade); `read_file` the referenced `file:line`.
3. **Reflect:** append an entry (template below) with `write_file` (`Append: true`) to `.system_generated/reflections.md`.
4. **Differentiate:** if the new Corrective Strategy matches a previous one for this ticket, choose a different one.
5. **Act:** apply the minimal fix; re-run **the same gate**.
6. **Promote:** if the same trap appears in two different tickets, add a one-line rule to `## Lessons`.

```markdown
### [R-NNNN] T<id> · <gate> · attempt <k>/3 · <ISO-8601 timestamp>
- **Signal (≤5 lines, verbatim):** `e: file:///…/Conjugator.kt:42:17 Unresolved reference 'Te'`
- **(a) Root Cause:** <mechanism that produced the failure, not the symptom>
- **(b) Faulty Assumption:** <which belief in my previous Thought was false>
- **(c) Corrective Code Strategy:** <minimal concrete change + why it fixes (a)>
- **Outcome:** <filled in after re-run: PASS | FAIL → R-NNNN+1>
```

**Universal Error-Recovery Prompt** (the Actor pastes this into its own Thought on every failure):

> *REFLEXION TRIGGER — Gate {G} failed on {ticket} (attempt {k}/3). I will not retry blindly. I will (1) quote the single most specific error line, (2) read the referenced code, (3) write Root Cause / Faulty Assumption / Corrective Strategy to reflections.md, (4) confirm my strategy differs from earlier attempts, (5) apply the minimal fix and re-run only gate {G}. I will not disable tests, add @Ignore, weaken assertions, skip tasks with `-x`, suppress warnings into errors-off, use destructive migrations, downgrade SDK levels, or invent dependency versions.*

Infrastructure flakes (Gradle daemon crash, file lock, sidecar timeout) still get a reflection entry, with Root Cause = infrastructure; one plain re-run is then allowed.

### 1.4 Evaluator Gates (M_e)

| Gate | Command (Windows PowerShell) | PASS signal |
| :--- | :--- | :--- |
| G0 static | `Test-Path`, `Select-String`, PowerShell/JSON parse checks (ticket-specific) | Ticket-specific |
| G1 compile | `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G1 -Ticket T<id>` | `GATE G1 PASS` |
| G2 unit tests | `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G2 -Ticket T<id>` (optionally `-Tasks ":core:domain:test"`) | `GATE G2 PASS` |
| G3 preview | `powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <your conversation id> -Reference "gradle://.?:app@debug" -Fqn "<fqn1>,<fqn2>"` then `read_file` the PNG(s) | `PREVIEW STATUS: completed` + visual check |
| G4 device | `android emulator list` → `android emulator start <avd>` → `android run --apks=app/build/outputs/apk/debug/app-debug.apk` → `android layout --pretty` / `android screen capture` | Expected texts present in layout JSON |
| G5 lint/schema | `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G5 -Ticket T<id>` | `GATE G5 PASS` |

On Linux (Omarchy) replace `powershell -NoProfile -File` with `pwsh -File`; the scripts call `./gradlew` automatically.

> [!NOTE]
> **Compose Preview gate rules.** `android preview render` is executed **by the Compose Preview sidecar**, not by the Actor. The FQNs listed in each ticket are the user-approved previews to render (this document is that approval). Never trigger a render in the same tool batch as code edits. If the sidecar is not running, `preview.ps1` exits 2 with `PREVIEW BLOCKED` → ask the user to open the **Android Compose Preview** pane, mark the ticket `BLOCKED-G3`, and continue only after it is available. Before G4, read the android-cli skill references `interact.md` and `journeys.md`.

### 1.5 Ticket Lifecycle

```mermaid
stateDiagram-v2
    [*] --> TODO
    TODO --> IN_PROGRESS: read ticket, spec §, reflections
    IN_PROGRESS --> VERIFYING: actions complete
    VERIFYING --> IN_PROGRESS: gate FAIL → reflection (k < 3)
    VERIFYING --> BLOCKED: gate FAIL, k = 3 / missing user input
    VERIFYING --> DONE: all gates PASS → commit
    BLOCKED --> IN_PROGRESS: user unblocks
    DONE --> [*]
```

Commit convention after each DONE ticket: `git add -A; git commit -m "feat(T3.2): rule-based conjugator with golden tests"`. Push at the end of each phase (`git push origin main`); if SSH auth fails, stop and ask the user — never modify keys or credentials.

### 1.6 Hard Rules (any violation = ticket invalid)

1. No `fallbackToDestructiveMigration`, no `@Insert(onConflict = REPLACE)` on content tables.
2. No `hiltViewModel()` or database access inside `@Preview` functions.
3. No invented library versions (spec §11).
4. `firestore.rules` is authored only by the `firestore-rules-author` subagent.
5. Never fabricate linguistic content (readings, JLPT levels, verb groups). Unknown ⇒ `null` + `review_queue.json`.
6. Never commit `google-services.json` to a public remote, keystores, or tokens.
7. Quote all paths (the workspace path contains spaces: `d:\.D Dokuments\Uni Stuff\…`).

---

## 2. Harness Scripts (created in T0.3)

### 2.1 `tools/agent/gate.ps1`

```powershell
<#
.SYNOPSIS  Evaluator (M_e) wrapper: runs a Gradle gate, stores the full log, prints a compact verdict.
.EXAMPLE   powershell -NoProfile -File tools/agent/gate.ps1 -Gate G2 -Ticket T3.2 -Tasks ":core:domain:test"
#>
param(
    [Parameter(Mandatory = $true)][ValidateSet('G1', 'G2', 'G5')][string]$Gate,
    [string]$Ticket = 'adhoc',
    [string]$Tasks = ''
)
$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location -LiteralPath $root
$logDir = Join-Path $root 'build/agent-logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$log = Join-Path $logDir ("{0}-{1}-{2}.log" -f $Ticket, $Gate, (Get-Date -Format 'yyyyMMdd-HHmmss'))
$gradlew = if ($env:OS -eq 'Windows_NT') { Join-Path $root 'gradlew.bat' } else { Join-Path $root 'gradlew' }
if (-not (Test-Path -LiteralPath $gradlew)) { Write-Output "GATE $Gate FAIL ($Ticket) gradle wrapper missing"; exit 1 }
$taskList = if ($Tasks) { $Tasks -split '\s+' } else {
    switch ($Gate) {
        'G1' { @('assembleDebug') }
        'G2' { @('testDebugUnitTest', ':core:domain:test') }
        'G5' { @('lintDebug') }
    }
}
$start = Get-Date
& $gradlew @taskList --console=plain *> $log
$code = $LASTEXITCODE
if ($code -eq 0) { Write-Output "GATE $Gate PASS ($Ticket) log=$log"; exit 0 }

Write-Output "GATE $Gate FAIL ($Ticket) exit=$code log=$log"
Write-Output '--- build signals (first 40) ---'
Select-String -LiteralPath $log -Pattern '^e: ', 'error:', 'What went wrong', 'Caused by:', 'Unresolved reference', 'FAILED' |
    Select-Object -First 40 | ForEach-Object { $_.Line }
Write-Output '--- failing tests (first 20) ---'
$dirs = @('app', 'core') | ForEach-Object { Join-Path $root $_ } | Where-Object { Test-Path -LiteralPath $_ }
Get-ChildItem -LiteralPath $dirs -Recurse -Filter 'TEST-*.xml' -ErrorAction SilentlyContinue |
    Where-Object { $_.LastWriteTime -gt $start } |
    ForEach-Object {
        [xml]$x = Get-Content -LiteralPath $_.FullName -Raw
        foreach ($tc in $x.testsuite.testcase) {
            if ($tc.failure) { '{0}.{1}: {2}' -f $tc.classname, $tc.name, (($tc.failure.message -split "`n")[0]) }
        }
    } | Select-Object -First 20
exit 1
```

### 2.2 `tools/agent/preview.ps1`

```powershell
<#
.SYNOPSIS  Evaluator G3: asks the Compose Preview sidecar to run `android preview render` and reports the job.
.EXAMPLE   powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <id> -Reference "gradle://.?:app@debug" -Fqn "a.b.FooKt.FooPreview,a.b.BarKt.BarPreview"
.EXAMPLE   powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <id> -Rerender
#>
param(
    [Parameter(Mandatory = $true)][string]$ConversationId,
    [string]$Fqn = '',
    [string]$Reference = 'gradle://.?:app@debug',
    [switch]$Rerender
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$androidHome = if ($env:ANDROID_USER_HOME) { $env:ANDROID_USER_HOME } else { Join-Path $HOME '.android' }
$infoFile = Join-Path $androidHome 'cli/sidecars/compose-preview.json'
if (-not (Test-Path -LiteralPath $infoFile)) { Write-Output 'PREVIEW BLOCKED: sidecar not running. Ask the user to open the "Android Compose Preview" pane.'; exit 2 }
$base = (Get-Content -LiteralPath $infoFile -Raw | ConvertFrom-Json).url
try { Invoke-RestMethod -Uri "$base/api/status" -TimeoutSec 10 | Out-Null }
catch { Write-Output 'PREVIEW BLOCKED: sidecar not responding. Ask the user to open the pane.'; exit 2 }
$headers = @{ 'X-Sidecar-Conversation' = $ConversationId }
if ($Rerender) {
    $res = Invoke-RestMethod -Method Post -Uri "$base/api/render" -Headers $headers -ContentType 'application/json' -Body '{"wait":true}' -TimeoutSec 1800
} else {
    $fqns = @($Fqn -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    if ($fqns.Count -eq 0) { Write-Output 'PREVIEW ERROR: -Fqn required for a new session'; exit 1 }
    $body = @{ project_dir = $root; reference = $Reference; fqns = $fqns; render = $true; wait = $true } | ConvertTo-Json -Depth 5
    $res = Invoke-RestMethod -Method Post -Uri "$base/api/session" -Headers $headers -ContentType 'application/json' -Body $body -TimeoutSec 1800
}
$job = if ($res.job) { $res.job } else { $res }
Write-Output "PREVIEW STATUS: $($job.status)"
if ($job.status -ne 'completed') {
    if ($job.error) { Write-Output "ERROR: $($job.error)" }
    $job.output | Select-Object -Last 40
    exit 1
}
$job.images | ForEach-Object { "IMAGE: $_" }
exit 0
```

After `PREVIEW STATUS: completed`, the Actor MUST `read_file` at least one listed PNG and state in its Verification what it sees (layout, text, colours) versus the ticket's expectation. For accessibility checks, read the sibling `<image>.json` component tree (semantics: `contentDescription`, `role`, `text`).

---

## 3. Phase Overview

```mermaid
flowchart LR
    P0["Phase 0<br/>Harness & Repo"] --> P1["Phase 1<br/>Scaffolding"] --> P2["Phase 2<br/>Data Core"] --> P3["Phase 3<br/>Japanese Module"] --> P4["Phase 4<br/>Chinese Module"] --> P5["Phase 5<br/>Polish & Sync"]
```

| Phase | Tickets | Exit gate |
| :--- | :--- | :--- |
| 0 Harness & Repo | T0.1–T0.3 | G0: env doc, git remote, harness scripts parse |
| 1 Scaffolding | T1.1–T1.5 | G1 + G2 + G3 (shell, theme, vault) + G4 (4 nav items on device) |
| 2 Data Core | T2.1–T2.6 | G2 (DAO, seeder, SRS, content validation, errata) + G4 (seeded counts) |
| 3 Japanese | T3.1–T3.9 | G2 engines + G3 every screen + G4 chapter-27 journey |
| 4 Chinese | T4.1–T4.5 | G2 pinyin + G3 every screen + G4 dialogue playback |
| 5 Polish & Sync | T5.1–T5.8 | G2 sync + rules emulator tests + G5 lint + G4 release smoke |

Size legend: **S** ≤ 5 files · **M** 5–15 files · **L** > 15 files.

---

## 4. Phase 0 — Agent Harness & Repository Bootstrap

**Exact Goal & Context.** Establish a verifiable working environment before any Kotlin is written: known toolchain, git wired to `git@github.com:NikkuIsRyu/Learning_App.git`, evaluator scripts, and the Reflexion memory file. The workspace root is `d:\.D Dokuments\Uni Stuff\Knowledge_App` and already contains `AGENTS.md`, `docs/`, and `.system_generated/reflections.md` — these MUST be preserved.

### T0.1 — Toolchain probe `[S]` `[G0]`
- **Thought:** I must know what exists before scaffolding: the `android` CLI, JDK 17+, Git, Node 22+, uv. Missing tools are reported, not silently installed.
- **Action 1 (`run_command`):** `android --version; android info; java -version; git --version; node -v; uv --version`
- **Action 2 (`write_file`):** `docs/ENVIRONMENT.md` with a table: tool, version, path, status (OK / MISSING), OS, workspace path.
- **Observation:** versions printed; `android info` shows SDK location.
- **Verification (G0):** `Select-String -LiteralPath docs/ENVIRONMENT.md -Pattern 'android','java','git','node','uv'` returns 5 matches.
- **DoD:** ENVIRONMENT.md complete. If `android` is missing → STOP and ask the user to approve the documented installer from the android-cli skill. If Java < 17 → STOP and ask.
- **Reflexion Guardrails:** *Trap:* assuming `java` on PATH is the one Gradle uses → Gradle uses `JAVA_HOME`/`org.gradle.java.home`; record both. *Recovery prompt:* "Which tool's absence caused this, and is installing it within my authority, or must I ask the user?"

### T0.2 — Git wiring `[S]` `[G0]`
- **Thought:** The folder is not yet a repository. The remote may be empty or may already have commits; I must check before committing to avoid divergent histories.
- **Action 1 (`run_command`):** `git init -b main; git remote add origin git@github.com:NikkuIsRyu/Learning_App.git; git ls-remote origin`
- **Action 2:** If `ls-remote` lists refs → `git fetch origin; git merge origin/main --allow-unrelated-histories` (stop and ask on any conflict). If empty → continue.
- **Action 3 (`write_file`):** `.gitignore` containing at least: `build/`, `.gradle/`, `local.properties`, `*.keystore`, `*.jks`, `.idea/`, `captures/`, `.kotlin/`, `app/google-services.json`, `tools/content-etl/node_modules/`, `tools/content-etl/.venv/`, `tools/content-etl/.cache/`, `content-private/`.
- **Action 4 (`run_command`):** `git add -A; git commit -m "chore(T0.2): architecture spec, roadmap, agent rules"`
- **Verification (G0):** `git remote -v` shows origin; `git status --porcelain` is empty; `git log --oneline -1` shows the commit.
- **Reflexion Guardrails:** *Trap:* SSH auth failure on `ls-remote` → do NOT generate keys or edit SSH config; record BLOCKED and ask the user. *Trap:* `.system_generated/` ignored by a global gitignore → verify with `git check-ignore -v .system_generated/reflections.md` (it MUST be tracked).

### T0.3 — Evaluator harness `[S]` `[G0]`
- **Thought:** Gates must produce compact, deterministic verdicts so my context is not flooded by Gradle output.
- **Action 1 (`write_file`):** `tools/agent/gate.ps1` and `tools/agent/preview.ps1` exactly as in §2.
- **Action 2 (`run_command`):** parse check:
  `foreach ($f in 'tools/agent/gate.ps1','tools/agent/preview.ps1') { $e=$null; [void][System.Management.Automation.Language.Parser]::ParseFile((Resolve-Path $f), [ref]$null, [ref]$e); "$f errors=$($e.Count)" }`
- **Action 3 (`run_command`):** `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G1 -Ticket T0.3` → expected `GATE G1 FAIL (T0.3) gradle wrapper missing` (proves the failure path works).
- **Verification (G0):** both files `errors=0`; the expected FAIL line appears.
- **Reflexion Guardrails:** *Trap:* execution policy blocks `-File` → do not change policy yourself; ask the user how they want scripts to run. *Trap:* Windows PowerShell 5.1 vs 7 syntax (no `??`, no `$IsWindows`) — the scripts are 5.1-compatible; keep them so.

**Phase 0 exit:** PROGRESS.md shows T0.1–T0.3 DONE; commit `chore(phase-0)`; push.

---

## 5. Phase 1 — Scaffolding

**Exact Goal & Context.** A compiling 4-module Compose app (`:app`, `:core:domain`, `:core:data`, `:core:designsystem`) with Hilt, the M3 adaptive top-level navigation (Today · 日本語 · 中文 · Vault), the design system (domain themes, CJK typography, furigana and pinyin primitives), and the finished Vault WIP screen. Spec: §3, §10, §11.

### T1.1 — Generate the Android project `[M]` `[G1]`
- **Thought:** `android create --force` overwrites files in a non-empty folder, which could clobber `AGENTS.md`/`docs`. I will generate into a temp folder and copy without overwriting.
- **Action 1 (`run_command`):** `android create --list` (pick the Compose empty-activity template, typically `empty-activity`).
- **Action 2 (`run_command`):** `android create empty-activity --name="Learning App" --application-id=tech.nikku.learningapp --namespace=tech.nikku.learningapp --min-sdk=26 --output="./.scaffold_tmp"`
- **Action 3 (`run_command`):** copy `.scaffold_tmp\*` into the root **skipping existing files** (`Get-ChildItem -LiteralPath .scaffold_tmp -Force | ForEach-Object { … Copy-Item … if (-not (Test-Path $dest)) }`), merge the template's `.gitignore` lines into ours, then remove `.scaffold_tmp`.
- **Action 4 (`edit_file`):** `gradle.properties`: ensure `org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8`, `android.useAndroidX=true`.
- **Observation:** `gradlew.bat`, `settings.gradle.kts`, `app/`, `gradle/libs.versions.toml` exist.
- **Verification:** G1 → `GATE G1 PASS`.
- **DoD:** template app builds; record AGP/Kotlin/Compose BOM versions from the catalog into `docs/ENVIRONMENT.md`.
- **Reflexion Guardrails:** *Trap:* SDK platform missing → `android sdk install platforms/android-<N>` with the compileSdk from the template (record it). *Trap:* path-with-spaces errors in scripts → quote. *Recovery focus:* "Is the failure from the template itself (environment) or from my copy step (missing file)?" — compare against `.scaffold_tmp` before deleting it (delete only after G1 PASS).

### T1.2 — Module split & dependency stack `[L]` `[G1, G2]`
- **Thought:** Introduce `:core:domain` (Kotlin/JVM), `:core:data` and `:core:designsystem` (Android libraries) and wire Hilt, KSP, Room plugin, kotlinx-serialization, Navigation, adaptive navigation suite, Paging, DataStore, WorkManager, Media3 — all versions resolved from official Maven metadata (spec §11), never guessed.
- **Action 1 (`run_command`):** for each new artifact, fetch the latest stable version, e.g. `Invoke-RestMethod "https://dl.google.com/dl/android/maven2/androidx/room/room-runtime/maven-metadata.xml"` and `Invoke-RestMethod "https://repo1.maven.org/maven2/com/google/dagger/hilt-android/maven-metadata.xml"`; select the newest version without `alpha|beta|rc`. KSP must be compatible with the catalog's Kotlin version (check KSP release notes / metadata).
- **Action 2 (`edit_file`):** `gradle/libs.versions.toml` — add versions, libraries, plugins (aliases: `hilt`, `ksp`, `room`, `kotlin-serialization`, `kotlin-jvm`).
- **Action 3 (`write_file`):** `core/domain/build.gradle.kts` (plugins `kotlin-jvm`, `kotlin-serialization`; deps coroutines-core, serialization-json; test junit4, coroutines-test, turbine), `core/data/build.gradle.kts` (android-library, ksp, hilt, room plugin with `room { schemaDirectory("$projectDir/schemas") }`, serialization), `core/designsystem/build.gradle.kts` (android-library + compose). Add `include(":core:domain", ":core:data", ":core:designsystem")` to `settings.gradle.kts`.
- **Action 4 (`write_file`):** `LearningApplication.kt` (`@HiltAndroidApp`), register in manifest; `@AndroidEntryPoint` on `MainActivity`.
- **Action 5 (`write_file`):** one smoke test per module (`core/domain/src/test/.../SmokeTest.kt`, `core/data/src/test/.../SmokeTest.kt`).
- **Verification:** G1 PASS, then G2 PASS.
- **Reflexion Guardrails:**
  - *Trap:* template uses AGP built-in Kotlin and you add `org.jetbrains.kotlin.android` → "plugin already applied / built-in Kotlin" error → remove it.
  - *Trap:* `ksp` version mismatch → "ksp-X is too old for kotlin-Y" → pick the KSP release compatible with the catalog Kotlin.
  - *Trap:* Hilt incompatible with template AGP after 2 reflections → apply pre-approved ADR-004 fallback: manual `AppContainer` + `viewModelFactory {}`; log the decision in reflections `## Lessons`.
  - *Trap:* compose compiler plugin missing in a Compose library module → add `kotlin-compose` plugin alias there.

### T1.3 — Design system & CJK primitives `[M]` `[G2, G3]`
- **Thought:** Text rendering is the heart of a language app. `RubyParser` (pure, in `:core:domain/engine/text`) must be property-tested; `FuriganaText` and `PinyinText` live in `:core:designsystem`. Japanese/Chinese locale hints prevent Han-unification glyph errors (spec §10.2).
- **Action 1 (`write_file`):** `core/domain/.../engine/text/RubyParser.kt` (`parse`, `render`, `toKanaOnly`, plus Anki/bracket-style importers) and `Pinyin.kt` (numbered↔diacritic, `toneOf(syllable)`).
- **Action 2 (`write_file`):** tests: `RubyParserTest` (round-trip `render(parse(s)) == s` on ≥ 20 samples incl. `{窓|まど}が{閉|し}めて あります`, Anki ` 開[あ]きます`), `PinyinTest` (`hao3→hǎo`, `lv4→lǜ`, `xiu1→xiū`, `gui4→guì`, `nv3→nǚ`, neutral `ma5→ma`).
- **Action 3 (`write_file`):** `core/designsystem/.../theme/{Color.kt, Theme.kt, Type.kt, DomainTheme.kt}` (domain seeds §10.1, `LearningTypography.japanese()/.chinese()` with `LocaleList`), `component/FuriganaText.kt` (modes FURIGANA / KANJI_ONLY / KANA_ONLY; `FlowRow` of segments, ruby above base, wraps by character), `component/PinyinText.kt` (tone colours, toggle), `component/FormulaPills.kt` (`List<PatternToken>` → coloured chips).
- **Action 4:** `@Preview`s: `FuriganaTextPreview` (all 3 modes), `PinyinTextPreview` (tone colours on/off, dark), `FormulaPillsPreview`.
- **Verification:** G2 (`-Tasks ":core:domain:test"`) PASS; G3 with `-Reference "gradle://.?:core:designsystem@debug" -Fqn "tech.nikku.learningapp.core.designsystem.component.FuriganaTextKt.FuriganaTextPreview,tech.nikku.learningapp.core.designsystem.component.PinyinTextKt.PinyinTextPreview,tech.nikku.learningapp.core.designsystem.component.FormulaPillsKt.FormulaPillsPreview"`; `read_file` PNGs: furigana is visually above the kanji, never above okurigana.
- **Reflexion Guardrails:** *Trap:* tone mark on wrong vowel (`guì` vs `gùi`) → rule: a/e first, then `o` in `ou`, else the **last** vowel. *Trap:* ruby wider than base breaks alignment → measure both, center the narrower. *Preview trap:* preview crashes with `NoSuchMethodError` → stale build; run G1 first, then `preview.ps1 -Rerender`.

### T1.4 — App shell & top-level navigation `[M]` `[G1, G3, G4]`
- **Thought:** Top-level domain switching via `NavigationSuiteScaffold` with type-safe routes; each destination gets a placeholder screen following the Route/Screen/Preview contract (spec §3.4–3.5).
- **Action 1 (`write_file`):** `navigation/TopLevelDestination.kt` (TODAY, JAPANESE "日本語", CHINESE "中文", VAULT; icons; content descriptions), `navigation/Routes.kt` (`@Serializable` objects/classes for every route in §3.5), `navigation/AppNavHost.kt`, `LearningAppRoot.kt` (scaffold + `NavHost`, `rememberSaveable` selected destination, preserves back stacks per tab).
- **Action 2 (`write_file`):** placeholder `TodayScreen`, `JapaneseHubScreen`, `ChineseHubScreen` each with previews.
- **Action 3 (`write_file`):** `values/strings.xml` + `values-de/strings.xml` (EN + DE UI strings).
- **Verification:** G1; G3 `-Fqn "tech.nikku.learningapp.LearningAppRootKt.LearningAppRootPreview"` (phone) + a `@Preview(device = Devices.TABLET)` variant showing a navigation rail; G4: `android run …` then `android layout --pretty` contains "Today", "日本語", "中文", "Vault".
- **Reflexion Guardrails:** *Trap:* `@Serializable` route without the serialization plugin in `:app` → "Serializer has not been found" → add plugin. *Trap:* back-stack loss on tab switch → use `popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true`.

### T1.5 — Vault "Work in Progress" screen `[S]` `[G2, G3]`
- **Thought:** Fully finished placeholder: content from `assets/content/vault/roadmap.json` (8 modules, spec §10.5); no DB.
- **Action 1 (`write_file`):** `core/data/src/main/assets/content/vault/roadmap.json`; `VaultRoadmapLoader` (kotlinx-serialization) + test.
- **Action 2 (`write_file`):** `feature/vault/VaultScreen.kt` (hero "Work in Progress", roadmap cards with `Planned/Drafting/Next up` badges, disabled "Notify me"), `VaultViewModel`, previews light/dark.
- **Verification:** G2 (loader test parses 8 modules); G3 `-Fqn "tech.nikku.learningapp.feature.vault.VaultScreenKt.VaultScreenPreview"`.
- **Reflexion Guardrails:** *Trap:* reading assets in a JVM unit test → keep the parser pure (`parse(json: String)`) and test it with a string; asset loading stays in the Android layer.

**Phase 1 exit:** all gates green; screenshots of the shell referenced in PROGRESS.md; push.

---

## 6. Phase 2 — Data Core

**Exact Goal & Context.** Implement the complete data foundation of spec §4–§7.5: domain models, Room schema exactly as specified, DAO tests, the content ETL from the client's Astro site/Anki deck/open datasets, the seeder, and the deterministic SRS scheduler. After this phase the app boots offline with all verified content in Room.

### T2.1 — Domain models & repository interfaces `[M]` `[G1, G2]`
- **Thought:** Domain types first (spec §5.2), pure Kotlin, so `:core:data` and UI compile against stable contracts.
- **Action (`write_file`):** `core/domain/.../model/*.kt` (enums, value types, `ExercisePayload`, `ClozeSegment`, UI-agnostic models `Word`, `Kanji`, `GrammarRule`, `Dialogue`, `ReviewItem`), `repository/*.kt` interfaces (`LexiconRepository`, `KanjiRepository`, `GrammarRepository`, `ExerciseRepository`, `DialogueRepository`, `ProgressRepository`, `ContentRepository`, `SettingsRepository`).
- **Action (test):** `ExercisePayloadSerializationTest` — round-trip every payload subtype with `classDiscriminator = "kind"`.
- **Verification:** G1, G2 (`-Tasks ":core:domain:test"`).
- **Reflexion Guardrails:** *Trap:* nested sealed `ClozeSegment` not serializable → each subclass needs `@Serializable` + `@SerialName`. *Trap:* domain importing `androidx.*` → forbidden by the dependency rule; move the type to `:core:data`.

### T2.2 — Room schema `[L]` `[G1, G0]`
- **Thought:** Transcribe spec §5.3–§5.8 verbatim (entities, relations, converters, DAOs, database, query builder). Schema export proves the schema compiles to SQL.
- **Action (`write_file`):** files under `core/data/.../local/{entity,relation,converter,dao}/`, `LearningDatabase.kt`, `LexiconQueryBuilder.kt`; Hilt `DatabaseModule` (`Room.databaseBuilder(ctx, LearningDatabase::class.java, LearningDatabase.NAME).build()` — **no** destructive fallback) providing every DAO.
- **Verification:** G1; G0: `Test-Path "core/data/schemas/tech.nikku.learningapp.core.data.local.LearningDatabase/1.json"`; G0 forbidden-pattern scan: `Select-String -Path core -Recurse -Pattern 'fallbackToDestructiveMigration','OnConflictStrategy.REPLACE'` returns nothing (use `Get-ChildItem -Recurse -Filter *.kt | Select-String …`).
- **Reflexion Guardrails:** *Trap:* "Cannot figure out how to save this field" → missing converter for that exact type (List<PatternToken> ≠ List<String>). *Trap:* FTS external-content column not found → FTS field names must exist in `WordEntity`. *Trap:* "Schema export directory not provided" → Room Gradle plugin block missing. *Trap:* KSP error points at a DAO query → read the SQL; enum literals are stored by `name` (`'VERB'`).

### T2.3 — DAO tests (Robolectric, JVM) `[M]` `[G2]`
- **Thought:** Prove the critical data guarantees on the JVM: upsert does not cascade, FTS works for meanings/pinyin, due queries respect suspension, CAS `markClean` works, log insert is idempotent.
- **Action (`write_file`):** `core/data/src/test/.../local/` tests with `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries()`, `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [<compileSdk>])`:
  1. `upsertParent_keepsChildren` (grammar rule upsert keeps examples; dialogue upsert keeps turns).
  2. `lexicon_ftsMatchesEnglishPrefixAndPinyinWithoutTones` (`"eat"` → 食べます; `"hao"` matches `hǎo`).
  3. `lexicon_kanaLikeAndChapterChips` (chapters {26, 27} + VERB + GODAN filter).
  4. `dueItems_excludesSuspendedAndNew`.
  5. `markClean_isCompareAndSet` (update row between read and markClean → returns 0, stays dirty).
  6. `reviewLogs_insertIgnoreIsIdempotent`.
  7. `kanjiWithCompounds_junctionResolves`.
- **Verification:** G2 (`-Tasks ":core:data:testDebugUnitTest"`).
- **Reflexion Guardrails:** *Trap:* Robolectric SDK not supported by the installed Robolectric → set `@Config(sdk = [34])` or the max supported. *Trap:* Paging `PagingSource` hard to assert → test the SQL via a non-paged `@RawQuery` twin used only in tests, or `PagingSource.load` with `LoadParams.Refresh`.

### T2.4 — Content ETL & seed JSON `[L]` `[G0, G2]`
- **Thought:** Content comes from the client's existing sources (spec §0, §6). Chinese data is TypeScript → export with Node/tsx; Japanese vocab/grammar from MDX tables → Python (uv); kanji/extended vocab from open datasets. Output must satisfy the validation invariants (spec §6.4).
- **Inputs (ask the user if not found):** `WEB_COMPANION_DIR` (default `d:\.D Dokuments\Uni Stuff\Website_project`), Anki `.apkg` path.
- **Action 1 (`write_file`):** `tools/content-etl/package.json` (+ `tsx`), `export-zh.mts`: dynamic-import `src/data/chineseGrammarData.ts` and `chineseDialogues.ts` via `pathToFileURL`, map to seed DTOs (`zh:g:*`, `zh:c:*`, `zh:d:*`; `chapter` from "Lektion N" / first number of "Lektionen 2–4"; `durationMs = durationS*1000`; turn `startMs/endMs`; `patternTokens` from formula split on `+` with a keyword→`SyntaxRole` table for DE/EN words like Subjekt/Subject, Zeit/Time, Ort/Place, Modal, Negation, Verb, Objekt/Object; unknown → `LITERAL` and logged), copy the 18 MP3s to `core/data/src/main/assets/media/zh/dialogues/`.
- **Action 2 (`write_file`):** `tools/content-etl/pyproject.toml` (uv) + `mdx_vocab.py`: parse `### NN.k 📖 Vocabulary Register` tables from EN + DE MDX (columns `#`, headword, reading, meaning), align by (chapter, #), build `rubyMarkup` by okurigana alignment (kana suffix/prefix matching; ambiguous → whole-word ruby + review queue), emit `ja/words_mnn_26_32.json` (`verified=true`, `source=MINNA`); parse `### NN.k` grammar sections into `ja/grammar_26_32.json` (`bodyMarkdown` = section body with MDX components stripped).
- **Action 3 (`write_file`):** `anki_extract.py`: unzip `.apkg`, decompress `collection.anki21b` (zstandard) → SQLite → print note types/fields **first** and ask the user to confirm the field mapping before emitting `ja/words_mnn_other.json` (`verified=false`) for chapters 1–25, 33–50.
- **Action 4 (`write_file`):** `kanji_build.py`: download KANJIDIC2, JMdict (multilingual, for `ger` glosses), KanjiVG release, KRADFILE (**EUC-JP encoded**) into `.cache/`; resolve `verbGroup`/`transitivity` for every MNN verb by kanji headword (`v5*` → GODAN, `v1` → ICHIDAN, `vs` → SURU, `vk` → KURU; `vt`/`vi`); compute `introducedChapter`; emit `ja/kanji.json`, `kanji_compounds.json`, `ja/review_queue.json`. JLPT tags only from a user-approved list; otherwise `null`.
- **Action 5 (`write_file`):** curated `ja/transitivity_pairs.json` for L29–30 (開く/開ける, 閉まる/閉める, つく/つける, 消える/消す, 壊れる/壊す, 割れる/割る, 折れる/折る, 破れる/破る, 汚れる/汚す, 掛かる/掛ける, 外れる/外す, 止まる/止める, 落ちる/落とす, 片付く/片付ける, 並ぶ/並べる, 入る/入れる, 出る/出す) → sets symmetric `pairWordId` where both exist.
- **Action 6 (`write_file`):** `manifest.json` generator (sha256 per file, `contentVersion` = 1).
- **Action 7 (`write_file`):** `core/data/src/test/.../content/ContentValidationTest.kt` (reads seed files from `src/main/assets/content` via the file system) implementing **every** invariant + the errata golden set of spec §6.4.
- **Verification:** G0: `ja/review_queue.json` reviewed with the user (list count in PROGRESS.md); G2 PASS (counts 45/41/54/51/48/39/57; errata assertions).
- **Reflexion Guardrails:** *Trap:* count mismatch → table rows with `|` inside cells or line breaks; parse by Markdown table grammar, not naive split. *Trap:* errata regression (e.g. `ひら` for 開きます) → the EN MDX is authoritative over Anki for 26–32; never overwrite verified rows with Anki data. *Trap:* Windows console mojibake → write files with UTF-8 explicitly (`encoding="utf-8"`, Node `utf8`); never round-trip through the console. *Recovery focus:* "Is the defect in the source, in my parser, or in the invariant? Only the user may change source truth."

### T2.5 — ContentSeeder & settings `[M]` `[G2, G4]`
- **Thought:** Seed on first launch / version bump inside one transaction using `@Upsert`, then `deleteStale` (spec §6.3). DataStore holds `contentVersion` and settings.
- **Action (`write_file`):** `content/ContentSeeder.kt` (reads manifest, verifies sha256, batch 500, ordered upserts, `db.withTransaction`), `content/dto/*` mirrors, `SettingsRepositoryImpl` (DataStore: furiganaMode, uiLanguage, dailyNewLimit, activeChapters = 26..32, playbackSpeed, toneColors), splash progress state in `MainActivity` (`installSplashScreen` optional).
- **Action (test):** Robolectric `ContentSeederTest`: seeds from a small fixture manifest; second run with same version is a no-op; version 2 fixture removes one word and keeps an existing `user_progress` row untouched.
- **Verification:** G2; G4: fresh install on emulator → `adb logcat -d -s ContentSeeder` (or an in-app debug line) reports `words=…, kanji=…, grammar=…, dialogues=18`.
- **Reflexion Guardrails:** *Trap:* FK violation during seed → wrong insert order; follow §6.3 order. *Trap:* seeding on main thread → ANR; run in `Dispatchers.IO`/WorkManager and show progress.

### T2.6 — SRS scheduler & review use cases `[M]` `[G2]`
- **Thought:** Deterministic SM-2-family scheduler (spec §7.5) is a pure function — essential for event-sourced sync replay (spec §8.4).
- **Action (`write_file`):** `engine/srs/{SrsConfig.kt, SrsSnapshot.kt, SrsScheduler.kt, LearningDay.kt}`; use cases `GradeAnswer` (writes log + projection in one transaction via `ProgressRepository.recordReview`), `StartReviewSession` (§7.6), `ReplayProgress(logs) → SrsSnapshot`.
- **Action (tests):** `SrsSchedulerTest`: new card GOOD→GOOD graduates to 1 d; REVIEW AGAIN lapses and EF −0.20 floored at 1.3; EASY bonus; interval cap; **determinism** (same inputs ⇒ identical output 1000×); **replay equivalence** (sequential `schedule` == `ReplayProgress(logs)`); learning-day cutoff 04:00.
- **Verification:** G2 (`-Tasks ":core:domain:test"`).
- **Reflexion Guardrails:** *Trap:* using `Random()` for fuzz → breaks determinism; derive from `seed`. *Trap:* timezone bugs → pass `ZoneId` explicitly; never call `ZoneId.systemDefault()` inside the engine.

**Phase 2 exit:** G2 green for all modules; G4 seeding verified; push.

---

## 7. Phase 3 — Japanese Module

**Exact Goal & Context.** Deliver the iMina replacement for Minna no Nihongo (spec §7.1–7.4, §10.3): chapter hub (26–32 verified), conjugation matrix drills, particle cloze, sentence builder, listening player, master lexicon, chapter kanji with stroke animation, and SRS reviews. Every screen follows the Route/Screen/Preview contract and passes G3.

### T3.1 — Chapter hub & chapter detail `[M]` `[G2, G3]`
- **Thought:** Entry point listing chapters 1–50 with status chips from `observeChapterCounts` + verified flags.
- **Action (`write_file`):** `feature/japanese/hub/{JapaneseHubRoute, JapaneseHubScreen, JapaneseHubViewModel}`; `feature/japanese/chapter/{ChapterRoute, ChapterScreen (tabs Grammar · Words · Kanji · Drills), ChapterViewModel}`; grammar detail renders `bodyMarkdown` (add a Compose markdown renderer library, version resolved per §11) and examples with `FuriganaText`.
- **Tests:** `JapaneseHubViewModelTest` with fake repositories (Turbine).
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.japanese.hub.JapaneseHubScreenKt.JapaneseHubScreenPreview,tech.nikku.learningapp.feature.japanese.chapter.ChapterScreenKt.ChapterScreenPreview"`; PNG shows chips "Verified" on 26–32.
- **Reflexion Guardrails:** *Trap:* markdown library pulls a conflicting Compose version → align via BOM or choose a renderer compatible with the BOM; reflect before swapping libraries.

### T3.2 — Conjugation engine `[M]` `[G2]`
- **Thought:** The generative core (spec §7.1–7.3): `Conjugator`, `AnswerNormalizer` with ending-diff explanation, `RomajiKana`. Pure Kotlin, exhaustively tested against the client's master table.
- **Action (`write_file`):** `engine/conjugation/{Conjugator.kt, KanaRows.kt, Exceptions.kt, Conjugation.kt}`, `engine/text/{RomajiKana.kt, KanaNormalizer.kt}`, `engine/exercise/AnswerNormalizer.kt`.
- **Action (tests):** `ConjugatorGoldenTest` — table-driven: 書く/話す/買う/待つ/飲む (GODAN), 食べる/見る/寝る (ICHIDAN), する, 来る × all 10 forms; exceptions 行く (行って/行った), ある (ない), いらっしゃる (いらっしゃいます); compounds 勉強する; surface **and** reading for 来る (来ない = こない). `RomajiKanaTest` (`kakeru→かける`, `kitte→きって`, `kon'ya→こんや`, `shinbun/shimbun→しんぶん`). `AnswerNormalizerTest` (katakana input, spaces, ら抜き accepted with note).
- **Verification:** G2 (`-Tasks ":core:domain:test"`).
- **Reflexion Guardrails:** *Trap:* operating on the kanji surface for 来る → reading changes (き/こ/く); conjugate the **reading** and rebuild the surface. *Trap:* godan う → a-row is **わ** (買わない), not あ. *Trap:* 行く te-form → exception table, not the く→いて rule.

### T3.3 — Conjugation Matrix & Drill UI `[L]` `[G2, G3]`
- **Thought:** Verb × form matrix from `conjugatableVerbs(activeChapters)`, mastery colour from `user_progress` (`conj:{wordId}:{form}`); drill card with IME/romaji input, instant diff feedback, auto-grade → `GradeAnswer`.
- **Action (`write_file`):** `feature/japanese/conjugation/{ConjugationMatrixRoute/Screen/ViewModel, ConjugationDrillRoute/Screen/ViewModel, components/MatrixCell.kt, components/KanaInputField.kt (live romaji→kana)}`.
- **Tests:** `ConjugationDrillViewModelTest` (correct → GOOD log; wrong → AGAIN with explanation; hint → HARD).
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.japanese.conjugation.ConjugationMatrixScreenKt.ConjugationMatrixScreenPreview,tech.nikku.learningapp.feature.japanese.conjugation.ConjugationDrillScreenKt.ConjugationDrillCorrectPreview,tech.nikku.learningapp.feature.japanese.conjugation.ConjugationDrillScreenKt.ConjugationDrillWrongPreview"`; PNG "Wrong" shows the highlighted ending and the rule text.
- **Reflexion Guardrails:** *Trap:* IME composition text submitted early → submit only on explicit action / IME `Done`, read `TextFieldValue.composition == null`. *Trap:* matrix with 50+ rows janky → `LazyColumn` of rows with `LazyRow` cells, stable `key`s.

### T3.4 — Particle cloze `[M]` `[G0, G2, G3]`
- **Thought:** Payload `ParticleCloze` (spec §5.2). Seed drafts for L26–32 derived from MDX example sentences (spaced bunsetsu ending in は/が/を/に/で/へ/と/も/から/まで/より/しか) with `verified=false`; per-wrong-option feedback references the grammar rule.
- **Action (`write_file`):** `tools/content-etl/gen_cloze.py` → `ja/exercises_cloze_26_32.json` (≥ 10 per chapter); `engine/exercise/ClozeGrader.kt` + test; `feature/japanese/particle/{ParticleClozeRoute/Screen/ViewModel}`.
- **Verification:** G0: generator output count ≥ 70 and contains no errata strings; G2 (grader + content validation); G3 `-Fqn "tech.nikku.learningapp.feature.japanese.particle.ParticleClozeScreenKt.ParticleClozeFeedbackPreview"`.
- **Reflexion Guardrails:** *Trap:* masking a non-particle (e.g. the が inside ながら) → mask only when the particle is the final kana(s) of a bunsetsu **and** the remainder is a known word/inflection; unsure ⇒ skip that sentence.

### T3.5 — Sentence builder (scramble) `[M]` `[G2, G3]`
- **Thought:** Chunks = spaced bunsetsu of example sentences; `acceptedOrders` starts with the source order; tap-to-place with `FlowRow`, drag optional.
- **Action (`write_file`):** `gen_scramble.py` → `ja/exercises_scramble_26_32.json`; `engine/exercise/ScrambleValidator.kt` + test (duplicate chunks, multiple accepted orders); `feature/japanese/scramble/{…Route/Screen/ViewModel}`.
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.japanese.scramble.SentenceBuilderScreenKt.SentenceBuilderPreview"`.
- **Reflexion Guardrails:** *Trap:* identical chunks (two `を`) break index-based validation → validate on chunk **indices of the original list**, compare normalized strings.

### T3.6 — Shared audio player & Japanese listening `[L]` `[G2, G3, G4]`
- **Thought:** One `AudioPlayerController` (Media3) for both languages: speed 0.5–1.5×, clip-based sentence replay, active-turn highlight, shadowing auto-pause; TTS fallback for Japanese sentences without audio (spec §9).
- **Action (`write_file`):** `feature/shared/player/{AudioPlayerController.kt, TtsController.kt, DialoguePlayerScreen.kt (generic, takes DialogueUi + display mode), TurnRow.kt, SpeedSlider.kt}`; `MediaRepository` (assets → files resolution); `feature/japanese/listening/{ListeningRoute/Screen/ViewModel}` with modes Furigana / Kanji-only / Kana-only / Romaji / Hidden.
- **Tests:** `TurnLocatorTest` (binary search position → turn); `ListeningViewModelTest` (mode switching, replay intent).
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.shared.player.DialoguePlayerScreenKt.DialoguePlayerJapanesePreview"`; G4: open a chapter listening set, press play, `android layout` shows the active-turn highlight state / speed label.
- **Reflexion Guardrails:** *Trap:* player leaks after leaving the screen → release in `DisposableEffect`/ViewModel `onCleared`. *Trap:* TTS voice data missing → show actionable message; never crash.

### T3.7 — Master Lexicon `[L]` `[G2, G3]`
- **Thought:** Paging 3 over `LexiconQueryBuilder` (spec §5.7) with chips: Chapter (multi), POS, Godan/Ichidan/Irregular, 自動詞/他動詞, JLPT, Verified-only; word detail with 自他 pair card and kanji links.
- **Action (`write_file`):** `LexiconRepositoryImpl` (normalizes query with `KanaNormalizer`, debounced 250 ms), `feature/japanese/lexicon/{LexiconRoute/Screen/ViewModel, WordDetailRoute/Screen, components/FilterChipsRow.kt, components/TransitivityPairCard.kt}`.
- **Tests:** `LexiconQueryBuilderTest` (pure SQL/arg assertions), `LexiconViewModelTest`.
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.japanese.lexicon.LexiconScreenKt.LexiconScreenPreview,tech.nikku.learningapp.feature.japanese.lexicon.WordDetailScreenKt.WordDetailPairPreview"`.
- **Reflexion Guardrails:** *Trap:* `LIKE` arguments in wrong order relative to `ORDER BY` placeholders → args list must follow placeholder order in the final SQL string. *Trap:* FTS syntax error from user input (`"`, `-`) → sanitizer in `toFtsPrefixQuery`.

### T3.8 — Chapter Kanji & stroke animation `[M]` `[G2, G3]`
- **Thought:** Grid of kanji where `introducedChapter = n`; detail with readings, radical/components, stroke count, verified compounds, animated KanjiVG strokes (spec §10.3).
- **Action (`write_file`):** `core/designsystem/component/StrokeOrderCanvas.kt` (parse `d` with `PathParser`, `PathMeasure.getSegment`, per-stroke `Animatable`, stroke numbers, play/step/loop), `feature/japanese/kanji/{ChapterKanjiRoute/Screen/ViewModel, KanjiDetailRoute/Screen/ViewModel}`; attribution entries for KanjiVG/KANJIDIC2.
- **Verification:** G2 (ViewModel test); G3 `-Reference "gradle://.?:core:designsystem@debug" -Fqn "tech.nikku.learningapp.core.designsystem.component.StrokeOrderCanvasKt.StrokeOrderStaticPreview"` then app previews `…kanji.KanjiDetailScreenKt.KanjiDetailPreview,…kanji.ChapterKanjiScreenKt.ChapterKanjiPreview`.
- **Reflexion Guardrails:** *Trap:* previews render the first animation frame (blank) → provide a `progress` parameter and preview at `progress = 1f`. *Trap:* scaling from 109×109 viewBox → apply a `Matrix` scale to the path, not to stroke width.

### T3.9 — Review session & Today screen `[M]` `[G2, G3, G4]`
- **Thought:** Generic `ReviewSession` host renders the right card per `ItemType`; Today shows due counts per domain (ticker re-subscription), streak, 12-week heatmap.
- **Action (`write_file`):** `feature/review/{ReviewSessionRoute/Screen/ViewModel, cards/MeaningRecallCard.kt, cards/KanjiReadingCard.kt}` (reuses drill composables), `feature/today/{TodayRoute/Screen/ViewModel, components/ActivityHeatmap.kt}`.
- **Verification:** G2 (`ReviewSessionViewModelTest`: interleaving 1 new : 4 due, new limit); G3 `-Fqn "tech.nikku.learningapp.feature.today.TodayScreenKt.TodayScreenPreview,tech.nikku.learningapp.feature.review.ReviewSessionScreenKt.ReviewSessionMeaningPreview"`; G4 journey: Japanese → Chapter 27 → Drills → answer 3 conjugations → Today due count changes.
- **Reflexion Guardrails:** *Trap:* due count frozen → Room `now` parameter snapshot; use a 60 s ticker + `flatMapLatest`.

**Phase 3 exit:** all Japanese previews in PROGRESS.md; G4 journey recorded (screenshot path); push.

---

## 8. Phase 4 — Chinese Module

**Exact Goal & Context.** Native port and enhancement of the Astro components (spec §10.4) over the seeded Chinese content (26 grammar rules, 6 confusable pairs, 18 dialogues with bundled audio).

### T4.1 — Chinese content verification & utilities `[S]` `[G2]`
- **Thought:** Confirm seeded counts and pinyin integrity before building UI.
- **Action (`write_file`):** extend `ContentValidationTest`: `zh` rules = 26, categories = 7, confusables = 6, dialogues = 18, every dialogue `audioRef` asset exists, every pinyin syllable parses with `Pinyin`.
- **Verification:** G2.
- **Reflexion Guardrails:** *Trap:* web data contains `ü`/`u:` variants or erhua → extend parser tests; do not edit source text silently.

### T4.2 — Grammar Explorer `[L]` `[G2, G3]`
- **Action (`write_file`):** `feature/chinese/grammar/{GrammarExplorerRoute/Screen/ViewModel, GrammarDetailRoute/Screen}`: category chips (7 + All), search, rule cards with `FormulaPills`, 3-layer aligned examples (hanzi / `PinyinText` / DE-EN by UI language), tone-colour toggle, pinyin active-recall (tap to reveal).
- **Verification:** G2 (ViewModel filter test); G3 `-Fqn "tech.nikku.learningapp.feature.chinese.grammar.GrammarExplorerScreenKt.GrammarExplorerPreview,tech.nikku.learningapp.feature.chinese.grammar.GrammarDetailScreenKt.GrammarDetailTonePreview"`; PNG shows coloured pills and tone colours.
- **Reflexion Guardrails:** *Trap:* Japanese glyphs for Chinese text → apply `LearningTypography.chinese()`.

### T4.3 — Grammar Cards `[M]` `[G2, G3]`
- **Action (`write_file`):** `feature/chinese/cards/{GrammarCardsRoute/Screen/ViewModel, FlipCard.kt}` — flip animation, self-grading with predicted intervals, SRS `ItemType.GRAMMAR`.
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.chinese.cards.GrammarCardsScreenKt.GrammarCardFrontPreview,tech.nikku.learningapp.feature.chinese.cards.GrammarCardsScreenKt.GrammarCardBackPreview"`.
- **Reflexion Guardrails:** *Trap:* `graphicsLayer { rotationY }` shows mirrored back face → swap content at 90° and counter-rotate.

### T4.4 — Confusable Pairs `[M]` `[G2, G3]`
- **Action (`write_file`):** `feature/chinese/confusables/{ConfusablesRoute/Screen/ViewModel, ConfusableCard.kt, ConfusableQuiz.kt}` — side-by-side (≥ 600 dp) / stacked (< 600 dp), ✓/✕ examples, rule, mnemonic, high-contrast toggle, quiz mode (`CONFUSABLE_CHOICE`).
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.chinese.confusables.ConfusablesScreenKt.ConfusablePhonePreview,tech.nikku.learningapp.feature.chinese.confusables.ConfusablesScreenKt.ConfusableTabletPreview"`.
- **Reflexion Guardrails:** *Trap:* ✓/✕ conveyed by colour only → include icons + text labels (verify in component tree semantics).

### T4.5 — Chinese Dialogue Player `[M]` `[G2, G3, G4]`
- **Action (`write_file`):** `feature/chinese/dialogues/{DialogueListRoute/Screen/ViewModel, ChineseDialogueRoute}` reusing `DialoguePlayerScreen` with speaker colour chips, hanzi + tone pinyin + translation, per-turn replay from timestamps.
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.shared.player.DialoguePlayerScreenKt.DialoguePlayerChinesePreview,tech.nikku.learningapp.feature.chinese.dialogues.DialogueListScreenKt.DialogueListPreview"`; G4: play `Dialogue2_1`, tap turn 3 → playback position ≈ 13.8 s (timestamp from source).
- **Reflexion Guardrails:** *Trap:* `ClippingConfiguration` end beyond duration → clamp to `durationMs`.

**Phase 4 exit:** push; PROGRESS.md lists all Chinese previews.

---

## 9. Phase 5 — Polish & Sync

**Exact Goal & Context.** Add Firebase Auth + Firestore replication per spec §8 without compromising offline behaviour, the feedback module, media packs, and release quality. Firebase configuration requires user-owned assets.

### T5.1 — Firebase project wiring `[S]` `[G1]` — **USER GATE**
- **Thought:** I cannot create credentials. I need the user's Firebase project and `app/google-services.json` (package `tech.nikku.learningapp`) plus the debug SHA-1 registered for Google Sign-In.
- **Action 1:** Read the `firebase-basics` skill; propose CLI steps (`firebase login`, `firebase projects:list`, `firebase apps:sdkconfig android …`) and **ask the user** to run/approve them. Print the debug SHA-1 via `.\gradlew.bat signingReport` for the user to register.
- **Action 2 (`edit_file`):** add google-services plugin + Firebase BoM, `firebase-auth`, `firebase-firestore` to the catalog/`:app`/`:core:data`.
- **Verification:** G1 PASS with the real `google-services.json` (gitignored).
- **Reflexion Guardrails:** missing file ⇒ `BLOCKED` (never fabricate a placeholder config).

### T5.2 — Authentication `[M]` `[G2, G3]`
- **Action (`write_file`):** `feature/auth/{AccountRoute/Screen/ViewModel}`, `AuthRepository` (Credential Manager `GetGoogleIdOption` → `GoogleAuthProvider.getCredential(idToken)`; optional anonymous sign-in with later `linkWithCredential`), sync status display. App remains fully usable signed-out.
- **Verification:** G2 (ViewModel with fake `AuthRepository`); G3 `-Fqn "tech.nikku.learningapp.feature.auth.AccountScreenKt.AccountSignedOutPreview,tech.nikku.learningapp.feature.auth.AccountScreenKt.AccountSignedInPreview"`.
- **Reflexion Guardrails:** *Trap:* `NoCredentialException` on emulator without a Google account → handled UI state, not a crash.

### T5.3 — Firestore security rules `[M]` `[rules emulator tests]` — **DELEGATED**
- **Thought:** Rules are security-critical and MUST be written by the `firestore-rules-author` subagent.
- **Action (`invoke_subagent`):** TypeName `firestore-rules-author`; prompt = spec §8.2 data model + §8.7 requirement list verbatim + "include emulator unit tests". Store results as `firestore.rules`, `firebase.json` (emulators), `tools/rules-tests/`.
- **Verification:** the subagent's emulator test run passes; the Actor re-runs it once (`npm test` in `tools/rules-tests`) and records the result.
- **Reflexion Guardrails:** the Actor does not hand-edit rules; disagreements go back to the subagent via `send_message`.

### T5.4 — SyncWorker `[L]` `[G2, G4]`
- **Thought:** Implement spec §8.3–8.6 exactly: push logs → push progress (CAS) → pull logs → replay → pull progress (LWW suspended) → cursors.
- **Action (`write_file`):** `sync/{SyncWorker.kt (@HiltWorker), SyncScheduler.kt, RemoteStore.kt (interface), FirestoreRemoteStore.kt, FakeRemoteStore.kt (tests), SyncCursorStore.kt (DataStore), SyncStatus.kt}`; `LearningApplication` implements `Configuration.Provider` with `HiltWorkerFactory`; remove the default WorkManager initializer in the manifest (`tools:node="remove"` on `androidx.work.WorkManagerInitializer` meta-data inside `InitializationProvider`). Firestore configured with memory cache.
- **Tests (Robolectric + `work-testing`):** two simulated devices sharing a `FakeRemoteStore` review the same item offline in different orders → after sync both have identical `user_progress`; CAS miss keeps row dirty; permission error → `Result.failure()`; network error → `Result.retry()`; replay respects `resetAt`.
- **Verification:** G2; G4 with real Firebase: review 5 items on device, trigger sync, observe documents in the Firebase console (ask the user to confirm).
- **Reflexion Guardrails:** *Trap:* "WorkManager is already initialized" / Hilt worker not created → default initializer still present. *Trap:* unbounded `IN (:ids)` → chunk 500. *Trap:* clock-based LWW used for SRS fields → forbidden; SRS fields come from replay only.

### T5.5 — Media packs (Tier B) `[M]` `[G2]` *(optional — confirm with user)*
- **Action (`write_file`):** `tools/media/sync_media.ps1` (Drive vault → assets + `media-manifest.json` with sha256), `media/MediaDownloadWorker.kt` (download to temp, verify sha256, atomic rename), pack status UI in Settings.
- **Verification:** G2 (`MediaRepositoryTest` with a local HTTP fixture or fake downloader).

### T5.6 — Feedback module `[S]` `[G2, G3]`
- **Action (`write_file`):** `feature/feedback/{FeedbackSheet.kt, FeedbackViewModel.kt}`, entry points (overflow menu, "Report erratum" on word/exercise pre-filling `itemKey`), `FeedbackRepository` (local queue → SyncWorker pushes to `feedback/`).
- **Verification:** G2; G3 `-Fqn "tech.nikku.learningapp.feature.feedback.FeedbackSheetKt.FeedbackSheetPreview"`.

### T5.7 — Quality pass `[M]` `[G3, G5]`
- **Action:** add `fontScale = 1.3f` and dark previews to key screens; fix every clickable without `contentDescription`/text found in component trees; Settings → "Licenses & Attribution" (JMdict, KANJIDIC2, KanjiVG, KRADFILE, JLPT list, GPT-SoVITS voices); remove unused resources.
- **Verification:** G3 on all key previews (font-scale variants show no clipping); G5 `GATE G5 PASS` (lint errors = 0; warnings triaged in PROGRESS.md).

### T5.8 — Release smoke & handback `[S]` `[G1, G4]`
- **Action:** `.\gradlew.bat assembleRelease` with R8 (keep rules for kotlinx-serialization models if needed); journey tests (read `journeys.md`): cold start offline → seed → Japanese drill → Chinese dialogue → Vault → Feedback; update `README.md` (build, ETL, content update procedure).
- **Verification:** release APK installs and passes the journey on the emulator; final PROGRESS.md summary.
- **Reflexion Guardrails:** *Trap:* R8 strips serializers → add the official kotlinx-serialization keep rules / verify the library's bundled consumer rules.

---

## 10. Worked Example — One Full ReAct + Reflexion Cycle (T3.2)

> **Thought₁:** Implement `Conjugator` per spec §7.1; first the godan TE/TA table, then exceptions. Gate: G2 on `:core:domain`.
> **Action₁:** `write_file core/domain/.../engine/conjugation/Conjugator.kt` · `write_file …/ConjugatorGoldenTest.kt`
> **Action₂:** `run_command powershell -NoProfile -File tools/agent/gate.ps1 -Gate G2 -Ticket T3.2 -Tasks ":core:domain:test"`
> **Observation₂:** `GATE G2 FAIL (T3.2)` · `ConjugatorGoldenTest.godan_te[行く]: expected:<行って> but was:<行いて>`
> **Reflexion (appended as R-0012):** (a) *Root Cause:* the te-form path applies the く→いて rule before consulting exceptions. (b) *Faulty Assumption:* I assumed exceptions only affect MASU/NAI. (c) *Corrective Strategy:* check `Exceptions.lookup(reading, form)` first for every form; add 行く TE/TA entries; add a test proving 書く still yields 書いて.
> **Action₃:** `read_file Conjugator.kt` (lines 30–60) → `edit_file` (exception lookup at the top of `conjugate`) → re-run the same gate.
> **Observation₃:** `GATE G2 PASS (T3.2)`.
> **Verification:** 100 % of golden rows pass; exception count = 6; R-0012 Outcome updated to PASS; PROGRESS.md T3.2 → DONE with log path; commit `feat(T3.2): …`.

---

## 11. Definition of Done (whole project)

- [ ] All tickets DONE in `docs/PROGRESS.md` with gate evidence.
- [ ] `:core:domain` engine coverage ≥ 90 %; all DAO queries tested; errata golden set green.
- [ ] Every screen has light + dark previews rendering via the sidecar.
- [ ] App fully functional in airplane mode (verified on device).
- [ ] Two-device sync convergence test green; rules emulator tests green.
- [ ] No forbidden patterns (`fallbackToDestructiveMigration`, content `REPLACE`, `hiltViewModel()` in previews).
- [ ] Attribution screen complete; repository visibility decision recorded (private recommended).
