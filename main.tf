# Configure the GitHub Provider
provider "github" {
}

module "repository" {
    source = "./modules/repository"

    for_each = { for repo in local.repositories : repo.name => repo }

    name = each.value.name
    description = each.value.description
    main_branch = lookup(each.value, "main_branch", "main")
    homepage = lookup(each.value, "homepage", null)
    github_pages = lookup(each.value, "github_pages", null)
    archived = lookup(each.value, "archived", null)
    required_status_checks = lookup(each.value, "required_status_checks", [])
    actions_secrets = lookup(each.value, "actions_secrets", {})
}
