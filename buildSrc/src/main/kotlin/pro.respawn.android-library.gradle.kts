plugins {
    kotlin("android")
    id("com.android.library")
}

kotlin {
    explicitApi()
}

android {
    configureAndroidLibrary(this)

    kotlinOptions {
        jvmTarget = Config.jvmTarget.target
    }
}
