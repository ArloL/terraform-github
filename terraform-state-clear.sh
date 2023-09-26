#!/bin/sh

set -o errexit
set -o nounset
set -o xtrace

terraform state list | cut -f 1 -d '[' | xargs -L 0 terraform state rm
