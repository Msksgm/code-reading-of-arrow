pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "code-reading-of-arrow"

val kotlin_repo_url: String? by settings
val kotlin_version: String? by settings
val ksp_version: String? by settings
val compose_version: String? by settings

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            if (!kotlin_version.isNullOrBlank()) {
                println("Overriding Kotlin version with $kotlin_version")
                version("kotlin", kotlin_version!!)
            }
            if (!ksp_version.isNullOrBlank()) {
                println("Overriding KSP version with $ksp_version")
                version("kspVersion", ksp_version!!)
            }
            if (!compose_version.isNullOrBlank()) {
                println("Overriding Compose version with $compose_version")
                version("composePlugin", compose_version!!)
            }
        }
    }
}

include("arrow-core")
project(":arrow-core").projectDir = file("arrow-libs/core/arrow-core")

include("arrow-atomic")
project(":arrow-atomic").projectDir = file("arrow-libs/core/arrow-atomic")
