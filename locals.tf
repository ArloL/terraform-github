locals {
    repositories_with_actions_secrets = [
        for repo in var.repositories : merge(repo, {
            actions_secrets = {
                for secret in coalesce(repo.actions_secrets, []) :
                secret => lookup(var.secret_values, "${repo.name}-${secret}")
            }
        })
    ]
    repositories = local.repositories_with_actions_secrets
}
