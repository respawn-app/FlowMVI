plugins {
    id(libs.plugins.kotlinMultiplatform.id)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

compose.resources {
    publicResClass = true
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = Config.jvmTarget.target
        targetCompatibility = Config.jvmTarget.target
    }
}
kotlin {
    jvm {
        compilerOptions {
            jvmTarget = Config.jvmTarget
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.compose)
            implementation(projects.debugger.debuggerCommon)

            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.animation)
            implementation(compose.animationGraphics)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)

            implementation(applibs.decompose.compose)
            implementation(applibs.decompose)
            implementation(projects.essenty.essentyCompose)
            implementation(projects.essenty)
            implementation(libs.bundles.serialization)
            implementation(libs.bundles.ktor.server)
            implementation(applibs.bundles.kmputils)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlin.collections)
            implementation(applibs.apiresult)
            implementation(libs.uuid)
            implementation(applibs.bundles.koin)
            implementation(libs.kotlin.io)
            implementation(libs.kotlin.atomicfu)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.common)
        }
    }
}
