plugins {
    id("com.nek12.android-library")
}

android {
    namespace = "${rootProject.group}.android.view"
}

dependencies {
    api(project(":android"))
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity)
}
