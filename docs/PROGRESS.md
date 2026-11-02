# Implementation Progress Ledger

Track every ticket from [`02_HANDOFF_ROADMAP.md`](02_HANDOFF_ROADMAP.md). Allowed statuses: `TODO`, `IN_PROGRESS`, `VERIFYING`, `BLOCKED`, `DONE`.

| Ticket | Phase | Title | Gates | Status | Gate Evidence / Log Path | Commit |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `T0.1` | 0 | Toolchain probe | G0 | `DONE` | `docs/ENVIRONMENT.md` generated & verified (G0 PASS) | — |
| `T0.2` | 0 | Git wiring | G0 | `BLOCKED` | Local repo & initial commit `d9f8fe9` ready; remote `ls-remote` returned `Permission denied (publickey)` | `d9f8fe9` |
| `T0.3` | 0 | Evaluator harness (`gate.ps1`, `preview.ps1`) | G0 | `DONE` | `gate.ps1 errors=0`, `preview.ps1 errors=0`, negative test verified | `86ffc32` |
| `T1.1` | 1 | Generate Android project (`android create`) | G1 | `DONE` | GATE G1 PASS (`build/agent-logs/T1.1-G1-20261102-145205.log`) | `266401c` |
| `T1.2` | 1 | 4-module split & dependency stack | G1, G2 | `DONE` | GATE G1 PASS (`build/agent-logs/T1.2-G1-20261102-150708.log`), GATE G2 PASS (`build/agent-logs/T1.2-G2-20261102-150945.log`) | `8da5c6d` |
| `T1.3` | 1 | Design system & CJK primitives (`FuriganaText`, `PinyinText`) | G2, G3 | `DONE` | GATE G2 PASS (`build/agent-logs/T1.3-G2-20261102-151519.log`), GATE G3 PASS (FuriganaText, PinyinText, FormulaPills previews rendered & visually verified) | `d352c35` |
| `T1.4` | 1 | App shell & adaptive top-level navigation | G1, G3, G4 | `DONE` | GATE G1 PASS (`build/agent-logs/T1.4-G1-20261102-152659.log`), GATE G3 PASS (Phone & Tablet previews rendered & verified) | `d7d7d35` |
| `T1.5` | 1 | Vault "Work in Progress" screen | G2, G3 | `DONE` | GATE G2 PASS (`build/agent-logs/T1.5-G2-20261102-153424.log`), GATE G3 PASS (VaultScreen light & dark previews rendered & verified) | `ca41612` |
| `T2.1` | 2 | Domain models & repository interfaces | G1, G2 | `DONE` | GATE G1 PASS (`build/agent-logs/T2.1-G1-20261102-153948.log`), GATE G2 PASS (`build/agent-logs/T2.1-G2-20261102-154010.log`) | `30e3323` |
| `T2.2` | 2 | Room schema (entities, relations, converters, DAOs) | G0, G1 | `DONE` | GATE G1 PASS (`build/agent-logs/T2.2-G1-20261102-154802.log`), GATE G0 PASS (1.json 13 entities, 0 forbidden patterns) | `21537d7` |
| `T2.3` | 2 | DAO tests (Robolectric JVM) | G2 | `DONE` | GATE G2 PASS (`build/agent-logs/T2.3-G2-20261102-155349.log`, 7 test cases verified) | `7adde3d` |
| `T2.4` | 2 | Content ETL & seed JSON + errata validation | G0, G2 | `DONE` | GATE G0 PASS (`review_queue.json` count=0); GATE G2 PASS (`build/agent-logs/T2.4-G2-20261102-162222.log`, 9 test cases, counts 45/41/54/51/48/39/57, errata golden set) | `7c4c0a1` |
| `T2.5` | 2 | `ContentSeeder` & DataStore settings | G2, G4 | `DONE` | GATE G2 PASS (`build/agent-logs/T2.5-G2-20261102-163430.log`, 5 test cases, fixture seeding, no-op check, version 2 stale pruning + user progress preservation, real assets counts verified), GATE G1 PASS (`build/agent-logs/T2.5-G1-20261102-163455.log`) | `6359ada` |
| `T2.6` | 2 | Deterministic SRS scheduler & review use cases | G2 | `DONE` | GATE G2 PASS (`build/agent-logs/T2.6-G2-20261102-164030.log`, 10 test cases, 1000× determinism, log replay equivalence, learning-day 04:00 cutoff, interleaving), GATE G1 PASS (`build/agent-logs/T2.6-G1-20261102-164041.log`) | `1fb4cef` |
| `T3.1` | 3 | Japanese Chapter Hub (1–50, 26–32 verified) & detail | G2, G3 | `DONE` | GATE G2 PASS (`build/agent-logs/T3.1-G2-20261102-165320.log`, JapaneseHubViewModelTest), GATE G1 PASS (`build/agent-logs/T3.1-G1-20261102-165359.log`), GATE G3 PASS (JapaneseHubScreenPreview & ChapterScreenPreview rendered & verified) | `cd58d35` |
| `T3.2` | 3 | Rule-based Conjugation Engine & golden tests | G2 | `DONE` | GATE G2 PASS (`build/agent-logs/T3.2-G2-20261102-170300.log`, ConjugatorGoldenTest for Godan/Ichidan/Suru/Kuru/Exceptions across all 10 forms, RomajiKanaTest bidirectional Hepburn/Kunrei, AnswerNormalizerTest katakana/spaces/punctuation/ra-nuki/ending-diff) | `dcda063` |
| `T3.3` | 3 | Conjugation Matrix & Drill UI | G2, G3 | `DONE` | GATE G1 PASS (`build/agent-logs/T3.3-G1-20261102-171709.log`), GATE G2 PASS (`build/agent-logs/T3.3-G2-20261102-171853.log`, ConjugationDrillViewModelTest correct/wrong/hint), GATE G3 PASS (ConjugationMatrixScreenPreview, ConjugationDrillCorrectPreview, ConjugationDrillWrongPreview rendered & visually verified with ending diff) | [pending] |
| `T3.4` | 3 | Particle Cloze engine & UI | G0, G2, G3 | `TODO` | — | — |
| `T3.5` | 3 | Sentence Builder (Scramble) engine & UI | G2, G3 | `TODO` | — | — |
| `T3.6` | 3 | Shared Media3 Audio Player & Japanese Listening | G2, G3, G4 | `TODO` | — | — |
| `T3.7` | 3 | Master Lexicon (hybrid FTS4/LIKE + filter chips) | G2, G3 | `TODO` | — | — |
| `T3.8` | 3 | Chapter Kanji & animated KanjiVG stroke order | G2, G3 | `TODO` | — | — |
| `T3.9` | 3 | Review Session host & Today dashboard | G2, G3, G4 | `TODO` | — | — |
| `T4.1` | 4 | Chinese content verification & Pinyin utilities | G2 | `TODO` | — | — |
| `T4.2` | 4 | Chinese Grammar Explorer (formula pills + tone colors) | G2, G3 | `TODO` | — | — |
| `T4.3` | 4 | Chinese Grammar Cards (flip + SRS) | G2, G3 | `TODO` | — | — |
| `T4.4` | 4 | Confusable Pairs cards & quiz | G2, G3 | `TODO` | — | — |
| `T4.5` | 4 | Chinese Dialogue Player (timestamp sentence replay) | G2, G3, G4 | `TODO` | — | — |
| `T5.1` | 5 | Firebase project wiring *(User Gate)* | G1 | `TODO` | — | — |
| `T5.2` | 5 | Firebase Authentication (Credential Manager) | G2, G3 | `TODO` | — | — |
| `T5.3` | 5 | Firestore security rules *(Delegated: `firestore-rules-author`)* | Emulator | `TODO` | — | — |
| `T5.4` | 5 | Event-sourced `SyncWorker` & convergence tests | G2, G4 | `TODO` | — | — |
| `T5.5` | 5 | On-demand Media Packs (Tier B) | G2 | `TODO` | — | — |
| `T5.6` | 5 | User Feedback module (offline queue + Firestore sync) | G2, G3 | `TODO` | — | — |
| `T5.7` | 5 | Accessibility, font-scale & attribution pass | G3, G5 | `TODO` | — | — |
| `T5.8` | 5 | Release R8 build & end-to-end smoke journey | G1, G4 | `TODO` | — | — |
