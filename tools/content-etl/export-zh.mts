import { pathToFileURL } from 'node:url';
import * as path from 'node:path';
import * as fs from 'node:fs';

const WEB_COMPANION_DIR = process.env.WEB_COMPANION_DIR || 'd:\\.D Dokuments\\Uni Stuff\\Website_project';
const ROOT_DIR = path.resolve(import.meta.dirname, '../..');
const OUTPUT_CONTENT_DIR = path.join(ROOT_DIR, 'core/data/src/main/assets/content/zh');
const OUTPUT_MEDIA_DIR = path.join(ROOT_DIR, 'core/data/src/main/assets/media/zh/dialogues');

fs.mkdirSync(OUTPUT_CONTENT_DIR, { recursive: true });
fs.mkdirSync(OUTPUT_MEDIA_DIR, { recursive: true });

function parseChapter(lessonStr: string | undefined): number | null {
  if (!lessonStr) return null;
  const match = lessonStr.match(/(\d+)/);
  return match ? parseInt(match[1], 10) : null;
}

function classifySyntaxRole(token: string): string {
  const t = token.trim().toLowerCase();
  if (/^(subjekt|subject|s)$/i.test(t)) return 'SUBJECT';
  if (/^(zeit|time|zeitpunkt|zeitdauer|temporal)$/i.test(t)) return 'TIME';
  if (/^(ort|place|location|lokal)$/i.test(t)) return 'PLACE';
  if (/^(modal|modalverb|mv)$/i.test(t)) return 'MODAL';
  if (/^(negation|neg|bù|méi|不|没)$/i.test(t)) return 'NEGATION';
  if (/^(verb|prädikat|predicate|v)$/i.test(t)) return 'VERB';
  if (/^(objekt|object|o)$/i.test(t)) return 'OBJECT';
  if (/^(partikel|particle|le|de|ma|ne|ba|着|了|过)$/i.test(t)) return 'PARTICLE';
  if (/^(aspekt|aspect)$/i.test(t)) return 'ASPECT';
  if (/^(adverb|adv)$/i.test(t)) return 'ADVERB';
  if (/^(attribut|komplement|complement|c)$/i.test(t)) return 'COMPLEMENT';
  return 'LITERAL';
}

function parsePatternTokens(formula: string): Array<{ text: string; role: string }> {
  if (!formula) return [];
  const parts = formula.split(/\s*\+\s*/);
  return parts.map(part => {
    const trimmed = part.trim();
    const role = classifySyntaxRole(trimmed);
    return { text: trimmed, role };
  });
}

