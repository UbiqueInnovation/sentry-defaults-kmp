enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "sentry-defaults-kmp"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":sentry")
project(":sentry").projectDir = file("lib")
