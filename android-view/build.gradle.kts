plugins {
    id("pro.respawn.shared-library")
    id("pro.respawn.android-library")
}

kotlin {
    configureMultiplatform(
        this,
        android = true,
        ios = false,
        jvm = false,
    )
}

android {
    namespace = "${rootProject.group}.android.view"
}

dependencies {
    api(project(":android"))
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
