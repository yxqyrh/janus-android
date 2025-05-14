pluginManagement {
    repositories {
        maven {url = uri("https://maven.aliyun.com/repository/public/")}
        maven {url = uri("https://maven.aliyun.com/repository/google/")}
        maven {url = uri("https://maven.aliyun.com/repository/jcenter/")}
        maven {url = uri("https://maven.aliyun.com/repository/central/")}
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {url = uri("https://www.jitpack.io")}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {url = uri("https://maven.aliyun.com/repository/public/")}
        maven {url = uri("https://maven.aliyun.com/repository/google/")}
        maven {url = uri("https://maven.aliyun.com/repository/jcenter/")}
        maven {url = uri("https://maven.aliyun.com/repository/central/")}
        google()
        mavenCentral()
        maven {url = uri("https://www.jitpack.io")}

    }
}

rootProject.name = "Janus Android"
include(":app")
 