async function main() {
  console.log(`Loading Chinese data from ${WEB_COMPANION_DIR}...`);

  const grammarDataPath = path.join(WEB_COMPANION_DIR, 'src/data/chineseGrammarData.ts');
  const dialoguesPath = path.join(WEB_COMPANION_DIR, 'src/data/chineseDialogues.ts');

  const grammarModule = await import(pathToFileURL(grammarDataPath).href);
  const dialoguesModule = await import(pathToFileURL(dialoguesPath).href);

  const rawRules = grammarModule.CHINESE_GRAMMAR_RULES || [];
  const rawConfusables = grammarModule.CONFUSABLE_PAIRS || [];
  const rawDialogues = dialoguesModule.CHINESE_DIALOGUES || [];

  console.log(`Found ${rawRules.length} grammar rules, ${rawConfusables.length} confusable pairs, ${rawDialogues.length} dialogues.`);

  // 1. Grammar Rules & Examples
  const grammarRules: any[] = [];
  const grammarExamples: any[] = [];

  rawRules.forEach((rule: any, ruleIdx: number) => {
    const ruleId = `zh:g:${rule.id}`;
    const chapter = parseChapter(rule.lesson);
    const patternTokens = parsePatternTokens(rule.formula);

    grammarRules.push({
      id: ruleId,
      domain: 'CHINESE',
      chapter,
      sectionLabel: rule.id,
      category: rule.category || null,
      part: rule.part || null,
      titleEn: rule.titleEn || rule.titleDe,
      titleDe: rule.titleDe || null,
      pattern: rule.formula,
      patternTokens,
      summaryEn: rule.summaryEn || rule.summaryDe,
      summaryDe: rule.summaryDe || null,
      nuancesEn: rule.nuancesEn || [],
      nuancesDe: rule.nuancesDe || [],
      bodyMarkdown: null,
      tags: rule.tags || [],
      verified: true,
      sortOrder: ruleIdx + 1
    });

    if (Array.isArray(rule.examples)) {
      rule.examples.forEach((ex: any, exIdx: number) => {
        grammarExamples.push({
          id: `${ruleId}#ex${exIdx + 1}`,
          ruleId,
          text: ex.hanzi,
          rubyMarkup: null,
          romanization: ex.pinyin,
          translationEn: ex.translationEn || ex.translationDe,
          translationDe: ex.translationDe || null,
          breakdown: ex.breakdown || [],
          note: ex.note || null,
          sortOrder: exIdx + 1
        });
      });
    }
  });

  // 2. Confusable Pairs
  function mapConfusableSide(side: any) {
    return {
      term: side.term,
      romanization: side.pinyin || side.romanization,
      conceptEn: side.conceptEn || side.conceptDe,
      conceptDe: side.conceptDe || null,
      formula: side.formula,
      ruleEn: side.ruleEn || side.ruleDe,
      ruleDe: side.ruleDe || null,
      correct: {
        text: side.correct.hanzi || side.correct.text,
        romanization: side.correct.pinyin || side.correct.romanization,
        translationEn: side.correct.translationEn || side.correct.translationDe,
        translationDe: side.correct.translationDe || null
      },
      incorrect: {
        text: side.incorrect.hanzi || side.incorrect.text,
        romanization: side.incorrect.pinyin || side.incorrect.romanization,
        explanationEn: side.incorrect.explanationEn || side.incorrect.explanationDe,
        explanationDe: side.incorrect.explanationDe || null
      }
    };
  }

  const confusables: any[] = [];
  rawConfusables.forEach((cp: any, idx: number) => {
    confusables.push({
      id: `zh:c:${cp.id}`,
      domain: 'CHINESE',
      chapter: parseChapter(cp.lesson),
      pairLabel: cp.pairLabel,
      topicEn: cp.topicEn || cp.topicDe,
      topicDe: cp.topicDe || null,
      left: mapConfusableSide(cp.left),
      right: mapConfusableSide(cp.right),
      mnemonicEn: cp.mnemonicEn || cp.mnemonicDe,
      mnemonicDe: cp.mnemonicDe || null,
      sortOrder: idx + 1
    });
  });

  // 3. Dialogues & Turns
  const dialogues: any[] = [];
  const dialogueTurns: any[] = [];

  rawDialogues.forEach((d: any, dIdx: number) => {
    const dialogueId = `zh:d:${d.id}`;
    const audioFilename = d.audioSrc ? path.basename(d.audioSrc) : null;
    const audioRef = audioFilename ? `media/zh/dialogues/${audioFilename}` : null;
    const durationMs = typeof d.durationS === 'number' ? Math.round(d.durationS * 1000) : null;

    const mappedSpeakers: Record<string, any> = {};
    if (d.speakers) {
      for (const [key, sp] of Object.entries(d.speakers as Record<string, any>)) {
        mappedSpeakers[key] = {
          label: sp.label,
          labelEn: sp.labelEn || null,
          role: sp.role,
          roleEn: sp.roleEn || null,
          voice: sp.voice || null,
          gender: sp.gender || 'female',
          colorHex: sp.color || sp.colorHex || '#4F46E5'
        };
      }
    }

    dialogues.push({
      id: dialogueId,
      domain: 'CHINESE',
      chapter: parseChapter(d.lesson),
      titleEn: d.titleEn || d.title,
      titleDe: d.title || null,
      nativeTitle: d.chineseTitle,
      topicEn: d.topicEn || d.topic,
      topicDe: d.topic || null,
      descriptionEn: d.descriptionEn || d.description,
      descriptionDe: d.description || null,
      audioRef,
      durationMs,
      speakers: mappedSpeakers,
      sortOrder: dIdx + 1
    });

    const tsFilePath = path.join(WEB_COMPANION_DIR, `public/audio/dialogues/${d.id}_timestamps.json`);
    let tsTurns: any[] | null = null;
    if (fs.existsSync(tsFilePath)) {
      try {
        const tsData = JSON.parse(fs.readFileSync(tsFilePath, 'utf-8'));
        if (Array.isArray(tsData.turns)) {
          tsTurns = tsData.turns;
        }
      } catch (e) {
        console.warn(`Could not read timestamps for ${d.id}`, e);
      }
    }

    if (tsTurns) {
      // Use exact synthesized audio turns from timestamps file
      tsTurns.forEach((t: any, idx: number) => {
        const dTurn = (Array.isArray(d.turns) ? d.turns.find((dt: any) => dt.hanzi === t.text) : null)
          || (Array.isArray(d.turns) && idx < d.turns.length ? d.turns[idx] : null);

        dialogueTurns.push({
          dialogueId,
          idx: t.turn_idx || (idx + 1),
          speaker: t.speaker || dTurn?.speaker || 'A',
          text: t.text || dTurn?.hanzi || '',
          rubyMarkup: null,
          romanization: dTurn?.pinyin || null,
          translationEn: dTurn?.translationEn || dTurn?.translation || null,
          translationDe: dTurn?.translation || null,
          startMs: Math.round(t.start_ms),
          endMs: Math.round(t.end_ms)
        });
      });
    } else if (Array.isArray(d.turns)) {
      d.turns.forEach((turn: any) => {
        const startMs = typeof turn.startS === 'number' ? Math.round(turn.startS * 1000) : null;
        const endMs = typeof turn.endS === 'number' ? Math.round(turn.endS * 1000) : null;
        dialogueTurns.push({
          dialogueId,
          idx: turn.idx,
          speaker: turn.speaker,
          text: turn.hanzi,
          rubyMarkup: null,
          romanization: turn.pinyin,
          translationEn: turn.translationEn || turn.translation || null,
          translationDe: turn.translation || null,
          startMs,
          endMs
        });
      });
    }
  });

  // Write seed JSON files
  fs.writeFileSync(path.join(OUTPUT_CONTENT_DIR, 'grammar_rules.json'), JSON.stringify(grammarRules, null, 2), 'utf-8');
  fs.writeFileSync(path.join(OUTPUT_CONTENT_DIR, 'grammar_examples.json'), JSON.stringify(grammarExamples, null, 2), 'utf-8');
  fs.writeFileSync(path.join(OUTPUT_CONTENT_DIR, 'confusables.json'), JSON.stringify(confusables, null, 2), 'utf-8');
  fs.writeFileSync(path.join(OUTPUT_CONTENT_DIR, 'dialogues.json'), JSON.stringify(dialogues, null, 2), 'utf-8');
  fs.writeFileSync(path.join(OUTPUT_CONTENT_DIR, 'dialogue_turns.json'), JSON.stringify(dialogueTurns, null, 2), 'utf-8');

  console.log(`Saved:
  - ${grammarRules.length} grammar rules -> grammar_rules.json
  - ${grammarExamples.length} grammar examples -> grammar_examples.json
  - ${confusables.length} confusable pairs -> confusables.json
  - ${dialogues.length} dialogues -> dialogues.json
  - ${dialogueTurns.length} dialogue turns -> dialogue_turns.json
`);

  // Copy audio files
  const audioSrcDir = path.join(WEB_COMPANION_DIR, 'public/audio/dialogues');
  if (fs.existsSync(audioSrcDir)) {
    const files = fs.readdirSync(audioSrcDir).filter(f => f.endsWith('.mp3'));
    console.log(`Copying ${files.length} MP3 files from ${audioSrcDir} to ${OUTPUT_MEDIA_DIR}...`);
    let copied = 0;
    for (const f of files) {
      fs.copyFileSync(path.join(audioSrcDir, f), path.join(OUTPUT_MEDIA_DIR, f));
      copied++;
    }
    console.log(`Successfully copied ${copied} MP3 files.`);
  } else {
    console.warn(`Audio source dir not found at ${audioSrcDir}`);
  }
}

main().catch(err => {
  console.error('Error during Chinese ETL export:', err);
  process.exit(1);
});
