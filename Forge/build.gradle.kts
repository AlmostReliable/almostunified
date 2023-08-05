val minecraftVersion: String by project
val modId: String by project
val junitVersion: String by project
val forgeVersion: String by project
val forgeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project

val extraModsPrefix = "extra-mods"

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // optional property for `gradle.properties`
        accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
        forge {
            convertAccessWideners.set(true)
            extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        }
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }

    forge {
        mixinConfigs("$modId-common.mixins.json" /*, "$modId-forge.mixins.json"*/)
    }
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
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionForge")) { isTransitive = false }

    // compile time mods
    modCompileOnly("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion") { // required for common jei plugin
        isTransitive = false // prevents breaking the forge runtime
    }
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion") // required for common rei plugin

    // runtime mods
    when (forgeRecipeViewer) {
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
        else -> throw GradleException("Invalid forgeRecipeViewer value: $forgeRecipeViewer")
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


    // tests
    testImplementation(project(":Common"))
    testImplementation(commonTests)
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
