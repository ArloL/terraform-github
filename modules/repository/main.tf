resource "github_repository" "repository" {
    name = var.name
    description = var.description
    homepage_url = var.homepage
    visibility = var.visibility
    allow_merge_commit = false
    allow_squash_merge = false
    delete_branch_on_merge = true
    has_downloads = true
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
            var.required_status_checks == null ? [] : var.required_status_checks
        ))
    }
}

resource "github_repository_dependabot_security_updates" "security_updates" {
    count = var.archived == true ? 0 : 1
    repository  = github_repository.repository.id
    enabled     = true
}

resource "github_actions_secret" "actions_secrets" {
    for_each        = var.actions_secrets

    repository      = github_repository.repository.id
    secret_name     = each.key
    plaintext_value = each.value
}

resource "github_workflow_repository_permissions" "workflow_permissions" {
    repository                       = github_repository.repository.id
    default_workflow_permissions     = "read"
    can_approve_pull_request_reviews = true
}

resource "github_repository_ruleset" "ruleset_required_status_checks" {
    count       = var.archived == true ? 0 : 1
    name        = "main-branch-rules"
    repository  = github_repository.repository.id
    target      = "branch"
    enforcement = "active"

    conditions {
    ref_name {
        include = ["refs/heads/main"]
        exclude = []
    }
    }

    rules {
        required_linear_history = true

        required_status_checks {
            dynamic "required_check" {
                for_each = distinct(concat(
                    ["check-actions.required-status-check"],
                    ["codeql-analysis.required-status-check"],
                    var.required_status_checks == null ? [] : var.required_status_checks
                ))
                content {
                    context        = required_check.value
                    integration_id = 15368
                }
            }
        }
    }
}
