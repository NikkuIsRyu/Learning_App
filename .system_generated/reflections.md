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
