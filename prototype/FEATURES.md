# Missing Features

## ~~1. Fix All Repository Settings (not just description)~~ DONE

Implemented: `applyFixes()` batches all drifted repo fields (description, homepage, has_issues, has_projects, has_wiki, allow_merge_commit, allow_squash_merge, allow_auto_merge, delete_branch_on_merge, archived) into a single PATCH call. Topics use a separate PUT endpoint via `replaceTopics()`. Topics checking was also added (config, state, diff, fix). Default branch remains check-only.

## 2. Fix Security Settings

Security settings (vulnerability alerts, automated security fixes, secret scanning, push protection) are checked but never fixed. The `checkSecuritySettings()` group is marked as not fixable yet.

### Plan

- Add `GitHubClient` methods:
  - PUT `/repos/{owner}/{repo}/vulnerability-alerts` to enable vulnerability alerts.
  - PUT `/repos/{owner}/{repo}/automated-security-fixes` to enable automated security fixes.
  - PATCH `/repos/{owner}/{repo}` with `security_and_analysis` payload for secret scanning and push protection.
- Add a `fixSecuritySettings()` method in `OrgChecker` that sends the full desired security state when any security setting drifts.
- Follow the same pattern as the existing `fixRepoSettings()` / `fixTopics()` endpoint groups.

## 3. Fix Workflow Settings

Workflow permissions are checked but not fixed.

### Plan

- Add `GitHubClient.updateWorkflowPermissions(owner, repo, defaultPermission, canApprovePullRequestReviews)`.
- Endpoint: PUT `/repos/{owner}/{repo}/actions/permissions/workflow`.
- Call from `applyFixes()` on drift.

## 4. Fix Branch Protection

Branch protection is checked but not fixed.

### Plan

- Add `GitHubClient.updateBranchProtection(owner, repo, branch, ...)`.
- Endpoint: PUT `/repos/{owner}/{repo}/branches/{branch}/protection`.
- Build the full protection payload from `RepositoryArgs` (enforce admins, linear history, force pushes, status checks, pull request reviews, restrictions).
- Call from `applyFixes()` when branch protection drifts are detected.

## 5. Repository Rulesets

Rulesets are in the spec but not implemented at all — no check, no fix.

### Plan

- Add `Ruleset` record to the client package mirroring the GitHub API response.
- Add `RepositoryArgs` fields: `rulesets` (list of ruleset configs with branch patterns, status checks, required reviews, linear history, force push).
- Add `GitHubClient` methods:
  - GET `/repos/{owner}/{repo}/rulesets` to list rulesets.
  - POST `/repos/{owner}/{repo}/rulesets` to create.
  - PUT `/repos/{owner}/{repo}/rulesets/{id}` to update.
- Add diff logic in `OrgChecker.computeDiffs()` comparing desired vs actual rulesets.
- Add fix logic to create/update rulesets.

## 6. GitHub Pages Validation and Fixing

Pages endpoint is queried and the github-pages environment is auto-created, but no actual Pages settings are validated (build type, source branch/path, HTTPS enforcement) and no fixes are applied.

### Plan

- Extend the `Pages` record to include `build_type`, `source` (branch + path), and `https_enforced`.
- Add `RepositoryArgs` fields for pages config: `pagesBuildType`, `pagesSourceBranch`, `pagesSourcePath`.
- Add diff logic comparing actual vs desired pages config.
- Add `GitHubClient` methods:
  - POST `/repos/{owner}/{repo}/pages` to enable pages.
  - PUT `/repos/{owner}/{repo}/pages` to update config.
  - DELETE `/repos/{owner}/{repo}/pages` to disable.
- Check HTTPS enforcement via GET and update via PUT.

## 7. Secret Creation via `--fix`

Secrets are checked for presence/absence but `--fix` does not create missing secrets.

### Plan

- Read `GITHUB_SECRETS` env var as a JSON map.
- Add `GitHubClient` methods:
  - GET `/repos/{owner}/{repo}/actions/secrets/public-key` to get the repo public key.
  - PUT `/repos/{owner}/{repo}/actions/secrets/{name}` to create/update a secret (libsodium sealed box encryption).
  - Same for environment secrets: `/repos/{owner}/{repo}/environments/{env}/secrets/{name}`.
