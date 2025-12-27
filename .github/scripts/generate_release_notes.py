#!/usr/bin/env python3
"""
Generate Play Store release notes from CHANGELOG.md.
Creates localized release notes within Play Store's 500 character limit.
"""
import re
import sys
from pathlib import Path
from typing import Dict, List, Optional, Tuple


class PlayStoreReleaseNotesGenerator:
    """Generate Play Store release notes with character limits."""

    # Play Store limits
    MAX_LENGTH = 500  # Play Store character limit

    # Version pattern
    VERSION_PATTERN = r'<a name="([^"]+)"></a>\n## \[\1\] - (\d{4}-\d{2}-\d{2})(.*?)(?=<a name="|$)'

    # Priority order for sections (most important first)
    SECTION_PRIORITY = [
        'Features',
        'Bug Fixes',
        'Performance',
        'Security',
        'Breaking Changes',
        'Code Refactoring',
        'Documentation',
    ]

    # Swahili translations
    TRANSLATIONS = {
        'en': {
            'header': 'What\'s new in version {version}',
            'Features': 'New features',
            'Bug Fixes': 'Bug fixes',
            'Performance': 'Performance improvements',
            'Security': 'Security updates',
            'Breaking Changes': 'Important changes',
            'Code Refactoring': 'Improvements',
            'bullet': '• ',
        },
        'sw': {
            'header': 'Nini kipya katika toleo {version}',
            'Features': 'Vipengele vipya',
            'Bug Fixes': 'Marekebisho ya makosa',
            'Performance': 'Maboresho ya utendaji',
            'Security': 'Marekebisho ya usalama',
            'Breaking Changes': 'Mabadiliko muhimu',
            'Code Refactoring': 'Maboresho',
            'bullet': '• ',
        }
    }

    def __init__(self, changelog_path: str = None):
        """Initialize the generator."""
        if changelog_path is None:
            # Default to two directories up
            script_dir = Path(__file__).parent
            changelog_path = script_dir / '..' / '..' / 'CHANGELOG.md'

        self.changelog_path = Path(changelog_path)

        if not self.changelog_path.exists():
            raise FileNotFoundError(f"Changelog not found: {changelog_path}")

    def read_changelog(self) -> str:
        """Read the changelog file."""
        with open(self.changelog_path, 'r', encoding='utf-8') as f:
            return f.read()

    def extract_latest_version(self, content: str) -> Optional[Tuple[str, str, str]]:
        """Extract latest version info."""
        matches = re.findall(self.VERSION_PATTERN, content, re.DOTALL)
        return matches[0] if matches else None

    def parse_sections(self, content: str) -> Dict[str, List[str]]:
        """Parse changelog sections."""
        sections = {}
        current_section = None

        for line in content.split('\n'):
            line = line.strip()

            if line.startswith('###'):
                current_section = line[4:].strip()
                sections[current_section] = []
            elif current_section and line.startswith('-'):
                # Clean the item
                item = line[1:].strip()
                # Remove markdown formatting
                item = re.sub(r'\*\*([^*]+)\*\*:', r'\1:', item)
                item = re.sub(r'\[([^\]]+)\]', r'\1', item)
                sections[current_section].append(item)

        return sections

    def prioritize_items(self, sections: Dict[str, List[str]],
                        max_items: int = 10) -> List[Tuple[str, str]]:
        """
        Prioritize and select most important items.
        Returns list of (section, item) tuples.
        """
        prioritized = []

        # First, add items from priority sections
        for section in self.SECTION_PRIORITY:
            if section in sections:
                for item in sections[section]:
                    prioritized.append((section, item))
                    if len(prioritized) >= max_items:
                        return prioritized

        # Then add from remaining sections
        for section, items in sections.items():
            if section not in self.SECTION_PRIORITY:
                for item in items:
                    prioritized.append((section, item))
                    if len(prioritized) >= max_items:
                        return prioritized

        return prioritized

    def truncate_text(self, text: str, max_length: int) -> str:
        """Truncate text to fit within max_length, ending at sentence."""
        if len(text) <= max_length:
            return text

        # Try to cut at last complete sentence
        truncated = text[:max_length - 3]  # Leave room for "..."

        # Find last sentence boundary
        for delimiter in ['. ', '! ', '? ', '\n']:
            last_delim = truncated.rfind(delimiter)
            if last_delim > max_length * 0.7:  # At least 70% of max length
                return truncated[:last_delim + 1]

        # If no good break point, just cut and add ellipsis
        return truncated.rstrip() + "..."

    def generate_release_notes(self, version: str, sections: Dict[str, List[str]],
                               language: str = 'en') -> str:
        """Generate release notes for specific language."""
        trans = self.TRANSLATIONS.get(language, self.TRANSLATIONS['en'])
        lines = []

        # Add header
        header = trans['header'].format(version=version.lstrip('v'))
        lines.append(header)
        lines.append('')

        # Get prioritized items
        items = self.prioritize_items(sections)

        # Group by section
        current_section = None
        section_items = []

        for section, item in items:
            # Check if adding this would exceed limit
            temp_lines = lines.copy()

            if section != current_section:
                # New section
                if section_items:
                    # Add previous section items
                    temp_lines.extend(section_items)
                    section_items = []

                section_title = trans.get(section, section)
                section_items = [f"{section_title}:"]
                current_section = section

            # Add item
            bullet = trans['bullet']
            section_items.append(f"{bullet}{item}")

            # Check total length
            test_text = '\n'.join(temp_lines + section_items)
            if len(test_text) > self.MAX_LENGTH - 50:  # Leave some buffer
                break

        # Add final section items
        lines.extend(section_items)

        result = '\n'.join(lines)

        # Final truncation if needed
        if len(result) > self.MAX_LENGTH:
            result = self.truncate_text(result, self.MAX_LENGTH)

        return result

    def generate_all(self, output_dir: str = 'fastlane/metadata/android') -> Dict[str, str]:
        """
        Generate release notes for all languages.

        Args:
            output_dir: Base directory for fastlane metadata

        Returns:
            Dictionary mapping language codes to file paths
        """
        # Read changelog
        changelog_content = self.read_changelog()

        # Extract version
        version_info = self.extract_latest_version(changelog_content)
        if not version_info:
            raise ValueError("No version found in CHANGELOG.md")

        version, date, content = version_info
        print(f"Generating release notes for version {version} ({date})")

        # Parse sections
        sections = self.parse_sections(content)

        if not sections:
            raise ValueError("No sections found in changelog")

        # Generate for each language
        output_dir = Path(output_dir)
        generated_files = {}

        for lang_code, trans in self.TRANSLATIONS.items():
            # Generate notes
            notes = self.generate_release_notes(version, sections, lang_code)

            # Determine locale directory
            if lang_code == 'en':
                locale_dir = output_dir / 'en-US'
            elif lang_code == 'sw':
                locale_dir = output_dir / 'sw'
            else:
                locale_dir = output_dir / lang_code

            # Create directory
            locale_dir.mkdir(parents=True, exist_ok=True)

            # Write changelogs directory for Play Store
            changelog_dir = locale_dir / 'changelogs'
            changelog_dir.mkdir(exist_ok=True)

            # Also write to whatsnew for F-Droid compatibility
            whatsnew_file = locale_dir / 'whatsnew'

            # Write files
            char_count = len(notes)

            # For Play Store, we also write version-specific changelog
            # (useful for keeping history)
            version_file = changelog_dir / f'{version.lstrip("v")}.txt'

            for filepath in [whatsnew_file, version_file]:
                filepath.parent.mkdir(parents=True, exist_ok=True)
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(notes)

                generated_files[f"{lang_code}_{filepath.name}"] = str(filepath)
                print(f"✓ Generated {filepath} ({char_count}/{self.MAX_LENGTH} chars)")

        return generated_files


