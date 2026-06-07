#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="${SCRIPT_DIR}/build"
JAR_PATH="${BUILD_DIR}/libs/chart-renderer.jar"
TEST_CLASSES_DIR="${BUILD_DIR}/test-classes"
TEST_OUTPUT_DIR="${BUILD_DIR}/test-output"
TEST_SOURCES_FILE="${BUILD_DIR}/test-sources.txt"

bash "${SCRIPT_DIR}/build.sh"

rm -rf "${TEST_CLASSES_DIR}" "${TEST_OUTPUT_DIR}"
mkdir -p "${TEST_CLASSES_DIR}" "${TEST_OUTPUT_DIR}"

printf '%s\n' \
  "${SCRIPT_DIR}/src/test/java/io/github/ciscoadmin/tachart/PngAssertions.java" \
  > "${TEST_SOURCES_FILE}"
javac --release 17 -encoding UTF-8 -d "${TEST_CLASSES_DIR}" @"${TEST_SOURCES_FILE}"

render_case() {
  local name="$1"
  shift
  local output="${TEST_OUTPUT_DIR}/${name}.png"

  java -Xms16m -Xmx64m -Djava.awt.headless=true \
    -jar "${JAR_PATH}" \
    "$@" \
    --width 400 \
    --height 300 \
    --scale 2 \
    --output "${output}"

  java -cp "${TEST_CLASSES_DIR}" io.github.ciscoadmin.tachart.PngAssertions "${output}" 800 600 1000
}

render_case "small-failed-label" --passed 31 --failed 1 --title "Regress feature"
render_case "all-failed-no-percent" --passed 0 --failed 767
render_case "empty-placeholder" --passed 0 --failed 0
render_case "large-failed" --passed 936 --failed 7672

echo "Chart renderer smoke tests passed."
