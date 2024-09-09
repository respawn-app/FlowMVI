#!/bin/bash


# needs to be run 2 times to upgrade both the wrapper script and the exec
./gradlew wrapper --gradle-version=latest
./gradlew wrapper --gradle-version=latest

./gradlew versionCatalogUpdate --no-configuration-cache
