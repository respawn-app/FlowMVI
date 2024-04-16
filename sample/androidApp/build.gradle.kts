plugins {
    kotlin("android")
    id(applibs.plugins.android.application.id)
    id("kotlin-parcelize")
}

android {
    configureAndroid(this)
    namespace = "${Config.Sample.namespace}.app"
    compileSdk = Config.compileSdk

    defaultConfig {
        applicationId = Config.artifactId
        minSdk = Config.appMinSdk
        targetSdk = Config.targetSdk
        versionCode = Config.versionCode
        versionName = Config.versionName
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    applicationVariants.all {
        setProperty("archivesBaseName", Config.Sample.namespace)
        outputs
            .matching { "apk" in it.outputFile.extension }
            .all {
                this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                outputFileName = "${Config.Sample.namespace}-$versionCode.apk"
            }
    }
    signingConfigs {
        val props = localProperties()
        val passwd = props["signing.password"].toString().trim()
        create("release") {
            keyAlias = "key"
            keyPassword = passwd
            storeFile = File(rootDir, "certificates/keystore.jks")
            storePassword = passwd.trim()
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-debug"
            isShrinkResources = Config.isMinifyEnabledDebug
        }
        release {
            ndk.debugSymbolLevel = "FULL"
            isShrinkResources = Config.isMinifyEnabledRelease
            signingConfig = signingConfigs.getByName("release")
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(projects.sample)

    implementation(libs.kotlin.serialization.json)
    implementation(applibs.bundles.kmputils)
    implementation(applibs.androidx.splashscreen)

    implementation(applibs.compose.activity)
    implementation(applibs.decompose.compose)
    implementation(applibs.decompose)
    implementation(applibs.koin.android)
    implementation(applibs.koin.android.compose)
    implementation(applibs.view.material)

    debugImplementation(libs.compose.tooling)
}
