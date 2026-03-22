#!/usr/bin/env bash
set -euo pipefail

# Downloads Jackson JARs via authenticated proxy (reads proxy config from JAVA_TOOL_OPTIONS),
# then compiles and packages the fat JAR without requiring Maven.

PROTO="$(cd "$(dirname "$0")" && pwd)"
M2="${HOME}/.m2/repository"
BASE="https://repo.maven.apache.org/maven2"

download() {
    local gpath="$1" aid="$2" ver="$3"
    local dir="${M2}/${gpath}/${aid}/${ver}"
    mkdir -p "$dir"
    for ext in pom jar; do
        local file="${aid}-${ver}.${ext}"
        local target="${dir}/${file}"
        if [[ -f "$target" && $(wc -c < "$target") -gt 1000 ]]; then
            continue  # already downloaded
        fi
        echo "Downloading ${file}..."
        curl -sf --proxy "http://21.0.0.203:15004" -o "$target" "${BASE}/${gpath}/${aid}/${ver}/${file}" || {
            # Try with proxy auth from JAVA_TOOL_OPTIONS
            local user pass
            user=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyUser=)\S+' || true)
            pass=$(echo "${JAVA_TOOL_OPTIONS:-}" | grep -oP '(?<=-Dhttp\.proxyPassword=)\S+' || true)
            if [[ -n "$user" ]]; then
                curl -sf --proxy "http://21.0.0.203:15004" --proxy-user "${user}:${pass}" \
                    -o "$target" "${BASE}/${gpath}/${aid}/${ver}/${file}"
            else
                echo "ERROR: Could not download ${file}" >&2; exit 1
            fi
        }
    done
}

# Download dependencies
download "com/fasterxml/jackson/core" "jackson-databind" "2.18.3"
download "com/fasterxml/jackson/core" "jackson-core" "2.18.3"
download "com/fasterxml/jackson/core" "jackson-annotations" "2.18.3"

# Compile
CP="${M2}/com/fasterxml/jackson/core/jackson-databind/2.18.3/jackson-databind-2.18.3.jar"
CP="${CP}:${M2}/com/fasterxml/jackson/core/jackson-core/2.18.3/jackson-core-2.18.3.jar"
CP="${CP}:${M2}/com/fasterxml/jackson/core/jackson-annotations/2.18.3/jackson-annotations-2.18.3.jar"

rm -rf "${PROTO}/target/classes" "${PROTO}/target/fat"
mkdir -p "${PROTO}/target/classes"

echo "Compiling..."
javac -cp "$CP" --release 21 -d "${PROTO}/target/classes" \
    "${PROTO}/src/main/java/io/github/arlol/githubcheck/Main.java"

# Package fat JAR
echo "Packaging..."
mkdir -p "${PROTO}/target/fat"
cd "${PROTO}/target/fat"
jar xf "${M2}/com/fasterxml/jackson/core/jackson-databind/2.18.3/jackson-databind-2.18.3.jar"
jar xf "${M2}/com/fasterxml/jackson/core/jackson-core/2.18.3/jackson-core-2.18.3.jar"
jar xf "${M2}/com/fasterxml/jackson/core/jackson-annotations/2.18.3/jackson-annotations-2.18.3.jar"
cp -r "${PROTO}/target/classes/"* .
rm -f META-INF/MANIFEST.MF META-INF/*.SF META-INF/*.DSA META-INF/*.RSA 2>/dev/null || true

jar --create --file="${PROTO}/target/github-check.jar" \
    --main-class=io.github.arlol.githubcheck.Main \
    -C "${PROTO}/target/fat" .

echo "Built: ${PROTO}/target/github-check.jar"
echo ""
echo "Run with: GITHUB_TOKEN=<token> java -jar ${PROTO}/target/github-check.jar"
