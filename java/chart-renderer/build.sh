#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="${SCRIPT_DIR}/src/main/java"
BUILD_DIR="${SCRIPT_DIR}/build"
CLASSES_DIR="${BUILD_DIR}/classes"
JAR_PATH="${BUILD_DIR}/libs/chart-renderer.jar"
SOURCES_FILE="${BUILD_DIR}/sources.txt"

rm -rf "${CLASSES_DIR}"
mkdir -p "${CLASSES_DIR}" "$(dirname "${JAR_PATH}")"

find "${SRC_DIR}" -name '*.java' | sort > "${SOURCES_FILE}"
javac --release 17 -encoding UTF-8 -d "${CLASSES_DIR}" @"${SOURCES_FILE}"
jar --create --file "${JAR_PATH}" --main-class io.github.ciscoadmin.tachart.ChartRenderer -C "${CLASSES_DIR}" .

echo "Built ${JAR_PATH}"
