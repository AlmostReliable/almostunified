plugins {
    idea
    `maven-publish`
    id("fabric-loom") version "0.12-SNAPSHOT"
}

val minecraftVersion: String by project
val fabricVersion: String by project
val fabricLoaderVersion: String by project
val modName: String by project
val modId: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val extraModsDirectory: String by project
val reiVersion: String by project
val jeiVersion: String by project

val baseArchiveName = "${modId}-fabric-${minecraftVersion}"

base {
    archivesName.set(baseArchiveName)
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        // TODO: change this when updating to 1.19.2
        parchment("org.parchmentmc.data:${mappingsChannel}-${minecraftVersion}.2:${mappingsVersion}@zip")
    })
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${reiVersion}")
    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${reiVersion}")

    // required to run the fabric client
    modRuntimeOnly("teamreborn:energy:2.2.0")
    modCompileOnlyApi("mezz.jei:jei-${minecraftVersion}-common-api:${jeiVersion}")
    modCompileOnlyApi("mezz.jei:jei-${minecraftVersion}-fabric-api:${jeiVersion}")

    fileTree("$extraModsDirectory-$minecraftVersion") { include("**/*.jar") }
        .forEach { f ->
            val sepIndex = f.nameWithoutExtension.lastIndexOf('-');
            if(sepIndex == -1) {
                throw IllegalArgumentException("Invalid mod name: ${f.nameWithoutExtension}")
            }
            val mod = f.nameWithoutExtension.substring(0, sepIndex);
            val version = f.nameWithoutExtension.substring(sepIndex + 1);
            println("Extra mod $mod with version $version detected")
            modLocalRuntime("$extraModsDirectory:$mod:$version")
        }

    implementation(project(":Common", "namedElements"))
}

loom {
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
        defaultRefmapName.set("${modId}.refmap.json")
    }
}

tasks {
    jar {
        from("LICENSE") {
            rename { "${it}_${modName}" }
        }
    }
    withType<JavaCompile> {
        source(project(":Common").sourceSets.main.get().allSource)
    }
    processResources {
        from(project(":Common").sourceSets.main.get().resources)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
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
