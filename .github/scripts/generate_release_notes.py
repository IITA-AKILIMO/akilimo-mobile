import re
import os

# Define the paths
changelog_path = 'CHANGELOG.md'
whatsnew_en_path = 'distribution/whatsnew/whatsnew-en-GB'
whatsnew_sw_path = 'distribution/whatsnew/whatsnew-sw'

# Read the CHANGELOG.md file
with open(changelog_path, 'r', encoding='utf-8') as f:
    changelog_content = f.read()

# Find the latest version section
# The pattern looks for a version header like ## [24.2.0] - 2023-09-14
version_pattern = r'<a name="([^"]+)"></a>\n## \[\1\] - (\d{4}-\d{2}-\d{2})(.*?)(?=<a name="|$)'
matches = re.findall(version_pattern, changelog_content, re.DOTALL)

if not matches:
    print("No version information found in CHANGELOG.md")
    exit(1)

# Get the latest version (first match)
latest_version, release_date, content = matches[0]
print(f"Found latest version: {latest_version} released on {release_date}")

# Extract sections from the content
sections = {}
current_section = None

for line in content.split('\n'):
    line = line.strip()
    if line.startswith('###'):
        current_section = line[4:].strip()
        sections[current_section] = []
    elif current_section and line.startswith('-'):
        sections[current_section].append(line)

# Generate English release notes
en_content = []
for section, items in sections.items():
    if items:  # Only include non-empty sections
        en_content.append(section)
        en_content.extend(items)
        en_content.append('')  # Add an empty line between sections

# Write English release notes
os.makedirs(os.path.dirname(whatsnew_en_path), exist_ok=True)
with open(whatsnew_en_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(en_content))

print(f"Generated English release notes at {whatsnew_en_path}")

# Generate Swahili release notes (simplified)
sw_content = [
    f"Utoaji huu wa hivi karibuni wa AKILIMO ni pamoja na:",
]

# Add bullet points for features and bug fixes
if 'Features' in sections and sections['Features']:
    sw_content.append("- Vipengele vipya")

if 'Bug Fixes' in sections and sections['Bug Fixes']:
    sw_content.append("- Marekebisho ya makosa")

if 'Code Refactoring' in sections and sections['Code Refactoring']:
    sw_content.append("- Maboresho ya programu")

# Write Swahili release notes
os.makedirs(os.path.dirname(whatsnew_sw_path), exist_ok=True)
with open(whatsnew_sw_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(sw_content))
    f.write('\n')  # Add final newline

print(f"Generated Swahili release notes at {whatsnew_sw_path}")