#!/bin/bash
# Imports all existing GitHub resources into Pulumi state before running pulumi up.
# Mirrors the approach of terraform-state-import.sh: ephemeral state, import on every run.
# Resource names are driven by Repositories.java via StateImporter.java.

set -o errexit
set -o nounset

SCRIPT_DIR="$(cd "$(dirname "${0}")" && pwd)"
cd "${SCRIPT_DIR}"

pulumi stack init dev 2>/dev/null || pulumi stack select dev

mvn --quiet exec:java -Dexec.mainClass=io.github.arlol.pulumigithub.StateImporter
