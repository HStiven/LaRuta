// settings.gradle.kts (raíz del proyecto)
// Dónde busca Gradle los plugins y librerías, y qué módulos forman el proyecto.

pluginManagement {
    repositories {
        google()
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

rootProject.name = "LaRuta"
include(":app")
