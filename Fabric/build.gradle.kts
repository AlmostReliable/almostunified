@file:Suppress("UnstableApiUsage")

val extraModsDirectory: String by project
val fabricRecipeViewer: String by project
val minecraftVersion: String by project
val fabricVersion: String by project
val fabricLoaderVersion: String by project
val reiVersion: String by project
val jeiVersion: String by project
val kubejsVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val modId: String by project
val modName: String by project

val baseArchiveName = "$modId-fabric-$minecraftVersion"

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
}

base {
    archivesName.set(baseArchiveName)
}

loom {
    shareCaches()

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
            vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
            vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
    }

    mixin {
        defaultRefmapName.set("$modId.refmap.json")
    }
}

dependencies {
    implementation(project(":Common", "namedElements")) { isTransitive = false }

    minecraft("com.mojang:minecraft:$minecraftVersion")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:$mappingsChannel-$minecraftVersion:$mappingsVersion@zip")
    })

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion") // required for fabric rei plugin
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") // required for common jei plugin and mixin
    // runtime only
    when (fabricRecipeViewer) {
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
        else -> throw GradleException("Invalid fabricRecipeViewer value: $fabricRecipeViewer")
    }

    // required for common kubejs plugin and fabric runtime
    modCompileOnly(modLocalRuntime("dev.latvian.mods:kubejs-fabric:$kubejsVersion")!!)

    val extraMods = fileTree("$extraModsDirectory-$minecraftVersion") { include("**/*.jar") }
    if (extraMods.files.isNotEmpty()) {
        // required when running the fabric client with extra mods
        modLocalRuntime("teamreborn:energy:2.2.0")
    }
    extraMods.forEach { f ->
        val sepIndex = f.nameWithoutExtension.lastIndexOf('-')
        if (sepIndex == -1) {
            throw IllegalArgumentException("Invalid mod name: ${f.nameWithoutExtension}")
        }
        val mod = f.nameWithoutExtension.substring(0, sepIndex)
        val version = f.nameWithoutExtension.substring(sepIndex + 1)
        println("Extra mod $mod with version $version detected")
        modLocalRuntime("$extraModsDirectory:$mod:$version")
    }
}

tasks {
    // TODO: test if this is necessary
    jar {
        from("LICENSE") {
            rename { "${it}_${modName}" }
        }
    }
    // TODO: test if this is necessary
    processResources {
        from(project(":Common").sourceSets.main.get().resources)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
    withType<JavaCompile> {
        source(project(":Common").sourceSets.main.get().allSource)
    }
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
