#!/bin/sh

set -o nounset
set -o xtrace

terraform import "module.repository[\"${1}\"].github_repository.repository" "${1}"
terraform import "module.repository[\"$1\"].github_branch_protection.default" "${1}:main"
terraform import "module.repository[\"${1}\"].github_branch_default.main" "${1}"
terraform import "module.repository[\"${1}\"].github_repository_dependabot_security_updates.security_updates" "${1}"
