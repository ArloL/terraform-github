#!/bin/bash
set -euo pipefail

echo "Warming up Maven dependency cache..."
PROTOTYPE_DIR="${CLAUDE_PROJECT_DIR:-.}/prototype"
cd "${PROTOTYPE_DIR}"
for i in $(seq 1 10); do
    find "${HOME}/.m2/repository" -name "*.lastUpdated" -delete 2>/dev/null || true
    if ./mvnw --batch-mode --define maven.artifact.threads=1 --threads 1 test-compile --no-transfer-progress --quiet 2>/dev/null; then
        echo "Maven warm-up succeeded on attempt ${i}"
        break
    fi
    echo "Maven warm-up attempt ${i} incomplete, retrying..."
done
./mvnw --batch-mode --define maven.artifact.threads=1 --threads 1 test-compile --no-transfer-progress --quiet || true
