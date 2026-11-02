#!/usr/bin/env python3
"""
tools/content-etl/gen_scramble.py

Generates Sentence Builder / Scramble exercises (ExercisePayload.Scramble)
for Minna no Nihongo Chapters 26–32 from verified example sentences.
- Produces >= 10 scramble exercises per chapter (>= 70 total, exactly 84 generated)
- Segments sentences into 3 to 6 bunsetsu chunks
- Computes canonical acceptedOrders and valid alternative word orders (swapping adverbials / topics)
- Validates that no errata strings exist
- Outputs to core/data/src/main/assets/content/ja/exercises_scramble_26_32.json
"""

import json
import os
import re
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent.parent.parent
EXAMPLES_JSON_PATH = Path("D:/.D Dokuments/Uni Stuff/Meine_Bibliothek/Code_Projects/Voice_ML/Voice_ML_Project/data/ja/vocab/b1_1_examples_generated.json")
OUTPUT_FILE = ROOT_DIR / "core/data/src/main/assets/content/ja/exercises_scramble_26_32.json"

FORBIDDEN_ERRATA = ["しめって あります", "つかたら", "とかいの しゅうり", "あたらしー"]

AUX_PHRASES = [
    'てもらいます', 'てくれます', 'てあげます', 'ています', 'てあります', 'ておきます', 'てしまいます',
    'てはいけません', 'てもいいです', 'なくてもいいです', 'なければなりません',
    'たほうがいいです', 'ないほうがいいです', 'かもしれません', 'でしょう', 'んです', 'のです'
]

PARTICLES = [
    'について', 'にとって', 'からは', 'までは', 'からも', 'までも',
    'には', 'では', 'へは', 'とは', 'にも', 'でも', 'へも', 'とも',
    'から、', 'ので、', 'のに、', 'たら、', 'ながら、', 'てから、', 'けど、', 'が、',
    'から', 'ので', 'のに', 'たら', 'ながら', 'てから', 'けど',
    'は、', 'が、', 'を、', 'に、', 'で、', 'へ、', 'と、', 'も、',
    'まで', 'より', 'しか', 'だけ',
    'は', 'が', 'を', 'に', 'で', 'へ', 'と', 'も'
]

def chunk_sentence(s: str) -> list[str]:
    s = s.strip().replace('　', ' ')
    protected = s
    for i, p in enumerate(AUX_PHRASES):
        protected = protected.replace(p, f'__AUX{i}__')
    
    for tw in ['明日', '昨日', '今日', '今朝', '今晩', '毎朝', '毎晩', '毎日', '週末']:
        if protected.startswith(tw) and len(protected) > len(tw) and not protected[len(tw):].startswith(('は', 'に', 'の', 'も')):
            protected = tw + ' ' + protected[len(tw):]

    tokens = []
    curr = ''
    idx = 0
    while idx < len(protected):
        curr += protected[idx]
        idx += 1
        if curr.endswith(' '):
            tokens.append(curr.strip())
            curr = ''
            continue
        matched_p = None
        for p in PARTICLES:
            if curr.endswith(p) and len(curr) > len(p):
                matched_p = p
                break
        if matched_p and idx < len(protected):
            next_char = protected[idx]
            if next_char in '、。！？':
                curr += next_char
                idx += 1
            tokens.append(curr.strip())
            curr = ''
    if curr.strip():
        tokens.append(curr.strip())

    restored = []
    for t in tokens:
        for i, p in enumerate(AUX_PHRASES):
            t = t.replace(f'__AUX{i}__', p)
        if t:
            restored.append(t)
    return restored

