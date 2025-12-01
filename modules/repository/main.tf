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
    count = var.visibility == "private" ? 0 : 1
    repository_id = github_repository.repository.node_id
    pattern = github_branch_default.main.branch
    enforce_admins = true
    required_linear_history = true
    force_push_bypassers = []
    dynamic "required_status_checks" {
        for_each = var.required_status_checks == null ? [] : [1]
        content {
            contexts = var.required_status_checks
        }
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
