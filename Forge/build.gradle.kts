val minecraftVersion: String by project
val forgeVersion: String by project
val junitVersion: String by project
val modId: String by project
val forgeRecipeViewer: String by project
val reiVersion: String by project
val jeiVersion: String by project


plugins {
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // Optional property for `gradle.properties` to enable access wideners.
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
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion") // required for common rei plugin | api does not work here
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer on jei plugin
    testCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // don't question this, it's required for compiling
    modCompileOnly("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion") { // required for common jei plugin and mixin
        isTransitive = false // prevents breaking the forge runtime
    }

    // runtime mods
    when (forgeRecipeViewer) {
        "rei" -> modLocalRuntime("me.shedaniel:RoughlyEnoughItems-forge:$reiVersion")
        "jei" -> modLocalRuntime("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion") { isTransitive = false }
        else -> throw GradleException("Invalid forgeRecipeViewer value: $forgeRecipeViewer")
    }

    // tests
    testImplementation(project(":Common"))
    testImplementation(commonTests)
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
