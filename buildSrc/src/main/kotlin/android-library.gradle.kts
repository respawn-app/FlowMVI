plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
}

android {
    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        minSdk = 22
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            setProperty(
                "archivesBaseName",
                project.name
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        prefab = false
        renderScript = false
        mlModelBinding = false
        resValues = false
    }
}
dependencies {
    testImplementation("junit:junit:${Versions.junit}")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
//    androidTestImplementation("androidx.work:work-testing:2.7.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            groupId = rootProject.group.toString()
            artifactId = project.name
            version = rootProject.version.toString()

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    if (it.name != "unspecified") {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", it.group)
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", it.version)
                    }
                }
            }
        }
    }
}

tasks.findByName("publishReleasePublicationToMavenLocal")!!.apply {
    dependsOn("bundleReleaseAar")
}
