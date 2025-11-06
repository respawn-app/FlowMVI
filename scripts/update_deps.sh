#!/bin/bash
set -euo pipefail

./gradlew wrapper --gradle-version=latest
./gradlew wrapper --gradle-version=latest

./gradlew versionCatalogUpdate --no-configuration-cache
./gradlew versionCatalogUpdateSample --no-configuration-cache

cache_dir="$(pwd)/docs/.npm-cache"

(
  cd docs
  npm_config_cache="$cache_dir" npx npm-check-updates -u --reject react,react-dom
  npm_config_cache="$cache_dir" npm install
  npm_config_cache="$cache_dir" npm outdated || true
)
