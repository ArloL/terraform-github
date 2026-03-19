terraform {
    required_providers {
        github = {
            source = "integrations/github"
        }
    }
}

resource "github_repository" "repository" {
    name = var.name
    description = var.description
    homepage_url = var.homepage
    visibility = var.visibility
    allow_merge_commit = false
    allow_squash_merge = false
    delete_branch_on_merge = true
    has_issues = true
    has_projects = true
    has_wiki = true
    vulnerability_alerts = var.archived == true ? false : true
    archived = var.archived
    allow_auto_merge = true
    auto_init = false

    dynamic "pages" {
        for_each = var.github_pages == true ? [1] : []
        content {
            build_type = "workflow"
        }
    }

    dynamic "security_and_analysis" {
        for_each = var.archived == true ? [] : [1]
        content {
            secret_scanning {
                status = "enabled"
            }
            secret_scanning_push_protection {
                status = "enabled"
            }
        }
    }
}

resource "github_branch_default" "main" {
    repository = github_repository.repository.name
    branch = var.main_branch
}

resource "github_branch_protection" "default" {
    count = (var.archived == true || var.visibility == "private") ? 0 : 1
    repository_id = github_repository.repository.node_id
    pattern = github_branch_default.main.branch
    enforce_admins = true
    required_linear_history = true
    force_push_bypassers = []
    required_status_checks {
        strict = false
        contexts = distinct(concat(
            ["check-actions.required-status-check"],
            ["codeql-analysis.required-status-check"],
            ["CodeQL"],
            ["zizmor"],
            var.required_status_checks == null ? [] : var.required_status_checks
        ))
    }
}

resource "github_repository_dependabot_security_updates" "security_updates" {
    count = var.archived == true ? 0 : 1
    repository  = github_repository.repository.id
    enabled     = true
}

resource "github_repository_environment" "environments" {
    for_each    = toset(coalesce(var.environments, []))

    repository  = github_repository.repository.name
    environment = each.key
}

resource "github_actions_secret" "actions_secrets" {
    for_each        = nonsensitive(toset(keys(coalesce(var.actions_secrets, {}))))

    repository      = github_repository.repository.id
    secret_name     = each.key
    plaintext_value = var.actions_secrets[each.key]
}

resource "github_actions_environment_secret" "environment_secrets" {
    for_each        = nonsensitive(toset(keys(coalesce(var.environment_secrets, {}))))

    repository      = github_repository.repository.id
    environment     = split("/", each.key)[0]
    secret_name     = split("/", each.key)[1]
    plaintext_value = var.environment_secrets[each.key]

    depends_on = [github_repository_environment.environments]
}

resource "github_workflow_repository_permissions" "workflow_permissions" {
    repository                       = github_repository.repository.id
    default_workflow_permissions     = "read"
    can_approve_pull_request_reviews = true
}
