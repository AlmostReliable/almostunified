val minecraftVersion: String by project
val modId: String by project
val junitVersion: String by project
val neoforgeVersion: String by project
val neoforgeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project

val extraModsPrefix = "extra-mods"

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

repositories {
    flatDir {
        name = extraModsPrefix
        dir(file("$extraModsPrefix-$minecraftVersion"))
    }
}

val common by configurations
val shadowCommon by configurations
val commonTests: SourceSetOutput = project(":Common").sourceSets["test"].output

dependencies {
    // loader
    neoForge("net.neoforged:neoforge:${neoforgeVersion}")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionNeoForge")) { isTransitive = false }

     // compile time mods
//     modCompileOnly("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion") { // required for common jei plugin
//         isTransitive = false // prevents breaking the forge runtime
//     }
     modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion") // required for common rei plugin

     // runtime mods
     when (neoforgeRecipeViewer) {
         "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
         "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion")
         else -> throw GradleException("Invalid forgeRecipeViewer value: $neoforgeRecipeViewer")
     }

    /**
     * helps to load mods in development through an extra directory
     * sadly, this does not support transitive dependencies
     */
    fileTree("$extraModsPrefix-$minecraftVersion") { include("**/*.jar") }
        .forEach { f ->
            val sepIndex = f.nameWithoutExtension.lastIndexOf('-')
            if (sepIndex == -1) {
                throw IllegalArgumentException("Invalid mod name: '${f.nameWithoutExtension}'. Expected format: 'modName-version.jar'")
            }
            val mod = f.nameWithoutExtension.substring(0, sepIndex)
            val version = f.nameWithoutExtension.substring(sepIndex + 1)
            println("Extra mod ${f.nameWithoutExtension} detected.")
            "modLocalRuntime"("extra-mods:$mod:$version")
        }

    modImplementation("curse.maven:applied-energistics-2-223794:4997094")

    // tests
    testImplementation(project(":Common"))
    testImplementation(commonTests)
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
