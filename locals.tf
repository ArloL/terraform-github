locals {
    repositories_with_actions_secrets = [
        for repo in var.repositories : merge(repo, {
            actions_secrets = {
                for secret in coalesce(repo.actions_secrets, []) :
                secret => lookup(var.secret_values, "${repo.name}-${secret}")
            }
        })
    ]
    repositories_with_homebrew_livecheck_pat = [
        for repo in local.repositories_with_actions_secrets : merge(repo, {
            actions_secrets = (
                coalesce(repo.homebrew_tap_livecheck_pat, false)
                    ? merge(repo.actions_secrets, {
                            "LIVECHECK_PAT": (
                                lookup(var.secret_values, "homebrew-tap-livecheck-pat")
                            )
                        })
                    : repo.actions_secrets
            )
        })
    ]
    repositories = local.repositories_with_homebrew_livecheck_pat
}
