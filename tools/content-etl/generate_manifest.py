#!/usr/bin/env python3
"""
tools/content-etl/generate_manifest.py

Computes SHA256 checksums of all seed JSON content files and generates:
core/data/src/main/assets/content/manifest.json
"""

import hashlib
import json
import os
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent.parent.parent
CONTENT_DIR = ROOT_DIR / "core/data/src/main/assets/content"

ENTITY_MAP = {
    "words_mnn_26_32.json": "word",
    "words_mnn_other.json": "word",
    "kanji.json": "kanji",
    "kanji_compounds.json": "kanji_compound",
    "grammar_26_32.json": "grammar_rule",
    "grammar_rules.json": "grammar_rule",
    "grammar_examples.json": "grammar_example",
    "confusables.json": "confusable_pair",
    "dialogues.json": "dialogue",
    "dialogue_turns.json": "dialogue_turn",
    "roadmap.json": "vault_roadmap",
    "exercises_cloze_26_32.json": "exercise",
}

def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()

def main():
    manifest_entries = []
    # Traverse CONTENT_DIR recursively
    for p in sorted(CONTENT_DIR.rglob("*.json")):
        if p.name == "manifest.json" or p.name == "review_queue.json" or p.name == "transitivity_pairs.json":
            continue
        rel_path = p.relative_to(CONTENT_DIR).as_posix()
        entity = ENTITY_MAP.get(p.name, "unknown")
        file_hash = sha256_file(p)
        manifest_entries.append({
            "path": rel_path,
            "entity": entity,
            "sha256": file_hash
        })

    manifest = {
        "contentVersion": 1,
        "files": manifest_entries
    }

    manifest_file = CONTENT_DIR / "manifest.json"
    with open(manifest_file, "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)

    print(f"Generated manifest.json with {len(manifest_entries)} files (contentVersion = 1).")

if __name__ == "__main__":
    main()
