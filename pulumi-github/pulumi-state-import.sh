#!/bin/bash
# Imports all existing GitHub resources into Pulumi state before running pulumi up.
# Mirrors the approach of terraform-state-import.sh: ephemeral state, import on every run.
# Resource names must match exactly what RepositoryProvisioner creates.

set -o errexit
set -o nounset

SCRIPT_DIR="$(cd "$(dirname "${0}")" && pwd)"
cd "${SCRIPT_DIR}"

pulumi stack init dev 2>/dev/null || pulumi stack select dev

# ---------------------------------------------------------------------------
# Repository definitions — must stay in sync with Repositories.java.
# Format: "name[:archived][:private][:envName/envName...]"
# Flags:
#   a = archived
#   p = private (non-public, skip branch protection)
# Environments listed after a second colon, comma-separated.
# ---------------------------------------------------------------------------

# Each entry: "name [flags] [environments]"
# flags: a=archived, p=private
declare -A REPO_ARCHIVED=(
    [actions]=1
    [dotnet-http-client-reproduction]=1
    [gitfx]=1
    [graalfx]=1
    [pico-playground]=1
    [self-hosted-gh-actions-runner]=1
    [testcontainers-colima-github-actions]=1
    [vitest-link-reproduction]=1
)

declare -A REPO_PRIVATE=()  # none currently

# Repos with environments: key=repoName, value=comma-separated env names
declare -A REPO_ENVIRONMENTS=(
    [terraform-github]="production"
)

REPOS=(
    abenteuer-irland
    actions
    advent-of-code
    angular-playground
    arlol.github.io
    beatunes-keytocomment
    bulma-playground
    business-english
    calver-tag-action
    chorito
    corporate-python
    dependabot-dockerfile-test
    dotfiles
    dotnet-http-client-reproduction
    eclipse-projects
    effortful-retrieval-questions
    git-dora-lead-time-calculator
    git-presentation-2018-10
    gitfx
    graalfx
    kafka-debugger
    homebrew-tap
    menubar-scripts
    music-stuff
    mvnx
    myprojects-cleaner
    newlinechecker
    nope-amine
    open-webui-runner
    pico-playground
    postgres-sync-demo
    python-nc
    rss-to-mail
    sci-fi-movies
    self-hosted-gh-actions-runner
    terraform-github
    testcontainers-colima-github-actions
    tsaf-parser
    vagrant-ssh-config
    vitest-link-reproduction
    wait-for-ports
    webapp-classloader-test
    website-janitor
)

# ---------------------------------------------------------------------------
# Build the import JSON
# ---------------------------------------------------------------------------

IMPORT_JSON="$(mktemp -t pulumi-import-XXXXX.json)"
trap 'rm -f "${IMPORT_JSON}"' EXIT

echo '{"resources": [' > "${IMPORT_JSON}"
first=1

add_resource() {
    local type="${1}" name="${2}" id="${3}"
    if [ "${first}" -eq 0 ]; then
        echo "," >> "${IMPORT_JSON}"
    fi
    printf '{"type":"%s","name":"%s","id":"%s"}' "${type}" "${name}" "${id}" \
        >> "${IMPORT_JSON}"
    first=0
}

for repo in "${REPOS[@]}"; do
    archived=${REPO_ARCHIVED[${repo}]+1}
    private=${REPO_PRIVATE[${repo}]+1}

    # github_repository
    add_resource \
        "github:index/repository:Repository" \
        "${repo}" \
        "${repo}"

    # github_branch_default
    add_resource \
        "github:index/branchDefault:BranchDefault" \
        "${repo}-default" \
        "${repo}"

    if [ -z "${archived}" ]; then
        # github_repository_dependabot_security_updates
        add_resource \
            "github:index/repositoryDependabotSecurityUpdates:RepositoryDependabotSecurityUpdates" \
            "${repo}-dependabot" \
            "${repo}"

        if [ -z "${private}" ]; then
            # github_branch_protection (public non-archived only)
            add_resource \
                "github:index/branchProtection:BranchProtection" \
                "${repo}-protection" \
                "${repo}:main"
        fi
    fi

    # github_repository_environment (for each environment)
    if [ -n "${REPO_ENVIRONMENTS[${repo}]+1}" ]; then
        IFS=',' read -ra envs <<< "${REPO_ENVIRONMENTS[${repo}]}"
        for env in "${envs[@]}"; do
            add_resource \
                "github:index/repositoryEnvironment:RepositoryEnvironment" \
                "${repo}-env-${env}" \
                "${repo}:${env}"
        done
    fi

    # Note: github_actions_secret and github_actions_environment_secret are
    # skipped here because their existence depends on the TF_VAR_secret_values
    # map. If secrets are configured, add them manually:
    #   add_resource "github:index/actionsSecret:ActionsSecret" \
    #     "${repo}-secret-PAT" "${repo}:PAT"
    #   add_resource "github:index/actionsEnvironmentSecret:ActionsEnvironmentSecret" \
    #     "${repo}-envsecret-${env}-${secret}" "${repo}:${env}:${secret}"
done

echo ']}'  >> "${IMPORT_JSON}"

# ---------------------------------------------------------------------------
# Run the import
# ---------------------------------------------------------------------------

echo "Importing $(grep -o '"type"' "${IMPORT_JSON}" | wc -l) resources..."
pulumi import \
    --file "${IMPORT_JSON}" \
    --yes \
    --skip-preview
