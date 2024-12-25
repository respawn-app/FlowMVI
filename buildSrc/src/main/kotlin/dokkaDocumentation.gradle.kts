import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    id("org.jetbrains.dokka")
    // id("org.jetbrains.dokka-javadoc")
}

val libs by versionCatalog

dokka {
    dokkaGeneratorIsolation = ClassLoaderIsolation()
    moduleName = project.name
    moduleVersion = project.version.toString()
    pluginsConfiguration.html {
        footerMessage = "© ${Config.vendorName}"
        customAssets.from(rootDir.resolve("docs/static/icon-512-maskable.png"))
        homepageLink = Config.url
    }
    dokkaPublications.configureEach {
        suppressInheritedMembers = false
        suppressObviousFunctions = true
    }
    dokkaSourceSets.configureEach {
        reportUndocumented = false
        enableJdkDocumentationLink = true
        enableAndroidDocumentationLink = true
        enableKotlinStdLibDocumentationLink = true
        skipEmptyPackages = true
        skipDeprecated = true
        jdkVersion = Config.javaVersion.majorVersion.toInt()
        documentedVisibilities(VisibilityModifier.Public)
    }
    // remoteUrl = Config.docsUrl
}

dependencies {
    dokkaPlugin(libs.requireLib("dokka-android"))
}
