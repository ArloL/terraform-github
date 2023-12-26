#!/bin/sh

set -o nounset
set -o xtrace

sh terraform-state-import-repository.sh abenteuer-irland gh-pages
sh terraform-state-import-repository.sh arlol.github.io gh-pages
sh terraform-state-import-repository.sh business-english gh-pages
sh terraform-state-import-repository.sh chorito
sh terraform-state-import-repository.sh corporate-python
sh terraform-state-import-repository.sh dotfiles
sh terraform-state-import-repository.sh eclipse-projects
sh terraform-state-import-repository.sh effortful-retrieval-questions
sh terraform-state-import-repository.sh git-dora-lead-time-calculator
sh terraform-state-import-repository.sh gitfx
sh terraform-state-import-repository.sh homebrew-tap
sh terraform-state-import-repository.sh menubar-scripts
sh terraform-state-import-repository.sh mvnx
sh terraform-state-import-repository.sh myprojects-cleaner
sh terraform-state-import-repository.sh newlinechecker
sh terraform-state-import-repository.sh nope-amine
sh terraform-state-import-repository.sh postgres-sync-demo
sh terraform-state-import-repository.sh python-nc
sh terraform-state-import-repository.sh rss-to-mail
sh terraform-state-import-repository.sh sci-fi-movies
sh terraform-state-import-repository.sh self-hosted-gh-actions-runner
sh terraform-state-import-repository.sh terraform-github
sh terraform-state-import-repository.sh testcontainers-colima-github-actions
sh terraform-state-import-repository.sh vagrant-ssh-config
sh terraform-state-import-repository.sh vitest-link-reproduction
sh terraform-state-import-repository.sh wait-for-ports
sh terraform-state-import-repository.sh webapp-classloader-test
sh terraform-state-import-repository.sh website-janitor
