plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.dokka.gradle)
// TODO: Workaround for https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/2062
    implementation(libs.kotlin.serialization.json)
}
