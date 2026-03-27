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

export JAVA_HOME="${HOME}/.local/share/java/temurin"
echo "export JAVA_HOME=${JAVA_HOME}" >> "${CLAUDE_ENV_FILE}"
echo "export PATH=${JAVA_HOME}/bin:\${PATH}" >> "${CLAUDE_ENV_FILE}"
echo "export JAVA_TOOL_OPTIONS=\"\${JAVA_TOOL_OPTIONS} -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts\"" >> "${CLAUDE_ENV_FILE}"

echo "Installing Temurin JDK..."
python3 "${SCRIPTS_DIR}/install-temurin.py"

export PATH="${JAVA_HOME}/bin:${PATH}"

sh "${SCRIPTS_DIR}/warmup-maven.sh"
