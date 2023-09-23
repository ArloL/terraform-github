terraform {
  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 5.0"
    }
  }
}

# Configure the GitHub Provider
provider "github" {
}

resource "github_repository" "dotfiles" {
  name = "dotfiles"
  description = "My collection of dotfiles used to configure my command line environments."
  visibility = "public"
  allow_merge_commit = false
  allow_squash_merge = false
  delete_branch_on_merge = true
  has_downloads = true
  has_issues = true
  has_projects = true
  has_wiki = true
  vulnerability_alerts = true
}

resource "github_branch_default" "main" {
  repository = github_repository.dotfiles.name
  branch = "main"
}

resource "github_branch_protection" "default" {
  repository_id = github_repository.dotfiles.node_id
  pattern = github_branch_default.main.branch
  force_push_bypassers = []
  push_restrictions = []
}