def compute_accepted_orders(chunks: list[str]) -> list[list[int]]:
    canonical = list(range(len(chunks)))
    orders = [canonical]

    # Rule 1: Time expression or Topic swap between chunk 0 and chunk 1
    # If chunk 0 is a time expression or topic, and chunk 1 is an adverbial/subject phrase
    c0 = chunks[0]
    c1 = chunks[1] if len(chunks) > 1 else ""

    time_starters = ('明日', '昨日', '今日', '今朝', '今晩', '毎朝', '毎晩', '毎日', '週末', '今週', '来週', '先週', '休みの日は')
    is_c0_time = any(c0.startswith(t) for t in time_starters)
    is_c1_time = any(c1.startswith(t) for t in time_starters)

    if (is_c0_time or c0.endswith('は') or c0.endswith('は、')) and (c1.endswith(('で', 'に', 'を', 'が', 'へ', 'と', 'は')) or is_c1_time):
        alt = [1, 0] + list(range(2, len(chunks)))
        if alt not in orders:
            orders.append(alt)

    # Rule 2: Case phrase swap between chunk 1 and chunk 2 (e.g. 先生に + 喉を <-> 喉を + 先生に)
    if len(chunks) >= 4:
        c1 = chunks[1]
        c2 = chunks[2]
        if (c1.endswith(('に', 'で', 'へ')) and c2.endswith('を')) or (c1.endswith('を') and c2.endswith(('に', 'で', 'へ'))):
            alt2 = [0, 2, 1] + list(range(3, len(chunks)))
            if alt2 not in orders:
                orders.append(alt2)

    return orders

def main():
    if not EXAMPLES_JSON_PATH.exists():
        print(f"Error: {EXAMPLES_JSON_PATH} does not exist.")
        sys.exit(1)

    raw_data = json.loads(EXAMPLES_JSON_PATH.read_text(encoding="utf-8"))
    exercises_by_chapter = {ch: [] for ch in range(26, 33)}

    for item in raw_data:
        lesson_str = item.get("lesson", "")
        m = re.match(r'L?(\d+)', lesson_str)
        if not m:
            continue
        ch = int(m.group(1))
        if ch not in exercises_by_chapter:
            continue

        jp = item.get("example_ja", "").strip()
        meaning = item.get("example_meaning", "")
        parts = [p.strip() for p in meaning.split("|")]
        en_trans = parts[0] if parts else ""
        de_trans = parts[1] if len(parts) > 1 else None

        # Check for errata
        full_text = f"{jp} {en_trans} {de_trans or ''}"
        if any(err in full_text for err in FORBIDDEN_ERRATA):
            continue

        chunks = chunk_sentence(jp)
        # We want between 3 and 6 chunks for good UX
        if not (3 <= len(chunks) <= 6):
            continue

        # Verify lossless reconstruction
        if "".join(chunks) != jp:
            continue

        # Skip if chapter already has 12 exercises
        if len(exercises_by_chapter[ch]) >= 12:
            continue

        accepted_orders = compute_accepted_orders(chunks)
        rule_id = f"ja:mnn:{ch}.1"
        ex_num = len(exercises_by_chapter[ch]) + 1
        ex_id = f"ja:ex:scramble:{ch}:{ex_num:02d}"

        exercises_by_chapter[ch].append({
            "id": ex_id,
            "domain": "JAPANESE",
            "chapter": ch,
            "type": "SCRAMBLE",
            "grammarRuleId": rule_id,
            "difficulty": 2,
            "payload": {
                "kind": "scramble",
                "chunks": chunks,
                "acceptedOrders": accepted_orders,
                "distractors": [],
                "translationEn": en_trans,
                "translationDe": de_trans
            },
            "verified": False,
            "sortOrder": ex_num
        })

    # Flatten
    all_exercises = []
    for ch in sorted(exercises_by_chapter.keys()):
        ch_list = exercises_by_chapter[ch]
        print(f"Chapter {ch}: generated {len(ch_list)} scramble exercises")
        if len(ch_list) < 10:
            print(f"Error: Chapter {ch} generated only {len(ch_list)} exercises (need >= 10)")
            sys.exit(1)
        all_exercises.extend(ch_list)

    print(f"Total exercises generated: {len(all_exercises)}")
    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(all_exercises, f, ensure_ascii=False, indent=2)

    print(f"Wrote scramble exercises to {OUTPUT_FILE}")

if __name__ == "__main__":
    main()
