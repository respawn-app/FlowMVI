plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

rootProject.group = "com.nek12.flowMVI"
rootProject.version = "0.2.5-alpha"

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradle.versions)
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
    }
}

allprojects {
    repositories {
        // order matters
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-opt-in=kotlin.experimental.ExperimentalTypeInference",
            )
        }
    }

    tasks.withType<JavaCompile> {
        targetCompatibility = "11"
    }

    detekt {
        source = objects.fileCollection().from(
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_SRC_DIR_JAVA,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_TEST_SRC_DIR_JAVA,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_SRC_DIR_KOTLIN,
            io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_TEST_SRC_DIR_KOTLIN,
        )
        buildUponDefaultConfig = true
    }

    dependencies {
        // use rootProject as subprojects libs are ambiguous
        detektPlugins(rootProject.libs.detekt.formatting)
        detektPlugins(rootProject.libs.detekt.compose)
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "1.8"
        reports {
            xml.required.set(false)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(false)
        }
    }
    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektFormat") {
    description = "Formats whole project."
    parallel = true
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    setSource(file(projectDir))
    config.setFrom(File(rootDir, "detekt.yml"))
    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**", "**/.idea/**")
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
    }
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
    description = "Runs the whole project at once."
    parallel = true
    buildUponDefaultConfig = true
    setSource(file(projectDir))
    config.setFrom(File(rootDir, "detekt.yml"))
    include("**/*.kt", "**/*.kts")
    exclude("**/resources/**", "**/build/**", "**/.idea/**")
    reports {
        xml.required.set(false)
        html.required.set(false)
        txt.required.set(false)
    }
}
