#!/usr/bin/env python3
"""
Generate release notes from CHANGELOG.md for multiple languages.
"""
import re
import os
import sys
from pathlib import Path
from typing import Dict, List, Tuple, Optional


class ReleaseNotesGenerator:
    """Generates release notes from CHANGELOG.md in multiple languages."""

    # Version pattern to match changelog entries
    VERSION_PATTERN = r'## \[([^\]]+)\] - (\d{4}-\d{2}-\d{2})(.*?)(?=## \[|$)'

    # Swahili translations for common section names
    SWAHILI_TRANSLATIONS = {
        'Features': 'Vipengele vipya',
        'Bug Fixes': 'Marekebisho ya makosa',
        'Code Refactoring': 'Maboresho ya programu',
        'Performance Improvements': 'Maboresho ya utendaji',
        'Documentation': 'Nyaraka',
        'Dependencies': 'Tegemezi',
        'Breaking Changes': 'Mabadiliko makubwa',
    }

    def __init__(self, changelog_path: str, output_dir: str = 'distribution/whatsnew'):
        """
        Initialize the generator.

        Args:
            changelog_path: Path to CHANGELOG.md
            output_dir: Directory for output files
        """
        self.changelog_path = Path(changelog_path)
        self.output_dir = Path(output_dir)

        if not self.changelog_path.exists():
            raise FileNotFoundError(f"Changelog not found: {changelog_path}")

    def read_changelog(self) -> str:
        """Read the changelog file."""
        try:
            with open(self.changelog_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            raise IOError(f"Error reading changelog: {e}")

    def extract_latest_version(self, content: str) -> Optional[Tuple[str, str, str]]:
        """
        Extract the latest version information from changelog.

        Args:
            content: Changelog content

        Returns:
            Tuple of (version, date, content) or None if not found
        """
        matches = re.findall(self.VERSION_PATTERN, content, re.DOTALL)
        if not matches:
            return None

        # First match is the latest version
        version, release_date, version_content = matches[0]
        return (version, release_date, version_content)

    def parse_sections(self, content: str) -> Dict[str, List[str]]:
        """
        Parse changelog content into sections.

        Args:
            content: Version content to parse

        Returns:
            Dictionary mapping section names to lists of items
        """
        sections = {}
        current_section = None

        for line in content.split('\n'):
            line = line.strip()

            if line.startswith('###'):
                current_section = line[4:].strip()
                sections[current_section] = []
            elif current_section and line.startswith('-'):
                # Clean up the bullet point
                item = line[1:].strip()
                sections[current_section].append(item)

        return sections

    def generate_english_notes(self, sections: Dict[str, List[str]]) -> str:
        """
        Generate English release notes.

        Args:
            sections: Parsed sections dictionary

        Returns:
            Formatted English release notes
        """
        lines = []

        for section, items in sections.items():
            if items:
                lines.append(section)
                for item in items:
                    lines.append(f"- {item}")
                lines.append('')  # Empty line between sections

        return '\n'.join(lines).rstrip() + '\n'

    def generate_swahili_notes(self, sections: Dict[str, List[str]],
                              version: str) -> str:
        """
        Generate Swahili release notes.

        Args:
            sections: Parsed sections dictionary
            version: Version number

        Returns:
            Formatted Swahili release notes
        """
        lines = [
            f"Toleo la {version} la AKILIMO lina maboresho yafuatayo:",
            ""
        ]

        # Add translated section summaries
        for section, items in sections.items():
            if items and section in self.SWAHILI_TRANSLATIONS:
                translation = self.SWAHILI_TRANSLATIONS[section]
                count = len(items)
                lines.append(f"- {translation} ({count})")

        return '\n'.join(lines) + '\n'

    def write_file(self, path: Path, content: str) -> None:
        """
        Write content to file, creating directories if needed.

        Args:
            path: Output file path
            content: Content to write
        """
        try:
            path.parent.mkdir(parents=True, exist_ok=True)
            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ Generated: {path}")
        except Exception as e:
            raise IOError(f"Error writing {path}: {e}")

    def generate(self) -> None:
        """Main generation process."""
        # Read and parse changelog
        print(f"Reading changelog from: {self.changelog_path}")
        changelog_content = self.read_changelog()

        # Extract latest version
        version_info = self.extract_latest_version(changelog_content)
        if not version_info:
            print("❌ No version information found in CHANGELOG.md", file=sys.stderr)
            sys.exit(1)

        version, release_date, content = version_info
        print(f"✓ Found version {version} (released {release_date})")

        # Parse sections
        sections = self.parse_sections(content)

        if not sections:
            print("⚠ Warning: No sections found in changelog", file=sys.stderr)

        # Generate and write English notes
        en_content = self.generate_english_notes(sections)
        en_path = self.output_dir / 'whatsnew-en-GB'
        self.write_file(en_path, en_content)

        # Generate and write Swahili notes
        sw_content = self.generate_swahili_notes(sections, version)
        sw_path = self.output_dir / 'whatsnew-sw'
        self.write_file(sw_path, sw_content)

        print(f"\n✓ Successfully generated release notes for version {version}")


def main():
    """Entry point for the script."""
    # Configuration
    # Default to CHANGELOG.md two directories up from the script location
    script_dir = Path(__file__).parent
    default_changelog = script_dir / '..' / '..' / 'CHANGELOG.md'

    changelog_path = os.getenv('CHANGELOG_PATH', str(default_changelog))
    output_dir = os.getenv('WHATSNEW_DIR', 'release/distribution/whatsnew')

    try:
        generator = ReleaseNotesGenerator(changelog_path, output_dir)
        generator.generate()
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()