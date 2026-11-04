# Phase 4 — Chinese Module Specification & Data Guide

## 1. Overview & Scope

Phase 4 implements the Chinese learning module, porting and enhancing the components from the companion Astro web project (`Website_project/src/components/chinese/*.astro`) to native Android Jetpack Compose, backed by Room database entities and the offline SRS engine.

The module consists of 5 tickets (`T4.1` – `T4.5`):
- **`T4.1`**: Chinese content verification & Pinyin utilities (`ContentValidationTest`, `Pinyin.parse`)
- **`T4.2`**: Chinese Grammar Explorer (`feature/chinese/grammar/`: categories, search, formula pills, 3-layer aligned text, tone colors)
- **`T4.3`**: Chinese Grammar Cards (`feature/chinese/cards/`: flip animation, SRS review `ItemType.GRAMMAR`)
- **`T4.4`**: Confusable Pairs (`feature/chinese/confusables/`: side-by-side / stacked, high-contrast, quiz mode)
- **`T4.5`**: Chinese Dialogue Player (`feature/chinese/dialogues/`: Media3 player, speaker color chips, per-turn replay)

---

## 2. Where the Data Comes From

All required seed content and audio assets for Phase 4 are **already bundled inside this repository** under `core/data/src/main/assets/`. No network downloads or external scraping are needed.

### 2.1 Bundled JSON Datasets (`core/data/src/main/assets/content/zh/`)

