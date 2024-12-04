# terraform-github

A project to manage github settings with terraform.

## Adding a new repository

1. Add it to `variables.tf`
2. Add it to `terraform-state-import-all.sh`
3. Execute `./terraform-state-import-repository.sh ${name}` once
4. Execute `terraform plan` and see if the changes make sense
5. Execute `terraform apply` and check if the changes make sense
6. Enter *yes* if they do
