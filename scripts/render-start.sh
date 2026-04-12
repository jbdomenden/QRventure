#!/usr/bin/env bash
set -euo pipefail

JDK_DIR="${JDK_DIR:-$PWD/.render-jdk}"
JAVA_HOME_DIR="$JDK_DIR/current"
JDK_ARCHIVE="$JDK_DIR/openjdk21.tar.gz"
JDK_URL="https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse"

if [ ! -x "$JAVA_HOME_DIR/bin/java" ]; then
  echo "[render-start] Java 21 not found. Downloading JDK..."
  mkdir -p "$JDK_DIR"
  rm -f "$JDK_ARCHIVE"
  curl -fsSL "$JDK_URL" -o "$JDK_ARCHIVE"

  TMP_DIR="$JDK_DIR/extract"
  rm -rf "$TMP_DIR"
  mkdir -p "$TMP_DIR"
  tar -xzf "$JDK_ARCHIVE" -C "$TMP_DIR"

  EXTRACTED_DIR="$(find "$TMP_DIR" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
  if [ -z "$EXTRACTED_DIR" ]; then
    echo "[render-start] Failed to extract JDK archive."
    exit 1
  fi

  rm -rf "$JAVA_HOME_DIR"
  mv "$EXTRACTED_DIR" "$JAVA_HOME_DIR"
  rm -rf "$TMP_DIR" "$JDK_ARCHIVE"
fi

export JAVA_HOME="$JAVA_HOME_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

echo "[render-start] Using $(java -version 2>&1 | head -n 1)"
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.daemon=false"
exec ./gradlew --no-daemon --max-workers=1 -Dkotlin.compiler.execution.strategy=in-process -Dkotlin.daemon.enabled=false -Dorg.gradle.vfs.watch=false run --args="-port ${PORT:-8020}"
