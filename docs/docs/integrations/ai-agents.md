---
sidebar_position: 6
sidebar_label: AI Agents
---

# Use FlowMVI with AI Agents

## LLM-friendly docs

We publish `LLMs.txt` and `LLMs-full.txt` at the documentation root so agents can index the full docs quickly.

```bash
curl -L https://opensource.respawn.pro/FlowMVI/LLMs.txt
curl -L https://opensource.respawn.pro/FlowMVI/LLMs-full.txt
```

You can also fetch any doc page as markdown by appending `.md` to its URL, which makes `curl`-based discovery easy:

```bash
curl -L https://opensource.respawn.pro/FlowMVI/quickstart.md
```

## Claude Code plugin

Install the FlowMVI Claude Code plugin from the Respawn marketplace:

```text
/plugin marketplace add respawn-app/claude-plugin-marketplace
/plugin install flowmvi@respawn-tools
```

## Codex skill

Install the FlowMVI Codex skill with the `skill-installer` skill:

```bash
scripts/install-skill-from-github.py --repo respawn-app/FlowMVI --path skills/flowmvi
```

Restart Codex after installation to pick up the new skill.

## Manual use (no harness)

Download the `skills/flowmvi` folder and place it in your repository, then read `skills/flowmvi/SKILL.md` (and browse `skills/flowmvi/references/`) to learn how to use FlowMVI without Claude Code or Codex.
