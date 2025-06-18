import os
import subprocess
import re

# Define output paths
whatsnew_en_path = 'distribution/whatsnew/whatsnew-en-GB'
whatsnew_sw_path = 'distribution/whatsnew/whatsnew-sw'

# Get the latest Git tag (or fallback to initial commit)
try:
    latest_tag = subprocess.check_output(['git', 'describe', '--tags', '--abbrev=0'], text=True).strip()
except subprocess.CalledProcessError:
    print("No tags found. Using entire commit history.")
    latest_tag = None

# Get commit messages since the last tag
git_log_cmd = ['git', 'log', '--pretty=format:%s']
if latest_tag:
    git_log_cmd.insert(2, f'{latest_tag}..HEAD')

commit_messages = subprocess.check_output(git_log_cmd, text=True).strip().splitlines()

# Prepare user-friendly mappings
friendly_messages = {
    "feature": "New feature added",
    "feat": "New feature added",
    "fix": "Bug fixes and performance improvements",
    "bug": "Bug fixes and performance improvements",
    "refactor": "App improvements and optimizations",
    "ui": "Improved design and usability",
    "perf": "Faster and more reliable performance",
    "update": "App updated with latest changes",
}

def simplify_message(msg: str) -> str:
    for keyword, friendly in friendly_messages.items():
        if msg.lower().startswith(keyword):
            return f"- {friendly}"
    return "- General improvements and updates"

# Process and de-duplicate messages
simplified = list({simplify_message(msg) for msg in commit_messages if msg.strip()})

# Generate Play Store–friendly English notes
os.makedirs(os.path.dirname(whatsnew_en_path), exist_ok=True)
with open(whatsnew_en_path, 'w', encoding='utf-8') as f:
    f.write("What's new:\n")
    f.write('\n'.join(simplified))
    f.write('\n')

print(f"✅ English Play Store release notes written to: {whatsnew_en_path}")

# Generate basic Swahili version
sw_messages = {
    "New feature added": "- Kipengele kipya kimeongezwa",
    "Bug fixes and performance improvements": "- Marekebisho ya hitilafu na uboreshaji wa utendaji",
    "App improvements and optimizations": "- Maboresho ya programu",
    "Improved design and usability": "- Muonekano na matumizi yameboreshwa",
    "Faster and more reliable performance": "- Utendaji bora na wa haraka",
    "App updated with latest changes": "- Programu imesasishwa",
    "General improvements and updates": "- Maboresho ya jumla"
}

swahili_output = [
    "Toleo hili lina:",
]

# Only include Swahili lines matching the simplified messages
for msg in simplified:
    key = msg[2:].strip()  # remove "- " prefix
    sw_msg = sw_messages.get(key)
    if sw_msg:
        swahili_output.append(sw_msg)

# Write Swahili notes
os.makedirs(os.path.dirname(whatsnew_sw_path), exist_ok=True)
with open(whatsnew_sw_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(swahili_output))
    f.write('\n')

print(f"✅ Swahili Play Store release notes written to: {whatsnew_sw_path}")
