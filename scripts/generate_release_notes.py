#!/usr/bin/env python3
"""
Generate Play Store release notes from Git tags using Ollama.
Optimised for Play Store's 500 character limit with high conversion impact.
"""

import os
import sys
import subprocess
from pathlib import Path
from typing import Dict, List, Optional, Tuple
import re

class PlayStoreReleaseNotesGeneratorGit:

    MAX_LENGTH = 500

    OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://localhost:11434")
    MODEL = os.getenv("OLLAMA_MODEL", "gemma3")

    LANGUAGE_CONFIG = {
        "en-GB": {
            "name": "English (UK)",
            "system": (
                "You write Play Store release notes in British English. "
                "Be warm, specific, and concise. "
                "Lead with the single most exciting change. "
                "Write in flowing sentences — no bullet points, no markdown. "
                "Never mention git or github related changes. "
                "Never start with 'This update' or 'We'. "
                "Stay under {max_length} characters."
            ),
            "prompt": (
                "Turn these Git commit messages into Play Store release notes.\n\n"
                "Rules:\n"
                "- Under {max_length} characters (hard limit)\n"
                "- Open with the headline improvement, named specifically\n"
                "- Mention 2–4 other changes naturally in following sentences\n"
                "- User-benefit language (what they gain, not what we changed)\n"
                "- End on a complete sentence\n"
                "- No bullet points, no emoji, no markdown\n\n"
                "Commits:\n{changelog}\n\n"
                "Release notes:"
            ),
        },
        "sw": {
            "name": "Swahili",
            "system": (
                "Unaandika maelezo ya toleo jipya kwa Kiswahili cha asili. "
                "Kuwa mfupi, wa kirafiki, na kueleza faida kwa mtumiaji. "
                "Usiongelee git ama github "
                "Sentensi mfululizo — si pointi wala markdown. "
                "Usianze na 'Sasisho hili' au 'Tumefanya'. "
                "Usiepite maneno {max_length}."
            ),
            "prompt": (
                "Badilisha mabadiliko haya ya Git kuwa maelezo ya toleo kwa Kiswahili.\n\n"
                "Masharti:\n"
                "- Chini ya herufi {max_length} (kikomo kigumu)\n"
                "- Anza na maboresho muhimu zaidi, yataje kwa jina\n"
                "- Taja mabadiliko 2–4 mengine kwa sentensi za kawaida\n"
                "- Eleza faida kwa mtumiaji, si mabadiliko ya kiufundi\n"
                "- Malizia kwa sentensi kamili\n"
                "- Bila pointi, emoji, wala markdown\n\n"
                "Mabadiliko:\n{changelog}\n\n"
                "Maelezo ya toleo:"
            ),
        },
    }

    def __init__(self, use_ai: bool = True):
        self.use_ai = use_ai
        script_dir = Path(__file__).parent
        self.output_dir = script_dir.parent / "release" / "distribution" / "whatsnew"

    def get_tags(self) -> List[str]:
        try:
            tags_output = subprocess.check_output(
                ["git", "tag", "--list", "--sort=-v:refname"],
                encoding="utf-8",
                stderr=subprocess.DEVNULL,
            )
            return [t.strip() for t in tags_output.splitlines() if t.strip()]
        except Exception as e:
            sys.exit(f"✗ Failed to get tags: {e}")

    def is_tag_at_head(self, tag: str) -> bool:
        try:
            head_sha = subprocess.check_output(["git", "rev-parse", "HEAD"], encoding="utf-8").strip()
            tag_sha = subprocess.check_output(["git", "rev-parse", tag], encoding="utf-8").strip()
            return head_sha == tag_sha
        except Exception:
            return False

    def get_commits(self, start_ref: str, end_ref: str) -> List[str]:
        try:
            log = subprocess.check_output(
                ["git", "log", f"{start_ref}..{end_ref}", "--pretty=format:%s"],
                encoding="utf-8"
            )
            return [line.strip() for line in log.splitlines() if line.strip()]
        except Exception as e:
            print(f"⚠ Failed to get commits: {e}")
            return []

    def build_changelog_summary(self, commits: List[str], max_items: int = 15) -> str:
        return "\n".join(f"- {c}" for c in commits[:max_items])

    def call_ollama(self, system: str, prompt: str) -> Optional[str]:
        try:
            import requests
            response = requests.post(
                f"{self.OLLAMA_HOST}/api/chat",
                json={
                    "model": self.MODEL,
                    "messages": [
                        {"role": "system", "content": system},
                        {"role": "user", "content": prompt},
                    ],
                    "stream": False,
                    "options": {"temperature": 0.65, "top_p": 0.9},
                },
                timeout=120,
            )
            if response.status_code == 200:
                return response.json()["message"]["content"].strip()
            return None
        except Exception:
            return None

    def generate_notes(self, locale: str, version: str, commits: List[str]) -> str:
        cfg = self.LANGUAGE_CONFIG[locale]
        changelog_summary = self.build_changelog_summary(commits)

        system = cfg["system"].format(max_length=self.MAX_LENGTH)
        prompt = cfg["prompt"].format(max_length=self.MAX_LENGTH, changelog=changelog_summary)

        if self.use_ai:
            print(f"🤖 Generating {cfg['name']} notes for {version}…")
            result = self.call_ollama(system, prompt)
            if result:
                result = self.truncate_to_sentence(result, self.MAX_LENGTH)
                return result

        return f"Version {version} includes {len(commits)} improvements and fixes."

    def truncate_to_sentence(self, text: str, max_length: int) -> str:
        if len(text) <= max_length:
            return text
        cutoff = text.rfind(".", 0, max_length)
        if cutoff == -1:
            return text[:max_length - 1] + "…"
        return text[:cutoff + 1]

    def write_file(self, path: Path, content: str):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        print(f"✓ Written: {path.name}")

    def generate(self, target_tag: Optional[str] = None):
        tags = self.get_tags()
        if not tags:
            sys.exit("✗ No Git tags found.")

        if target_tag:
            if target_tag not in tags:
                sys.exit(f"✗ Tag '{target_tag}' not found.")
            idx = tags.index(target_tag)
            if idx + 1 < len(tags):
                start_ref, end_ref, version = tags[idx+1], target_tag, target_tag
            else:
                sys.exit(f"✗ Tag '{target_tag}' is the oldest tag.")
        else:
            latest_tag = tags[0]
            if self.is_tag_at_head(latest_tag):
                if len(tags) > 1:
                    start_ref, end_ref, version = tags[1], latest_tag, latest_tag
                    print(f"📄 HEAD is at {latest_tag}. Comparing with {start_ref}")
                else:
                    sys.exit(f"✗ HEAD is at {latest_tag}, but no previous tag found.")
            else:
                start_ref, end_ref, version = latest_tag, "HEAD", "Unreleased"
                print(f"📄 Comparing {start_ref}..HEAD (Unreleased)")

        commits = self.get_commits(start_ref, end_ref)
        if not commits:
            sys.exit("✗ No commits found in range.")

        for locale, filename in [("en-GB", "whatsnew-en-GB"), ("sw", "whatsnew-sw")]:
            notes = self.generate_notes(locale, version, commits)
            self.write_file(self.output_dir / filename, notes)

def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--tag", help="Specific tag to generate notes for")
    parser.add_argument("--no-ai", action="store_true")
    parser.add_argument("--model")
    args = parser.parse_args()

    if args.model: os.environ["OLLAMA_MODEL"] = args.model
    generator = PlayStoreReleaseNotesGeneratorGit(use_ai=not args.no_ai)
    generator.generate(target_tag=args.tag)

if __name__ == "__main__":
    main()
