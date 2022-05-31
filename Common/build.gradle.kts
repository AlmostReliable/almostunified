plugins {
    `maven-publish`
    id("fabric-loom") version "0.10-SNAPSHOT"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

val minecraftVersion: String by project
val commonRunsEnabled: String by project
val commonClientRunName: String? by project
val commonServerRunName: String? by project
val modName: String by project
val modId: String by project
val fabricLoaderVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project

val baseArchiveName = "${modName}-common-${minecraftVersion}"

base {
    archivesName.set(baseArchiveName)
}

loom {
    remapArchives.set(false);
    setupRemappedVariants.set(false);
    runConfigs.configureEach {
        ideConfigGenerated(false)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:${mappingsChannel}-${minecraftVersion}:${mappingsVersion}@zip")
    })
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    /**
     * DON'T USE THIS! NEEDED TO COMPILE THIS PROJECT
     */
    modCompileOnly("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
}

tasks.processResources {
    val buildProps = project.properties

    filesMatching("pack.mcmeta") {
        expand(buildProps)
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

buildConfig {
    buildConfigField("String", "MOD_ID", "\"${modId}\"")
    buildConfigField("String", "MOD_VERSION", "\"${project.version}\"")
    buildConfigField("String", "MOD_NAME", "\"${modName}\"")

    packageName(project.group as String)
}
