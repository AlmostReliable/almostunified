val minecraftVersion: String by project
val junitVersion: String by project
val neoforgeVersion: String by project
val neoforgeRecipeViewer: String by project
val enableRuntimeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project

val common by configurations
val shadowCommon by configurations

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    // load the test mod manually because NeoForge always uses main by default
    mods {
        create("testmod") {
            sourceSet(sourceSets.test.get())
            sourceSet(project(":Common").sourceSets.test.get())
        }
    }
}

dependencies {
    // loader
    neoForge("net.neoforged:neoforge:${neoforgeVersion}")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionNeoForge")) { isTransitive = false }
    testImplementation(project(":Common", "namedElements"))

    forgeRuntimeLibrary("org.junit.jupiter:junit-jupiter-api:$junitVersion")

    // compile time mods
    modCompileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:$jeiVersion") { // required for common jei plugin
        isTransitive = false // prevents breaking the forge runtime
    }
    // TODO go back to API when solved: https://github.com/architectury/architectury-loom/issues/204
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion") // required for common rei plugin

    if (enableRuntimeRecipeViewer == "true") {
        // runtime mods
        when (neoforgeRecipeViewer) {
            "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-neoforge:$jeiVersion") { isTransitive = false }
            "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-neoforge:$reiVersion")
            else -> throw GradleException("Invalid forgeRecipeViewer value: $neoforgeRecipeViewer")
        }
    }
}
