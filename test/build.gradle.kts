plugins {
    id("pro.respawn.shared-library")
    alias(libs.plugins.maven.publish)
    dokkaDocumentation
}
android {
    namespace = "${Config.namespace}.test"
}
dependencies {
    commonMainApi(projects.core)
    commonMainImplementation(kotlin("test"))
    commonMainApi(libs.kotlin.coroutines.core)
    commonMainApi(libs.kotlin.coroutines.test)
    commonMainApi(libs.kotlin.atomicfu)
}
