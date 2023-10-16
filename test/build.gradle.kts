@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("pro.respawn.shared-library")
}
android {
    namespace = "${Config.namespace}.test"
}
dependencies {
    commonMainCompileOnly(project(":annotations"))
    commonMainApi(project(":core"))
    commonMainImplementation(kotlin("test"))
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainApi(libs.kotlin.coroutines.test)
}
