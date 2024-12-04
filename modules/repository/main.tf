resource "github_repository" "repository" {
    name = var.name
    description = var.description
    homepage_url = var.homepage
    visibility = "public"
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

    dynamic "pages" {
        for_each = var.pages_build_type == null ? [] : [1]
        content {
            build_type = var.pages_build_type
        }
    }
}

resource "github_branch_default" "main" {
    repository = github_repository.repository.name
    branch = var.main_branch
}

resource "github_branch_protection" "default" {
    repository_id = github_repository.repository.node_id
    pattern = github_branch_default.main.branch
    enforce_admins = true
    required_linear_history = true
    force_push_bypassers = []
    push_restrictions = []
    dynamic "required_status_checks" {
        for_each = var.required_status_checks == null ? [] : [1]
        content {
            contexts = var.required_status_checks
        }
    }
}

resource "github_repository_dependabot_security_updates" "security_updates" {
    repository  = github_repository.repository.id
    enabled     = true
}
