#!/bin/bash

set -o errexit
set -o nounset
set -o xtrace

cleanup() {
    currentExitCode=$?
    rm -rf work_dir
    exit ${currentExitCode}
}

repository_from_addr() {
    echo "${1}" | gsed -n 's/[^"]*"\([^"]*\)".*/\1/p'
}

secret_name_from_addr() {
    echo "${1}" | gsed -n 's/[^"]*"[^"]*"[^"]*"\([^"]*\)".*/\1/p'
}

trap cleanup INT TERM EXIT

rm -f terraform.tfstate
rm -rf work_dir
mkdir -p work_dir

terraform plan \
        -out=work_dir/terraform-plan.out \
    > /dev/null 2>&1

terraform show \
        -no-color \
        work_dir/terraform-plan.out \
    > work_dir/terraform-plan.txt

gsed -n \
        's/^  # \(\S\+\).*/\1/p' \
        work_dir/terraform-plan.txt \
    > work_dir/addrs.txt

commands_file=$(mktemp -p work_dir)

repos=$(gh repo list ArloL \
  --no-archived \
  --limit 200 \
  --json name \
  --jq '.[].name')

for repository in $repos; do
    state=$(mktemp -u -p work_dir -t state_)
    addr="module.repository[\"${repository}\"].github_repository.repository"
    echo terraform import \
        -state "${state}" \
        "'${addr}'" \
        "${repository}" >> "${commands_file}"
done

while IFS= read -r addr; do
    repository=$(repository_from_addr "${addr}")
    state=$(mktemp -u -p work_dir -t state_)
    if [[ "$addr" == *".github_branch_protection."* ]]; then
        pattern=main
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}:${pattern}" >> "${commands_file}"
    elif [[ "$addr" == *".github_actions_secret."* ]]; then
        secret_name=$(secret_name_from_addr "${addr}")
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}/${secret_name}" >> "${commands_file}"
    else
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}" >> "${commands_file}"
    fi
done < work_dir/addrs.txt

parallel \
    --verbose \
    --bar \
    --jobs "100%" \
    < "${commands_file}"

for state in work_dir/state_*; do
    resources=$(terraform state list -state="${state}")
    for resource in $resources; do
        terraform state mv \
            -state="${state}" \
            -state-out=work_dir/terraform.tfstate \
            "${resource}" \
            "${resource}"
    done
done

mv -f \
    work_dir/terraform.tfstate \
    terraform.tfstate
