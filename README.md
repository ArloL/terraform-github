# terraform-github

A project to manage github settings with terraform.

## Adding a new repository

1. Add it to `variables.tf`
2. Execute `terraform-state-import.sh`
3. Execute `terraform plan` and see if the changes make sense
4. Execute `terraform apply` and check if the changes make sense
5. Enter *yes* if they do

# Secrets

These are the secrets you need:

* homebrew-tap-livecheck
    * Fine-grained token
    * Name: homebrew-tap-livecheck
    * Only selected repositories: ArloL/homebrew-tap
    * Permissions
        * Contents: Read and write
        * Metadata: Read-only
* chorito-trigger
    * Fine-grained token
    * Name: chorito-trigger
    * Only selected repositories
        * See <https://github.com/ArloL/chorito/blob/main/.github/workflows/main.yaml#L287>
    * Permissions
        * Contents: Read and write
        * Metadata: Read-only

And you can add them in a `mise.local.toml` like this

```
[env]
GITHUB_TOKEN='token'
TF_VAR_secret_values=""" {
        "chorito-pat": "token",
        "homebrew-tap-livecheck-pat": "token"
    } """
```