def main():
    """Entry point."""
    import argparse

    parser = argparse.ArgumentParser(
        description='Generate Play Store release notes from CHANGELOG.md'
    )
    parser.add_argument(
        '--changelog',
        default=None,
        help='Path to CHANGELOG.md (default: ../../CHANGELOG.md)'
    )
    parser.add_argument(
        '--output',
        default='fastlane/metadata/android',
        help='Output directory (default: fastlane/metadata/android)'
    )
    parser.add_argument(
        '--preview',
        action='store_true',
        help='Preview release notes without writing files'
    )

    args = parser.parse_args()

    try:
        generator = PlayStoreReleaseNotesGenerator(args.changelog)

        if args.preview:
            # Just show preview
            changelog = generator.read_changelog()
            version_info = generator.extract_latest_version(changelog)

            if not version_info:
                print("❌ No version found in CHANGELOG.md", file=sys.stderr)
                sys.exit(1)

            version, date, content = version_info
            sections = generator.parse_sections(content)

            print(f"Preview for version {version}\n")
            print("=" * 60)

            for lang in ['en', 'sw']:
                notes = generator.generate_release_notes(version, sections, lang)
                lang_name = 'English' if lang == 'en' else 'Swahili'
                print(f"\n{lang_name} ({len(notes)}/{generator.MAX_LENGTH} chars):")
                print("-" * 60)
                print(notes)
                print()
        else:
            # Generate files
            files = generator.generate_all(args.output)
            print(f"\n✓ Successfully generated {len(files)} release note files")

    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()