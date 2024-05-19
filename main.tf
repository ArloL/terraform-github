# Configure the GitHub Provider
provider "github" {
}

module "repository" {
    source = "./modules/repository"

    for_each = var.repository

    name = each.value.name
    description = each.value.description
    main_branch = lookup(each.value, "main_branch", "main")
    homepage = lookup(each.value, "homepage", null)
    pages_build_type = lookup(each.value, "pages_build_type", null)
    archived = lookup(each.value, "archived", null)
}
