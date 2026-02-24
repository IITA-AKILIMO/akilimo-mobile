# Scripts

This directory contains utility scripts used by the AKILIMO mobile project.

## `generate_release_notes.py`

Generates Play Store “What’s New” text from Git history and writes the output to:

- `release/distribution/whatsnew/whatsnew-en-GB`
- `release/distribution/whatsnew/whatsnew-sw`

By default, the script:

- Detects the latest relevant Git tag range automatically.
- Uses Ollama to draft concise release notes in English (UK) and Swahili.
- Enforces Play Store-friendly output length (max 500 characters).

### Requirements

- Python 3.12+
- A Git repository with tags
- (Optional) Ollama running locally or remotely if AI generation is enabled

### Usage

Run from the repository root:

```bash
python3 scripts/generate_release_notes.py
```

Useful options:

```bash
# Generate notes for a specific tag
python3 scripts/generate_release_notes.py --tag v1.2.3

# Skip AI generation and use fallback text
python3 scripts/generate_release_notes.py --no-ai

# Override the Ollama model
python3 scripts/generate_release_notes.py --model gemma3
```

### Environment variables

- `OLLAMA_HOST` (default: `http://localhost:11434`)
- `OLLAMA_MODEL` (default: `gemma3`)
