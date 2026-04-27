pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Bubblymarble"

include(":app")

include(":core:common")
include(":core:data")
include(":core:designsystem")
include(":core:health")
include(":core:ai")
include(":core:foodapi")
include(":core:notifications")

include(":feature:onboarding")
include(":feature:plans")
include(":feature:workouts")
include(":feature:stats")
include(":feature:settings")
include(":feature:meals")
include(":feature:measurements")
