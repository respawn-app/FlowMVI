plugins {
    id("com.nek12.android-library")
}

dependencies {
    api(project(":android"))
    implementation("androidx.fragment:fragment-ktx:${Versions.fragment}")
    implementation("androidx.activity:activity-ktx:$${Versions.activity}")
}
