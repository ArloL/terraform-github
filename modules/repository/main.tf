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
    vulnerability_alerts = true

    lifecycle {
        ignore_changes = [
            pages
        ]
    }
}

resource "github_branch_default" "main" {
    repository = github_repository.repository.name
    branch = var.main_branch
}

resource "github_branch_protection" "default" {
    repository_id = github_repository.repository.node_id
    pattern = github_branch_default.main.branch
    required_linear_history = true
    force_push_bypassers = []
    push_restrictions = []
}
