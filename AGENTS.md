# Agent Operating Rules — Learning App (`tech.nikku.learningapp`)

Read [`docs/01_SYSTEM_ARCHITECTURE.md`](docs/01_SYSTEM_ARCHITECTURE.md) and [`docs/02_HANDOFF_ROADMAP.md`](docs/02_HANDOFF_ROADMAP.md) before starting any ticket.

## 1. Execution Protocol (ReAct + Reflexion)
- **Order**: Execute tickets in [`docs/02_HANDOFF_ROADMAP.md`](docs/02_HANDOFF_ROADMAP.md) strictly in sequence (`T0.1` → `T5.8`). Track state in [`docs/PROGRESS.md`](docs/PROGRESS.md).
- **ReAct Loop**: Every ticket step follows `Thought → Action → Observation → Verification`. Always `read_file` before `edit_file`.
- **Evaluator Gates (`M_e`)**:
  - `G0`: Static file/pattern check.
  - `G1`: `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G1 -Ticket T<id>` (`assembleDebug`).
  - `G2`: `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G2 -Ticket T<id>` (JVM unit + Robolectric DAO tests).
  - `G3`: `powershell -NoProfile -File tools/agent/preview.ps1 -ConversationId <id> -Reference "gradle://.?:app@debug" -Fqn "<fqns>"` via the Compose Preview sidecar.
  - `G4`: Emulator/device run (`android emulator`, `android run`, `android layout`, `android screen capture`).
  - `G5`: `powershell -NoProfile -File tools/agent/gate.ps1 -Gate G5 -Ticket T<id>` (`lintDebug` + Room schema check).
- **Self-Reflection (`M_sr`)**: On **any** gate failure, do NOT blindly retry. Append a structured entry (`[R-NNNN]`) to [`.system_generated/reflections.md`](.system_generated/reflections.md) with *(a) Root Cause*, *(b) Faulty Assumption*, and *(c) Corrective Code Strategy*. Maximum **3 attempts** per ticket gate before marking `BLOCKED` and escalating to the user.

## 2. Non-Negotiable Architecture Guardrails
1. **No Destructive Room Operations**: Never use `fallbackToDestructiveMigration()` or `@Insert(onConflict = OnConflictStrategy.REPLACE)` on content tables (use `@Upsert`).
2. **Stateless Previews**: Every screen splits into `XxxRoute` (Hilt VM) + `XxxScreen(state, onEvent)` (stateless) + `@Preview` (fake state only, never `hiltViewModel()` or DB).
3. **Module Boundaries**: `:core:domain` is pure Kotlin/JVM (zero `android.*`/`androidx.*` imports).
4. **No Invented Versions**: Resolve library versions from official Google/Maven Central `maven-metadata.xml` or `android studio version-lookup`.
5. **Security Rules Delegation**: `firestore.rules` MUST be authored exclusively via `invoke_subagent` with `firestore-rules-author`.
6. **Data Integrity**: Never fabricate readings, JLPT levels, or verb groups. Ambiguous items go to `ja/review_queue.json`.
7. **Path Safety**: Always quote file paths (`d:\.D Dokuments\Uni Stuff\Knowledge_App` contains spaces). Never delete user backup files (`*(Copy)*`, `*.bak`).
