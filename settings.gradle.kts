pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.ar.sceneform.plugin") {
                useModule("com.google.ar.sceneform:plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // Add any other repositories here (not in build.gradle.kts)
    }
}

rootProject.name = "FinAR"
include(":app")
 