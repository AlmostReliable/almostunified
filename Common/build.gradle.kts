@file:Suppress("UnstableApiUsage")

plugins {
    `maven-publish`
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

val minecraftVersion: String by project
val modName: String by project
val modId: String by project
val fabricLoaderVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val kubejsVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project

val baseArchiveName = "${modName}-common-${minecraftVersion}"

base {
    archivesName.set(baseArchiveName)
}

loom {
    remapArchives.set(false)
    setupRemappedVariants.set(false)
    runConfigs.configureEach {
        ideConfigGenerated(false)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        // TODO: change this when updating to 1.19.2
        parchment("org.parchmentmc.data:${mappingsChannel}-${minecraftVersion}.2:${mappingsVersion}@zip")
    })
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    modApi("dev.latvian.mods:kubejs-fabric:${kubejsVersion}")

    // required for JEI compat plugin and mixin
    modCompileOnly("mezz.jei:jei-${minecraftVersion}-common:${jeiVersion}")
    // required for REI compat plugin
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:${reiVersion}")

    /**
     * DON'T USE THIS! NEEDED TO COMPILE THIS PROJECT
     */
    modCompileOnly("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    /**
     * Test dependencies
     */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    processResources {
        val buildProps = project.properties

        filesMatching("pack.mcmeta") {
            expand(buildProps)
        }
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
