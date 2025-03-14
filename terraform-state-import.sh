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

repos=$(gh repo list ArloL \
  --no-archived \
  --limit 200 \
  --json name \
  --jq '.[].name')

for repository in $repos; do
    addr="module.repository[\"${repository}\"].github_repository.repository"
    echo "${addr}" >> work_dir/addrs.txt
done

sort work_dir/addrs.txt | uniq > work_dir/addrs-unique.txt

while IFS= read -r addr; do
    repository=$(repository_from_addr "${addr}")
    state=$(mktemp -u -p work_dir -t state_)
    if [[ "$addr" == *".github_branch_protection."* ]]; then
        pattern=main
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}:${pattern}" >> work_dir/commands.txt
    elif [[ "$addr" == *".github_actions_secret."* ]]; then
        secret_name=$(secret_name_from_addr "${addr}")
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}/${secret_name}" >> work_dir/commands.txt
    else
        echo terraform import \
            -state "${state}" \
            "'${addr}'" \
            "${repository}" >> work_dir/commands.txt
    fi
done < work_dir/addrs-unique.txt

set +o errexit
parallel \
    --verbose \
    --bar \
    --jobs "100%" \
    < work_dir/commands.txt
status=$?
set -o errexit

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

if [ $status -ne 0 ]; then
    exit $status
fi
