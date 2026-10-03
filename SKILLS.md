# Skills Architecture & Security

## Overview
Skills in TAJ EGY provide domain-specific knowledge, guidelines, system instructions, and specialized prompt templates.

## Adding Skills
1. **Manual Entry**: Specify name, description, trigger keywords, and system instructions in Markdown.
2. **ZIP Import**: Users can import packaged skills containing a `SKILL.md` instruction file and auxiliary resources.

## Security Defenses
- **Zip Slip Defense**: Archive entry paths are validated with `SecurityValidator.validateZipEntry` to ensure canonical destination containment. Entries attempting path traversal (e.g. `../../`) trigger an immediate `SecurityException`.
- **Zip Bomb Defense**: Uncompressed archive size is strictly bounded to 50MB, and entry counts are limited to 200 files.
- **Zero Binary Execution**: Skills are strictly declarative text and Markdown. Executable code, shared libraries, or `.dex` files are prohibited.

## Dynamic Semantic Matching
The `SkillMatcher` evaluates user prompts against skill trigger hints and tags. Only relevant skills are injected into the active prompt, minimizing token consumption and context window overhead.
