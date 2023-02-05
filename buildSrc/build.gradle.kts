plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.sam.with.receiver") version libs.versions.kotlin
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
}
