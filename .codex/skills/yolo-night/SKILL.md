---
name: yolo-night
description: [OMX] Overnight autonomous project mode for personal repos when the user explicitly says yolo, night mode, sleep mode, 컴퓨터 켜놓고 잘거야, 밤새 작업, 토큰 많이 써도 돼, or asks Codex to maximize useful safe work without asking. Use to run long local edit-test-commit-PR loops with strong guardrails and progress artifacts.
metadata:
  short-description: Overnight autonomous safe-work mode
---

# YOLO Night

Use only after an explicit user request for overnight/YOLO/autonomous high-throughput work.

## Mission
Maximize useful, verified progress while the user is away. Prefer high-impact repo-local work that can be completed, tested, committed, pushed, and PR'd/merged when permissions allow.

## Autonomy defaults
- Do not ask for routine choices. Make a reasonable decision, document it, and continue.
- Work in small vertical slices: inspect → plan slice → edit → test → fix → commit → push/PR when useful.
- Keep going until the task backlog is exhausted, tests are blocked by external services, credentials are missing, or the user stops/cancels.
- Spend tokens on correctness: read code, run tests, inspect failures, and iterate.
- Prefer native subagents for independent lanes: `explore` for repo mapping, `test-engineer` for tests, `code-reviewer`/`verifier` for review, `executor` for implementation.

## Allowed without asking
- Local file edits inside the repo.
- Dependency installation already required by lockfiles (`npm ci`, Gradle wrapper downloads, etc.).
- Non-destructive cleanup of generated artifacts already ignored by git.
- Running lint, test, build, smoke checks, and local dev diagnostics.
- Creating feature branches, commits, PRs, and merging PRs when CI/permissions allow.
- Updating docs, tests, CI, type definitions, API clients, frontend/backend code, and project dependencies when they materially improve delivery.

## Hard stops / ask first
- Force push, history rewrite of shared branches, deleting remote branches, or resetting `main`.
- Deleting user data, databases, credentials, `.env*`, or non-generated assets.
- Changing billing, payments, production infrastructure, DNS, secrets, org settings, repo visibility, or external account settings.
- Adding dependencies is allowed when useful; prefer reputable, maintained packages and record why they were added in commit/PR notes.
- If a command needs a secret not already available through the environment/credential helper.

## Backlog selection order
1. Fix failing tests, CI, lint, typecheck, or build.
2. Complete unfinished user-requested features.
3. Add regression tests for recently changed behavior.
4. Improve reliability/error states/loading/empty states.
5. Improve UX polish already aligned with `DESIGN.md`.
6. Refactor only when it removes duplication or clarifies boundaries without broad churn.
7. Add or upgrade dependencies when they unlock clear product quality, testing, DX, or reliability wins.
8. Update README/DESIGN/API docs to match implemented behavior.

## Verification contract
For every slice, run the smallest proof first, then broader checks before commit:
- Backend: `cd backend && ./gradlew test --no-daemon`
- Frontend: `cd frontend && npm run lint && npm run build`
- CI changes: verify the exact workflow commands locally when possible.

If a check fails, fix and rerun. If blocked, record the blocker and continue with an independent safe slice.

## Git contract
- Work from an up-to-date `origin/main` unless continuing a user-specified branch.
- Use descriptive feature branches.
- Commit each verified slice with the Lore Commit Protocol from AGENTS.md; mention any dependency additions/upgrades in `Constraint:` or `Directive:`.
- Keep diffs reviewable. Prefer multiple small PRs over one huge PR.
- Push and create PRs when credentials allow. Merge only when mergeable and checks are not failing.

## Progress artifacts
Maintain lightweight notes when running long:
- `.omx/notepad.md` or `.omx/state/` when OMX state exists.
- Final report: commits, PRs, merges, verification, known blockers, recommended next backlog.
