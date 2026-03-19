locals {
    repositories_with_actions_secrets = [
        for repo in var.repositories : merge(repo, {
            actions_secrets = {
                for secret in coalesce(repo.actions_secrets, []) :
                secret => var.secret_values["${repo.name}-${secret}"]
                if contains(keys(var.secret_values), "${repo.name}-${secret}")
            }
            environments = [
                for env in coalesce(repo.environments, []) : env.name
            ]
            environment_secrets = {
                for pair in flatten([
                    for env in coalesce(repo.environments, []) : [
                        for secret in coalesce(env.secrets, []) : {
                            key   = "${env.name}/${secret}"
                            value = lookup(var.secret_values, "${repo.name}-${env.name}-${secret}", null)
                        }
                    ]
                ]) : pair.key => pair.value
                if pair.value != null
            }
        })
    ]
    repositories = local.repositories_with_actions_secrets
}
