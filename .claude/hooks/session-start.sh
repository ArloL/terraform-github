#!/bin/bash
set -euo pipefail

# Only needed in Claude Code Web remote environment
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
    exit 0
fi

SCRIPTS_DIR="${CLAUDE_PROJECT_DIR}/.claude/scripts"

echo "Configuring Maven proxy..."
python3 "${SCRIPTS_DIR}/configure-maven-proxy.py"

# Single-threaded artifact download avoids parallel 407s through the proxy
echo "export MAVEN_OPTS=\"\${MAVEN_OPTS:+\$MAVEN_OPTS }-Dmaven.artifact.threads=1\"" >> "${CLAUDE_ENV_FILE}"
