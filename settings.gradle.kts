pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://api.modrinth.com/maven")
        mavenLocal()
        maven("https://repo.terradevelopment.net/repository/maven-releases/")
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        mavenLocal()
        maven("https://api.modrinth.com/maven")
        maven("https://repo.terradevelopment.net/repository/maven-releases/")
        maven("https://mvn.devos.one/snapshots/")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.modmuss50.me/")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://maven.bai.lol")
        maven("https://maven.blamejared.com/")
        maven("https://maven.ryanliptak.com/")
    }
}

rootProject.name = "gc-physics"