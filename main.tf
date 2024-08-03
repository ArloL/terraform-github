# Configure the GitHub Provider
provider "github" {
    token = file("${path.module}/.github-token")
}

module "repository" {
    source = "./modules/repository"

    for_each = { for repo in var.repository : repo.name => repo }

    name = each.value.name
    description = each.value.description
    main_branch = lookup(each.value, "main_branch", "main")
    homepage = lookup(each.value, "homepage", null)
    pages_build_type = lookup(each.value, "pages_build_type", null)
    archived = lookup(each.value, "archived", null)
    required_status_checks = lookup(each.value, "required_status_checks", [])
}
