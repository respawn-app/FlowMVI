import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import kotlinx.benchmark.gradle.benchmark

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
        jvm = true,
    )
}
tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dkotlinx.coroutines.debug=off")
}
dependencies {
    commonMainImplementation(projects.core)
    commonMainImplementation(projects.metrics)

    commonMainImplementation(libs.kotlin.coroutines.test)
    commonMainImplementation(libs.kotlin.test)
    commonMainImplementation(libs.kotlin.benchmark)

    commonMainImplementation(applibs.mvikotlin.main)
    commonMainImplementation(applibs.mvikotlin)
    commonMainImplementation(applibs.mvikotlin.coroutines)
    commonMainImplementation(applibs.orbit.core)
}

benchmark {
    configurations {
        named("main") {
            iterations = 100
            warmups = 20
            iterationTime = 500
            iterationTimeUnit = "ms"
            outputTimeUnit = "us"
            mode = "avgt" // "thrpt" - throughput, "avgt" - average
            reportFormat = "text"
            // advanced("nativeGCAfterIteration", true)
            advanced("jvmForks", "definedByJmh")
        }
    }
    targets {
        register("jvm") {
            this as JvmBenchmarkTarget
            jmhVersion = libs.versions.jmh.get()
        }
    }
}
