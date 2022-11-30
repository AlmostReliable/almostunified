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

val baseArchiveName = "$modId-common"

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

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
    minecraft("com.mojang:minecraft:$minecraftVersion")
    modCompileOnly("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:$mappingsChannel-$minecraftVersion:$mappingsVersion@zip")
    })

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion") // required for common rei plugin
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer on jei plugin
    testCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // don't question this, it's required for compiling
    modCompileOnly("mezz.jei:jei-$minecraftVersion-common:$jeiVersion") // required for common jei plugin and mixin
    modCompileOnly("dev.latvian.mods:kubejs-fabric:$kubejsVersion") // required for common kubejs plugin | common has remapping issues

    // JUnit Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
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
