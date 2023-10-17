plugins {
    id("pro.respawn.android-library")
}

private val PluginPrefix = "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination"

android {
    namespace = "${Config.artifactId}.android.compose"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
        useLiveLiterals = true
    }

    kotlinOptions {
        freeCompilerArgs += buildList {
            addAll(Config.jvmCompilerArgs)
            if (project.findProperty("enableComposeCompilerReports") == "true") {
                add("-P")
                add("$PluginPrefix=${layout.buildDirectory.get()}/compose_metrics")
                add("-P")
                add("$PluginPrefix=${layout.buildDirectory.get()}/compose_metrics")
            }
        }
        jvmTarget = Config.jvmTarget.target
        languageVersion = Config.kotlinVersion.version
    }
}

dependencies {
    api(projects.core)
    api(projects.android)

    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.preview)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.lifecycle.runtime)
}
