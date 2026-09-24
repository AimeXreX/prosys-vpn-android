pluginManagement {
    repositories {
        // Google Maven mirror fallback for networks where dl.google.com is blocked.
        maven {
            name = "GoogleMavenMirror"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
        maven {
            name = "GoogleMavenMirror"
            url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ProSySVPN"
include(":app")
