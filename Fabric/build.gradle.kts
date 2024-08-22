val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricRecipeViewer: String by project
val enableRuntimeRecipeViewer: String by project
val jeiVersion: String by project
val reiVersion: String by project
val emiVersion: String by project

val common by configurations
val shadowCommon by configurations

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // optional property for `gradle.properties`
        accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

dependencies {
    // loader
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")

    // common module
    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionFabric")) { isTransitive = false }
    testImplementation(project(":Common", "namedElements"))

    // compile time
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric-api:$jeiVersion") // required for common jei plugin
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion") // required for common rei plugin
    modCompileOnly("dev.emi:emi-fabric:$emiVersion+1.21:api") // required for common emi plugin TODO: replace on EMI release

    // runtime
    if (enableRuntimeRecipeViewer == "true") {
        modLocalRuntime(
            when (fabricRecipeViewer) {
                "jei" -> "mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion"
                "rei" -> "me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion"
                "emi" -> "dev.emi:emi-fabric:$emiVersion+1.21" // TODO: replace on EMI release
                else -> throw GradleException("Invalid fabricRecipeViewer value: $fabricRecipeViewer")
            }
        )
    }
}

/**
 * force the fabric loader and api versions that are defined in the project
 * some mods ship another version which crashes the runtime
 */
configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        force("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")
    }
}
