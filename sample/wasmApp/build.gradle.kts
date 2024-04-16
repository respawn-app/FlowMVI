import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrainsCompose)
}


val copyWasmResources = tasks.create("copyWasmResourcesWorkaround", Copy::class.java) {
    from(project(":sample").file("src/commonMain/composeResources"))
    into("build/processedResources/wasmJs/main")
}

afterEvaluate {
    project.tasks.getByName("wasmJsProcessResources").finalizedBy(copyWasmResources)
    project.tasks.getByName("wasmJsDevelopmentExecutableCompileSync").dependsOn(copyWasmResources)
    project.tasks.getByName("wasmJsProductionExecutableCompileSync").dependsOn(copyWasmResources)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            testTask { enabled = false }
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer(port = 8081)).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {

        commonMain.dependencies {
            implementation(projects.sample)
            implementation(applibs.bundles.koin)
            implementation(applibs.decompose)
            implementation(libs.essenty.statekeeper)
            implementation(libs.kotlin.serialization.json)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose.experimental {
    web {
        application { }
    }
}
