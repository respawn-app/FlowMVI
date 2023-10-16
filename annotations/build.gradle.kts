plugins {
    // id(libs.plugins.atomicfu.id)
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    configureMultiplatform(this)
}

android {
    configureAndroidLibrary(this)
    namespace = "${Config.namespace}.annotations"
}
