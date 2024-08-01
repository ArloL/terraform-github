#!/bin/sh

set -o nounset
set -o xtrace

terraform state rm "module.repository[\"${1}\"].github_repository.repository"
terraform state rm "module.repository[\"$1\"].github_branch_protection.default"
terraform state rm "module.repository[\"${1}\"].github_branch_default.main"
terraform state rm "module.repository[\"${1}\"].github_repository_dependabot_security_updates.security_updates"
