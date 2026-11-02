#!/usr/bin/env python3
"""
tools/content-etl/kanji_build.py

Downloads and caches KANJIDIC2, KRADFILE, and KanjiVG.
Extracts kanji details for all kanji appearing in Minna no Nihongo Chapters 26–32:
- On/Kun readings, English meanings, stroke count, grade, freq, jlpt, radical
- Radical component decomposition from KRADFILE
- Ordered stroke path SVG 'd' strings from KanjiVG
- introducedChapter (first MNN chapter introducing this kanji in vocabulary)
- KanjiCompoundCrossRef links to vocabulary words
- Emits ja/kanji.json, ja/kanji_compounds.json, ja/review_queue.json
"""

import gzip
import json
import os
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

ROOT_DIR = Path(__file__).resolve().parent.parent.parent
CACHE_DIR = ROOT_DIR / "tools/content-etl/.cache"
OUTPUT_CONTENT_JA = ROOT_DIR / "core/data/src/main/assets/content/ja"

CACHE_DIR.mkdir(parents=True, exist_ok=True)
OUTPUT_CONTENT_JA.mkdir(parents=True, exist_ok=True)

KANJIDIC2_URL = "http://ftp.edrdg.org/pub/Nihongo/kanjidic2.xml.gz"
KRADFILE_URL = "http://ftp.edrdg.org/pub/Nihongo/kradfile.gz"
KANJIVG_URL = "https://github.com/KanjiVG/kanjivg/releases/download/r20260714/kanjivg-20260714.xml.gz"

def download_cached(url: str, filename: str) -> Path:
    dest = CACHE_DIR / filename
    if dest.exists() and dest.stat().st_size > 0:
        print(f"Using cached {filename} ({dest.stat().st_size} bytes)")
        return dest
    print(f"Downloading {url} to {dest}...")
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        with open(dest, "wb") as f:
            while chunk := resp.read(65536):
                f.write(chunk)
    print(f"Downloaded {filename} ({dest.stat().st_size} bytes).")
    return dest

def is_kanji(ch: str) -> bool:
    return '\u4e00' <= ch <= '\u9fff' or '\u3400' <= ch <= '\u4dbf'

def parse_kradfile(gz_path: Path, target_kanji: set) -> dict:
    print("Parsing KRADFILE for target kanji components...")
    components_map = {}
    with gzip.open(gz_path, "rb") as gz:
        for raw_line in gz:
            try:
                line = raw_line.decode("euc-jp", errors="ignore").strip()
            except Exception:
                continue
            if not line or line.startswith("#"):
                continue
            # Format: 探 : 扌 冖 木
            parts = line.split(":", 1)
            if len(parts) == 2:
                k = parts[0].strip()
                if k in target_kanji:
                    comps = [c.strip() for c in parts[1].split() if c.strip()]
                    components_map[k] = comps
    print(f"Resolved components for {len(components_map)} / {len(target_kanji)} kanji.")
    return components_map

def parse_kanjivg(gz_path: Path, target_kanji: set) -> dict:
    print("Parsing KanjiVG for stroke paths...")
    stroke_paths_map = {}
    target_hex_map = {f"{ord(k):05x}": k for k in target_kanji}

    with gzip.open(gz_path, "rb") as gz:
        for event, elem in ET.iterparse(gz, events=("end",)):
            if elem.tag == "kanji":
                elem_id = elem.attrib.get("id", "")
                # Format: kvg:kanji_065e5
                hex_code = elem_id.replace("kvg:kanji_", "")
                if hex_code in target_hex_map:
                    k = target_hex_map[hex_code]
                    paths = [p.attrib.get("d") for p in elem.iter("path") if "d" in p.attrib]
                    stroke_paths_map[k] = paths
                elem.clear()
    print(f"Resolved stroke paths for {len(stroke_paths_map)} / {len(target_kanji)} kanji.")
    return stroke_paths_map