- Key format from env var: `<repo>-<secret>` for action secrets, `<repo>-<env>-<secret>` for environment secrets.
- Add a dependency on a libsodium/NaCl library (e.g. `com.goterl:lazysodium-java`) for sealed box encryption.
- In `applyFixes()`, for each missing secret, look up value in the map and create it. If value not in map, report as unfixable.

## 8. Environment Fixes (reviewers, wait timer, deployment branches)

Environments are checked for existence and secret presence, but the spec requires managing reviewers, wait timers, and deployment branch policies. Fixing environments is also not implemented.

### Plan

- Extend `EnvironmentArgs` with fields: `reviewers`, `waitTimer`, `deploymentBranchPolicies`.
- Extend `Environment` record to capture these from the API response.
- Add diff logic for environment settings beyond just existence and secrets.
- Add `GitHubClient` methods:
  - PUT `/repos/{owner}/{repo}/environments/{name}` to create/update environments with full config.
  - POST/DELETE for deployment branch policies.
- Call from `applyFixes()`.

## 9. Immutable Releases Validation

`GitHubClient.getImmutableReleases()` exists but is never called in `computeDiffs()`.

### Plan

- Add `immutableReleases` boolean field to `RepositoryArgs`.
- In `computeDiffs()`, call `getImmutableReleases()` and compare against the desired value.
- In `applyFixes()`, call the endpoint to enable/disable immutable releases.
- Add `GitHubClient.updateImmutableReleases(owner, repo, enabled)`.

## 10. Owner as CLI Argument (not hardcoded)

The spec says the owner/org is a CLI argument. Currently it's hardcoded to "ArloL".

### Plan

- Parse the first positional CLI argument as the owner.
- Print usage and exit with code 1 if no owner is provided.
- Pass owner through to `OrgChecker` instead of using a hardcoded value.

## 11. Configurable Repo Groups with Defaults

The spec describes repo groups sharing defaults with per-repo overrides. The current implementation has a flat `repositories()` method with a single defaults builder.

### Status

The builder pattern with `defaults.toBuilder()` is already in place, which effectively supports the grouping model. This is **partially implemented** — the mechanism works but there's only one group. Adding more groups is a matter of adding more default builders in `GitHubCheck.repositories()`. No code changes needed — this is a config concern.

## 12. Output: Show API Calls That `--fix` Would Make

The spec says the default (non-fix) output should show the API calls that `--fix` would make (e.g. `Would fix: PATCH /repos/...`).

### Plan

- After printing drifts for a repo, compute and print the API calls that would be made.
- Format: `Would fix: METHOD /path {payload}`.
- This requires the fix logic to be queryable without executing — extract fix plans as data before applying.

## 13. `--verbose` Flag

The spec mentions a `--verbose` flag. Not implemented.

### Plan

- Add `--verbose` CLI flag parsing alongside `--fix`.
- Pass verbosity to `OrgChecker`.
- In verbose mode, print additional detail: full API responses, request/response headers, rate limit status.

## 14. Allow Rebase Merge Check

The spec lists "Allow rebase merge" as a managed setting. The current `RepositoryArgs` has `allowMergeCommit` and `allowSquashMerge` but not `allowRebaseMerge`.

### Plan

- Add `allowRebaseMerge` field to `RepositoryArgs`.
- Add diff check comparing `RepositoryFull.allowRebaseMerge()` against config.
- Include in the PATCH payload for fixes.

## 15. Visibility Check and Fix

The spec lists visibility (public/private) as a managed setting. Not currently checked.

### Plan

- Add `visibility` field to `RepositoryArgs` (enum: PUBLIC, PRIVATE).
- Check `RepositoryFull.visibility()` or `isPrivate()` against desired value.
- Fix via PATCH `/repos/{owner}/{repo}` with `"visibility"` field.

## 16. Required Pull Request Reviews in Branch Protection

The spec lists "Required pull request reviews" as a branch protection setting. Not currently checked.

### Plan

- Add `requiredPullRequestReviews` config to `RepositoryArgs` (dismiss stale reviews, required approving review count, etc.).
- Parse from `BranchProtection` response.
- Add diff logic and include in branch protection update payload.

## 17. Branch Protection Restrictions

The spec lists "Restrictions" as a branch protection setting. Not currently checked.

### Plan

- Add `restrictions` config to `RepositoryArgs` (users, teams, apps that can push).
- Parse from `BranchProtection` response.
- Add diff logic and include in branch protection update payload.
