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
}
