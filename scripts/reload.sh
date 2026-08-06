#!/bin/bash
set -euo pipefail

WD="$(pwd)"
MAVEN_REPO=~/.m2/repository

source .env

# Clean stale maven local cache
rm -rf "$MAVEN_REPO/io/github/leawind/perspectiveapi/perspective_api"

# Build and publish Perspective API to maven local
cd "$PERSPECTIVE_API_DIR"
./gradlew build -x test --offline
./gradlew publishToMavenLocal --offline --no-parallel

# Clear loom cache
cd "$WD"
rm -rf .gradle/loom-cache/remapped_mods/remapped/io/github/leawind/perspectiveapi
