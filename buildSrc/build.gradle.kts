plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.sam.with.receiver") version libs.versions.kotlin
    // `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
}
