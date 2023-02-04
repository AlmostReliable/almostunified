val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricRecipeViewer: String by project
val reiVersion: String by project
val jeiVersion: String by project

plugins {
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // Optional property for `gradle.properties` to enable access wideners.
        accessWidenerPath.set(project(":Common").loom.accessWidenerPath)
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

val common by configurations
val shadowCommon by configurations
dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modApi("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion+$minecraftVersion")

    common(project(":Common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":Common", "transformProductionFabric")) { isTransitive = false }

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion") // required for common rei plugin
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer on jei plugin
    testCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // don't question this, it's required for compiling
    modCompileOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion") // required for common jei plugin and mixin
    when (fabricRecipeViewer) { // runtime only
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
        else -> throw GradleException("Invalid fabricRecipeViewer value: $fabricRecipeViewer")
    }
}
