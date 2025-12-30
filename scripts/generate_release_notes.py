#!/usr/bin/env python3
"""
Generate conversational release notes from CHANGELOG.md using AI.
Creates localized, user-friendly release notes within Play Store's 500 character limit.
"""
import re
import os
import sys
import json
from pathlib import Path
from typing import Dict, List, Optional, Tuple


class ConversationalReleaseNotesGenerator:
    """Generate user-friendly release notes using AI."""

    # Version pattern
    VERSION_PATTERN = r'## \[([^\]]+)\] - (\d{4}-\d{2}-\d{2})(.*?)(?=## \[|$)'

    # Character limit
    MAX_LENGTH = 500

    # API configuration
    API_ENDPOINT = os.getenv('LLM_API_ENDPOINT', 'http://localhost:1234/v1/chat/completions')
    API_KEY = os.getenv('LLM_API_KEY', 'lm-studio')  # LM Studio doesn't require real key
    MODEL = os.getenv('LLM_MODEL', 'local-model')  # LM Studio uses whatever model is loaded

    # Priority order for sections
    SECTION_PRIORITY = [
        'Features',
        'Bug Fixes',
        'Performance Improvements',
        'Security',
        'Breaking Changes',
        'Code Refactoring',
        'Documentation',
    ]

    def __init__(self, changelog_path: str = None, use_ai: bool = True):
        if changelog_path is None:
            script_dir = Path(__file__).parent
            changelog_path = script_dir / '..' / 'CHANGELOG.md'

        self.changelog_path = Path(changelog_path)
        self.use_ai = use_ai

        # place output dir at same level as changelog file
        self.output_dir = self.changelog_path.parent / 'release/distribution/whatsnew'

        if not self.changelog_path.exists():
            raise FileNotFoundError(f"Changelog not found: {changelog_path}")


    def truncate_text(self, text: str, max_length: int) -> str:
        """
        Truncate text to `max_length` characters.
        Attempts to cut at a sentence boundary for readability.
        """
        if len(text) <= max_length:
            return text

        # Try to cut at last full stop before max_length
        cutoff = text.rfind('.', 0, max_length)
        if cutoff == -1:
            # No full stop found, just hard cut
            return text[:max_length - 3] + "..."
        return text[:cutoff + 1]

    def read_changelog(self) -> str:
        """Read the changelog file."""
        with open(self.changelog_path, 'r', encoding='utf-8') as f:
            return f.read()

    def extract_latest_version(self, content: str) -> Optional[Tuple[str, str, str]]:
        """Extract latest version info."""
        matches = re.findall(self.VERSION_PATTERN, content, re.DOTALL)
        if not matches:
            return None
        version, release_date, version_content = matches[0]
        return (version, release_date, version_content)

    def parse_sections(self, content: str) -> Dict[str, List[str]]:
        """Parse changelog sections."""
        sections = {}
        current_section = None

        for line in content.split('\n'):
            line = line.strip()

            if line.startswith('###'):
                current_section = line[4:].strip()
                # Clean section name (remove emojis)
                current_section = re.sub(r'[^\w\s-]', '', current_section).strip()
                sections[current_section] = []
            elif current_section and line.startswith('-'):
                item = line[1:].strip()
                # Remove markdown formatting
                item = re.sub(r'\*\*([^*]+)\*\*:', r'\1:', item)
                item = re.sub(r'\[([^\]]+)\]', r'\1', item)
                sections[current_section].append(item)

        return sections

    async def call_claude_api(self, prompt: str) -> str:
        """Legacy async method - not used with LM Studio."""
        return self.call_llm_api(prompt)

    def call_llm_api(self, prompt: str, language: str = "English") -> str:
        """Call local LLM via LM Studio API."""
        try:
            import requests

            # LM Studio uses OpenAI-compatible API format
            response = requests.post(
                self.API_ENDPOINT,
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {self.API_KEY}"
                },
                json={
                    "model": self.MODEL,
                    "messages": [
                        {
                            "role": "system",
                            "content": f"You are a helpful assistant that writes user-friendly app release notes in {language}. Be concise, friendly, and focus on user benefits."
                        },
                        {
                            "role": "user",
                            "content": prompt
                        }
                    ],
                    "temperature": 0.7,
                    "max_tokens": 500,
                    "stream": False
                },
                timeout=60
            )

            if response.status_code == 200:
                data = response.json()
                return data['choices'][0]['message']['content'].strip()
            else:
                print(f"⚠ LLM API call failed: {response.status_code}")
                print(f"   Response: {response.text}")
                return None
        except ImportError:
            print("⚠ requests not installed. Install with: pip install requests")
            return None
        except requests.exceptions.ConnectionError:
            print("⚠ Cannot connect to LM Studio. Make sure:")
            print("   1. LM Studio is running")
            print("   2. A model is loaded")
            print("   3. Server is started (default: http://localhost:1234)")
            return None
        except Exception as e:
            print(f"⚠ LLM API error: {e}")
            return None

    def generate_english_notes_with_ai(self, version: str, sections: Dict[str, List[str]]) -> str:
        """Generate conversational English release notes using AI."""
        # Prepare the changelog data
        changelog_text = f"Version {version}\n\n"
        for section, items in sections.items():
            if items:
                changelog_text += f"{section}:\n"
                for item in items[:10]:  # Limit items per section
                    changelog_text += f"- {item}\n"
                changelog_text += "\n"

        prompt = f"""Convert these technical changelog entries into friendly, conversational release notes for regular app users.

Requirements:
- EXACTLY {self.MAX_LENGTH} characters maximum (strict limit)
- Friendly, conversational tone
- Focus on user benefits, not technical details
- Simple language for non-technical users
- Start with most exciting features
- Use phrases like "You can now...", "We've improved...", "We've fixed..."
- Write in flowing sentences, NOT bullet points
- Be enthusiastic but natural
- IMPORTANT: End at a complete sentence. Do not cut off mid-sentence.

Technical changelog:
{changelog_text}

Write conversational release notes (must be under {self.MAX_LENGTH} characters and end at a complete sentence):"""

        if self.use_ai:
            print("🤖 Generating conversational English notes with local LLM...")
            ai_content = self.call_llm_api(prompt, "English")

            if ai_content:
                # Clean up the response
                ai_content = ai_content.strip()

                # If it exceeds limit, truncate smartly
                if len(ai_content) > self.MAX_LENGTH:
                    print(f"   Content too long ({len(ai_content)} chars), truncating...")
                    ai_content = self.truncate_text(ai_content, self.MAX_LENGTH)

                return ai_content

        # Fallback to basic version
        return self.generate_english_notes_basic(sections)

    def generate_english_notes_basic(self, sections: Dict[str, List[str]]) -> str:
        """Basic English notes without AI."""
        lines = []
        item_count = 0
        max_items = 15

        for section in self.SECTION_PRIORITY:
            if section in sections and sections[section]:
                clean_section = re.sub(r'[^\w\s-]', '', section).strip()
                lines.append(clean_section)

                for item in sections[section]:
                    if item_count >= max_items:
                        break
                    lines.append(f"- {item}")
                    item_count += 1

                lines.append('')
                if item_count >= max_items:
                    break

        result = '\n'.join(lines).rstrip() + '\n'

        if len(result) > self.MAX_LENGTH:
            result = result[:self.MAX_LENGTH - 3] + "..."

        return result

    def generate_swahili_notes_with_ai(self, version: str, sections: Dict[str, List[str]]) -> str:
        """Generate conversational Swahili release notes using AI."""
        # Prepare changelog summary
        section_summary = []
        for section, items in sections.items():
            if items:
                section_summary.append(f"{section}: {len(items)} changes")

        summary_text = "\n".join(section_summary)

        prompt = f"""Convert these app changes into friendly Swahili (Kiswahili) release notes for the Google Play Store.

Requirements:
- Maximum {self.MAX_LENGTH} characters
- Write ONLY in Swahili (Kiswahili)
- Friendly, conversational tone
- Focus on what users care about
- Write in flowing sentences, NOT bullet points
- Natural and enthusiastic

Changes:
{summary_text}

Write conversational Swahili release notes (max {self.MAX_LENGTH} chars):"""

        if self.use_ai:
            print("🤖 Generating conversational Swahili notes with local LLM...")
            ai_content = self.call_llm_api(prompt, "Swahili")

            if ai_content:
                if len(ai_content) > self.MAX_LENGTH:
                    ai_content = ai_content[:self.MAX_LENGTH - 3] + "..."
                return ai_content

        # Fallback to basic version
        return self.generate_swahili_notes_basic(version, sections)

    def generate_swahili_notes_basic(self, version: str, sections: Dict[str, List[str]]) -> str:
        """Basic Swahili notes without AI."""
        combined = {
            'Vipengele vipya': 0,
            'Marekebisho': 0,
            'Maboresho': 0,
        }

        section_mapping = {
            'Features': 'Vipengele vipya',
            'feat': 'Vipengele vipya',
            'Bug Fixes': 'Marekebisho',
            'Fix': 'Marekebisho',
            'fix': 'Marekebisho',
        }

        for section, items in sections.items():
            if items:
                clean = re.sub(r'[^\w\s-]', '', section).strip()
                mapped = section_mapping.get(section, section_mapping.get(clean, 'Maboresho'))
                combined[mapped] += len(items)

        lines = [f"Toleo la {version} lina maboresho haya:", ""]

        for category, count in combined.items():
            if count > 0:
                lines.append(f"- {category} ({count})")

        return '\n'.join(lines) + '\n'

    def write_file(self, path: Path, content: str) -> None:
        """Write content to file."""
        try:
            path.parent.mkdir(parents=True, exist_ok=True)
            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
            char_count = len(content)
            status = "✓" if char_count <= self.MAX_LENGTH else "⚠"
            print(f"{status} {path} ({char_count}/{self.MAX_LENGTH} chars)")
        except Exception as e:
            raise IOError(f"Error writing {path}: {e}")

    def generate(self) -> None:
        """Main generation process."""
        print(f"Reading changelog from: {self.changelog_path}")
        changelog_content = self.read_changelog()

        version_info = self.extract_latest_version(changelog_content)
        if not version_info:
            print("❌ No version information found in CHANGELOG.md", file=sys.stderr)
            sys.exit(1)

        version, release_date, content = version_info
        print(f"✓ Found version {version} (released {release_date})")

        sections = self.parse_sections(content)

        if not sections:
            print("⚠ Warning: No sections found in changelog", file=sys.stderr)

        # Generate English notes
        if self.use_ai:
            en_content = self.generate_english_notes_with_ai(version, sections)
        else:
            en_content = self.generate_english_notes_basic(sections)

        en_path = self.output_dir / 'whatsnew-en-GB'
        self.write_file(en_path, en_content)

        # Generate Swahili notes
        if self.use_ai:
            sw_content = self.generate_swahili_notes_with_ai(version, sections)
        else:
            sw_content = self.generate_swahili_notes_basic(version, sections)

        sw_path = self.output_dir / 'whatsnew-sw'
        self.write_file(sw_path, sw_content)

        print(f"\n✓ Successfully generated release notes for version {version}")


def main():
    """Entry point."""
    import argparse

    parser = argparse.ArgumentParser(
        description='Generate conversational release notes using AI'
    )
    parser.add_argument(
        '--changelog',
        default=None,
        help='Path to CHANGELOG.md'
    )
    parser.add_argument(
        '--no-ai',
        action='store_true',
        help='Disable AI rewriting (use basic format)'
    )

    args = parser.parse_args()

    try:
        generator = ConversationalReleaseNotesGenerator(
            args.changelog,
            use_ai=not args.no_ai
        )
        generator.generate()
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()