| File | Entity / Schema | Record Count | Description |
| :--- | :--- | :--- | :--- |
| [`grammar_rules.json`](file:///d:/.D%20Dokuments/Uni%20Stuff/Knowledge_App/core/data/src/main/assets/content/zh/grammar_rules.json) | `ChineseGrammarRuleEntity` | 26 rules | Grammar rules across 7 categories (Aspect, Comparison, Complements, Questions, Sentence Structure, Special Structures, Time/Duration) covering Lessons 2–10. Contains formula tokens, rule explanations, and common pitfalls. |
| [`grammar_examples.json`](file:///d:/.D%20Dokuments/Uni%20Stuff/Knowledge_App/core/data/src/main/assets/content/zh/grammar_examples.json) | `ChineseGrammarExampleEntity` | Multi-record | 3-layer aligned examples: Hanzi text, Pinyin with tone diacritics, and translations (DE / EN). Foreign keyed to `ruleId`. |
| [`confusables.json`](file:///d:/.D%20Dokuments/Uni%20Stuff/Knowledge_App/core/data/src/main/assets/content/zh/confusables.json) | `ConfusablePairEntity` | 6 pairs | Pairs of easily confused words/patterns (e.g., 二 vs 两, 不 vs 没, 的 vs 得 vs 地). Includes correct/incorrect example sentences, distinction rule, and mnemonics. |
| [`dialogues.json`](file:///d:/.D%20Dokuments/Uni%20Stuff/Knowledge_App/core/data/src/main/assets/content/zh/dialogues.json) | `DialogueEntity` | 18 dialogues | Dialogues `Dialogue2_1` through `Dialogue10_2`. Contains metadata, title, audio asset reference, and speaker mappings. |
| [`dialogue_turns.json`](file:///d:/.D%20Dokuments/Uni%20Stuff/Knowledge_App/core/data/src/main/assets/content/zh/dialogue_turns.json) | `DialogueTurnEntity` | Multi-record | Exact line-by-line turns with millisecond timestamps (`startMs`, `endMs`), speaker IDs, Hanzi lines, Pinyin, and translations. |

### 2.2 Bundled Audio Files (`core/data/src/main/assets/media/zh/dialogues/`)

All 18 dialogue audio recordings (Tier A bundled media) are stored as MP3 files:
- `Dialogue2_1.mp3`, `Dialogue2_2.mp3`
- `Dialogue3_1.mp3`, `Dialogue3_2.mp3`
- `Dialogue4_1.mp3`, `Dialogue4_2.mp3`
- `Dialogue5_1.mp3`, `Dialogue5_2.mp3`
- `Dialogue6_1.mp3`, `Dialogue6_2.mp3`
- `Dialogue7_1.mp3`, `Dialogue7_2.mp3`
- `Dialogue8_1.mp3`, `Dialogue8_2.mp3`
- `Dialogue9_1.mp3`, `Dialogue9_2.mp3`
- `Dialogue10_1.mp3`, `Dialogue10_2.mp3`

### 2.3 Original Upstream Sources (Reference Only)

If you need to cross-check or update data in the future:
1. **Web Project Source**:
   - `Website_project/src/data/chineseGrammarData.ts` (Grammar rules and confusables source)
   - `Website_project/src/data/chineseDialogues.ts` (Dialogue scripts and speaker definitions)
   - `Website_project/public/audio/dialogues/*.mp3` and `*_timestamps.json` (GPT-SoVITS voice synthesis recordings)
2. **Database Ingestion**:
   - `ContentSeeder.kt` (in `:core:data`) already handles parsing and seeding `manifest.json` and all `content/zh/*.json` files on first app launch or schema update.

---

## 3. Step-by-Step Ticket Requirements

### T4.1 — Chinese Content Verification & Utilities
- **Goal**: Verify database integrity and test Pinyin diacritic parsing before writing UI.
- **Tasks**:
  1. Extend `ContentValidationTest` to assert:
     - Total Chinese grammar rules = 26
     - Rule categories = 7 (`ASPECT`, `COMPARISON`, `COMPLEMENT`, `QUESTION`, `STRUCTURE`, `SPECIAL`, `TIME`)
     - Confusable pairs = 6
     - Dialogues = 18; verify all corresponding MP3 files exist in `media/zh/dialogues/`
  2. Verify `Pinyin.parse()` handles all standard syllables, tone marks (`ā á ǎ à a`), `ü` / `u:` notations, and neutral tones.
- **Gates**: `G2` (Robolectric JVM test suite).

### T4.2 — Chinese Grammar Explorer
- **Goal**: Full grammar browser with category filtering, search, and rich linguistic typography.
- **Location**: `app/src/main/kotlin/tech/nikku/learningapp/feature/chinese/grammar/`
- **Features**:
  - Filter chips: 7 categories + "All".
  - Search query bar matching title, keywords, and formula.
  - Formula display using `FormulaPills` (`:core:designsystem`) with syntax color coding.
  - 3-layer aligned text: Hanzi (`LocaleList("zh-CN")`), Pinyin with tone colors, and translated meaning.
  - Tone color toggle: Pleco palette (T1 Red, T2 Green, T3 Blue, T4 Purple, Neutral Gray).
  - Pinyin active recall: Tap to reveal / hide Pinyin.
- **Gates**: `G2` (ViewModel unit tests), `G3` (Compose Preview rendering).

### T4.3 — Chinese Grammar Cards
- **Goal**: Interactive SRS flip-cards for mastering grammar rules.
- **Location**: `app/src/main/kotlin/tech/nikku/learningapp/feature/chinese/cards/`
- **Features**:
  - Front: Rule title, formula tokens, category chip.
  - 3D Flip animation (`graphicsLayer { rotationY }` with non-mirrored backface swap at 90°).
  - Back: Explanation, key example with 3-layer aligned text, SRS rating buttons (Again, Hard, Good, Easy) predicting next interval.
  - Integrated with `ItemType.GRAMMAR` in the SRS scheduler.
- **Gates**: `G2`, `G3` (Front and Back card previews).

### T4.4 — Confusable Pairs
- **Goal**: Side-by-side comparison screen and quiz for easily confused structures.
- **Location**: `app/src/main/kotlin/tech/nikku/learningapp/feature/chinese/confusables/`
- **Features**:
  - Responsive layout: Side-by-side columns on wide screens (>= 600 dp), vertically stacked on phones (< 600 dp).
  - Visual distinction: Correct (✓) and incorrect (✕) examples with semantic accessibility descriptions.
  - Rule explanation & mnemonic cards.
  - High-contrast toggle.
  - Quiz mode (`ConfusableQuiz`): Multiple choice prompt ("Which word correctly completes this sentence?").
- **Gates**: `G2`, `G3` (Phone & Tablet previews).

### T4.5 — Chinese Dialogue Player
- **Goal**: Audio playback of dialogues with real-time turn highlighting and sentence replay.
- **Location**: `app/src/main/kotlin/tech/nikku/learningapp/feature/chinese/dialogues/`
- **Features**:
  - Reuses the shared Media3 player controller (`:app/feature/shared/player/`).
  - Speaker identification with color chips from `SpeakerInfo.colorHex`.
  - Line-by-line sync: Highlights active turn based on `startMs` / `endMs`.
  - Tap any sentence to seek and replay that exact turn.
  - Speed control chips (0.75×, 1.0×, 1.25×).
- **Gates**: `G2`, `G3` (Previews), `G4` (Device/Emulator playback verification).

---

## 4. Key Architectural Guardrails for Phase 4

1. **Han-Unification Glyph Safety**:
   Always use `LearningTypography.chinese()` or `TextStyle(localeList = LocaleList("zh-CN"))` on all Chinese Hanzi text to avoid Japanese font fallback glyphs.
2. **Accessible Tone Colors**:
   Tone colors must never be the sole carrier of meaning; always keep tone diacritics in the Pinyin string, and ensure contrast >= 4.5:1.
3. **Card Flip Transform**:
   When implementing 3D card flip in Jetpack Compose using `graphicsLayer { rotationY }`, swap between front and back composables at 90° and apply a counter-rotation so text on the back face is never mirrored.
4. **Media Timestamp Safety**:
   Clamp end clipping timestamps to `mediaDurationMs` to prevent Media3 `ClippingConfiguration` exceptions on truncated audio files.
