import configureMultiplatform
import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import kotlinx.benchmark.gradle.benchmark

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.multiplatform.id)
    alias(libs.plugins.kotlin.benchmark)
    alias(libs.plugins.kotlin.allopen)
}

allOpen { // jmh benchmark classes must be open
    annotation("org.openjdk.jmh.annotations.State")
}
kotlin {
    configureMultiplatform(
        ext = this,
        explicitApi = false,
        wasmWasi = false,
        android = false,
        linux = false,
        iOs = false,
        macOs = false,
        watchOs = false,
        tvOs = false,
        windows = false,
        wasmJs = false,
    )

}
tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dkotlinx.coroutines.debug=off")
}
dependencies {
    commonMainImplementation(projects.core)

    val fluxo = "0.1-2306082-SNAPSHOT"
    //noinspection UseTomlInstead
    commonMainImplementation("io.github.fluxo-kt:fluxo-core:$fluxo")

    commonMainImplementation(libs.kotlin.coroutines.test)
    commonMainImplementation(libs.kotlin.test)
    commonMainImplementation(libs.kotlin.benchmark)
}

benchmark {
    configurations {
        named("main") {
            iterations = 100
            warmups = 10
            iterationTime = 100
            iterationTimeUnit = "ms"
            outputTimeUnit = "us"
            mode = "avgt"
            reportFormat = "text"
            // advanced("nativeGCAfterIteration", true)
        }
    }
    targets {
        register("jvm") {
            this as JvmBenchmarkTarget
            jmhVersion = libs.versions.jmh.get()
        }
    }
}
