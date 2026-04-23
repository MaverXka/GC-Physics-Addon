plugins {
    id("fabric-loom") version "1.15.5"
    id("maven-publish")
    id("dev.galacticraft.mojarn") version "0.6.1+19"
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

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
    maven("https://maven.ryanhcode.dev/releases")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(
        mojarn.mappings(
            "net.fabricmc:yarn:${property("minecraft_version")}+build.${property("yarn_build")}:v2"
        )
    )
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    modApi("dev.ryanhcode.sable:sable-fabric-${property("minecraft_version")}:${property("sable_version")}")

    modLocalRuntime("foundry.veil:veil-fabric-${property("veil_version")}")

    modImplementation("dev.galacticraft:Galacticraft:${property("galacticraft_version")}")
    modApi("dev.galacticraft:MachineLib:${property("machinelib_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name") as String
            from(components["java"])
        }
    }
}