def parse_kanjidic2(gz_path: Path, target_kanji: set) -> dict:
    print("Parsing KANJIDIC2 for kanji metadata...")
    kanji_meta = {}
    with gzip.open(gz_path, "rb") as gz:
        for event, elem in ET.iterparse(gz, events=("end",)):
            if elem.tag == "character":
                lit_elem = elem.find("literal")
                if lit_elem is not None and lit_elem.text in target_kanji:
                    k = lit_elem.text
                    # Stroke count
                    sc_elem = elem.find(".//stroke_count")
                    stroke_count = int(sc_elem.text) if sc_elem is not None else 0

                    # Grade & freq & jlpt
                    misc = elem.find("misc")
                    grade = None
                    freq = None
                    jlpt = None
                    if misc is not None:
                        g_elem = misc.find("grade")
                        if g_elem is not None:
                            try:
                                grade = int(g_elem.text)
                            except ValueError:
                                pass
                        f_elem = misc.find("freq")
                        if f_elem is not None:
                            try:
                                freq = int(f_elem.text)
                            except ValueError:
                                pass
                        j_elem = misc.find("jlpt")
                        if j_elem is not None:
                            try:
                                j_val = int(j_elem.text)
                                # Map 1..5 to N5..N1 (old JLPT 1=N1, 2=N2, 3=N3/N4, 4=N5)
                                jlpt_map = {1: "N1", 2: "N2", 3: "N3", 4: "N5", 5: "N5"}
                                jlpt = jlpt_map.get(j_val)
                            except ValueError:
                                pass

                    # Radical
                    radical = ""
                    radical_num = None
                    rad_elem = elem.find(".//radical/rad_value[@rad_type='classical']")
                    if rad_elem is not None:
                        try:
                            radical_num = int(rad_elem.text)
                        except ValueError:
                            pass

                    # On/Kun readings
                    on_readings = []
                    kun_readings = []
                    for rm in elem.findall(".//reading_meaning/rmgroup/reading"):
                        r_type = rm.attrib.get("r_type")
                        if r_type == "ja_on":
                            on_readings.append(rm.text)
                        elif r_type == "ja_kun":
                            kun_readings.append(rm.text)

                    # English meanings
                    meanings_en = []
                    for m in elem.findall(".//reading_meaning/rmgroup/meaning"):
                        if "m_lang" not in m.attrib:
                            meanings_en.append(m.text)

                    kanji_meta[k] = {
                        "strokeCount": stroke_count,
                        "grade": grade,
                        "frequencyRank": freq,
                        "jlpt": jlpt,
                        "radical": radical,
                        "radicalNumber": radical_num,
                        "onReadings": on_readings,
                        "kunReadings": kun_readings,
                        "meaningsEn": meanings_en
                    }
                elem.clear()
    print(f"Resolved metadata for {len(kanji_meta)} / {len(target_kanji)} kanji.")
    return kanji_meta

def main():
    words_file = OUTPUT_CONTENT_JA / "words_mnn_26_32.json"
    if not words_file.exists():
        print(f"Error: {words_file} not found. Run mdx_vocab.py first.")
        sys.exit(1)

    with open(words_file, "r", encoding="utf-8") as f:
        words = json.load(f)

    # Collect target kanji and their introduced chapters and compounds
    target_kanji = set()
    introduced_chapter = {}
    kanji_to_words = {}

    for w in words:
        ch = w["chapter"]
        word_id = w["id"]
        # extract kanji from headword
        word_kanji = set(c for c in w["headword"] if is_kanji(c))
        for k in word_kanji:
            target_kanji.add(k)
            if k not in introduced_chapter or ch < introduced_chapter[k]:
                introduced_chapter[k] = ch
            kanji_to_words.setdefault(k, []).append(word_id)

    print(f"Found {len(target_kanji)} unique kanji across {len(words)} MNN words.")

    # Download datasets
    krad_file = download_cached(KRADFILE_URL, "kradfile.gz")
    kdic_file = download_cached(KANJIDIC2_URL, "kanjidic2.xml.gz")
    kvg_file = download_cached(KANJIVG_URL, "kanjivg.xml.gz")

    components_map = parse_kradfile(krad_file, target_kanji)
    stroke_paths_map = parse_kanjivg(kvg_file, target_kanji)
    meta_map = parse_kanjidic2(kdic_file, target_kanji)

    kanji_entities = []
    compounds_entities = []
    review_queue = []

    for k in sorted(target_kanji):
        meta = meta_map.get(k, {})
        comps = components_map.get(k, [k])
        paths = stroke_paths_map.get(k, [])
        intro_ch = introduced_chapter.get(k)

        if not paths:
            review_queue.append({
                "item": k,
                "reason": "Missing KanjiVG stroke paths",
                "chapter": intro_ch
            })

        kanji_entities.append({
            "literal": k,
            "id": f"ja:kanji:{k}",
            "strokeCount": meta.get("strokeCount", 0),
            "onReadings": meta.get("onReadings", []),
            "kunReadings": meta.get("kunReadings", []),
            "meaningsEn": meta.get("meaningsEn", []),
            "meaningsDe": [],
            "radical": meta.get("radical", ""),
            "radicalNumber": meta.get("radicalNumber"),
            "components": comps,
            "strokePaths": paths,
            "introducedChapter": intro_ch,
            "jlpt": meta.get("jlpt"),
            "grade": meta.get("grade"),
            "frequencyRank": meta.get("frequencyRank"),
            "mnemonicEn": None,
            "contentVersion": 1
        })

        word_ids = kanji_to_words.get(k, [])
        for sort_order, w_id in enumerate(word_ids, 1):
            compounds_entities.append({
                "kanji": k,
                "wordId": w_id,
                "verified": True,
                "sortOrder": sort_order
            })

    # Save outputs
    with open(OUTPUT_CONTENT_JA / "kanji.json", "w", encoding="utf-8") as f:
        json.dump(kanji_entities, f, ensure_ascii=False, indent=2)

    with open(OUTPUT_CONTENT_JA / "kanji_compounds.json", "w", encoding="utf-8") as f:
        json.dump(compounds_entities, f, ensure_ascii=False, indent=2)

    with open(OUTPUT_CONTENT_JA / "review_queue.json", "w", encoding="utf-8") as f:
        json.dump(review_queue, f, ensure_ascii=False, indent=2)

    print(f"Saved: {len(kanji_entities)} kanji to kanji.json, {len(compounds_entities)} compound links to kanji_compounds.json, {len(review_queue)} items in review_queue.json")

if __name__ == "__main__":
    main()
