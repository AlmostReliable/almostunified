@file:Suppress("UnstableApiUsage")

val junitVersion: String by project
val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val reiVersion: String by project
val jeiVersion: String by project
val kubejsVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val modId: String by project
val modName: String by project

val baseArchiveName = "$modId-common-$minecraftVersion"

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

base {
    archivesName.set(baseArchiveName)
}

loom {
    runConfigs.configureEach {
        ideConfigGenerated(false)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    modCompileOnly("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:$mappingsChannel-$minecraftVersion:$mappingsVersion@zip")
    })

    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion")

    modCompileOnlyApi("dev.latvian.mods:kubejs:$kubejsVersion")

    // JUnit Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    // TODO: test if this is necessary
    processResources {
        val buildProps = project.properties

        filesMatching("pack.mcmeta") {
            expand(buildProps)
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"${modId}\"")
    buildConfigField("String", "MOD_VERSION", "\"${project.version}\"")
    buildConfigField("String", "MOD_NAME", "\"${modName}\"")
    packageName(project.group as String)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = baseArchiveName
            from(components["java"])
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}
