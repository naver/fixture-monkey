# Overview

This section is written for **AI coding agents** — Claude Code, Cursor, GitHub Copilot, Codex — and for the people configuring them. It is deliberately dense and prescriptive: rules an agent can follow, not a tutorial.

Human readers looking for an introduction should start with [Creating objects](https://naver.github.io/fixture-monkey/ko/docs/get-started/creating-objects) instead.

## The one rule

> Pin the properties the scenario depends on. Leave everything else random.

Fixture Monkey exists so a test does not have to name data it does not care about. An agent that sets every field has written the same brittle fixture a hand-rolled builder would have produced, just with more ceremony.

Setting only what matters has a second effect that matters for agents in particular: the test stops reacting to unrelated production changes. Add a field to a class, rename one the test never mentions, reorder a constructor — a scenario-faithful fixture keeps compiling and keeps passing, and the test file stays out of the diff.

## Pages

| Page | Contents |
| :--- | :--- |
| [Writing tests](https://naver.github.io/fixture-monkey/ko/docs/agent-guide/writing-tests) | The decision procedure: how to tell which properties a scenario depends on, which API to pin them with, and how to verify the result |
| [API reference](https://naver.github.io/fixture-monkey/ko/docs/agent-guide/api-reference) | Condensed Java and Kotlin API surface — selectors, builder methods, setup |

## Wiring this into an agent

**Claude Code** — install the plugin, which packages the rules above as a skill:

```
/plugin marketplace add naver/fixture-monkey --sparse .claude-plugin claude-plugins
/plugin install fixture-monkey@fixture-monkey
```

`--sparse` limits the checkout to the two directories holding the plugin. Without it the whole library source is cloned to reach three small files.

Then ask for a test as usual. The skill activates on its own when a request involves test data; `/fixture-monkey:write-fixture` invokes it explicitly.

**Other agents** — point the tool at these pages. Append `.md` to any of them to get the source text instead of the rendered page, which is what an agent should read:

```
https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests.md
https://naver.github.io/fixture-monkey/docs/agent-guide/api-reference.md
```

These pages are the single source of truth — the repository ships no per-tool copy of them, so there is nothing to drift. For a tool that reads a rules file, point that file at the URLs above rather than pasting the content in:

```markdown
When writing tests that need test data, follow
https://naver.github.io/fixture-monkey/docs/agent-guide/writing-tests
```

That works as a Cursor rule (`.cursor/rules/*.mdc`), an `AGENTS.md` entry, a `CLAUDE.md` line, or a Copilot instructions file.

## Version note

These pages describe the current release line. The selector APIs (`javaGetter`, the Kotlin `Exp` extensions) are marked experimental and have moved packages before — check the [migration guide](https://naver.github.io/fixture-monkey/ko/docs/migration-guide) if the project you are working in is pinned to an older version.
