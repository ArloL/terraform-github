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

echo "Warming up Maven dependency cache..."
PROTOTYPE_DIR="${CLAUDE_PROJECT_DIR}/prototype"
cd "${PROTOTYPE_DIR}"
for i in $(seq 1 10); do
    find "${HOME}/.m2/repository" -name "*.lastUpdated" -delete 2>/dev/null || true
    if ./mvnw -Dmaven.artifact.threads=1 -T 1 test-compile --no-transfer-progress -q 2>/dev/null; then
        echo "Maven warm-up succeeded on attempt ${i}"
        break
    fi
    echo "Maven warm-up attempt ${i} incomplete, retrying..."
done
