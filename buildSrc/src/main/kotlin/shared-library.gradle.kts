@file:Suppress("Unused")

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    signing
}

val libs by versionCatalog

configurePublication()
