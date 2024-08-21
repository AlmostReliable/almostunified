val enabledPlatforms: String by project
val minecraftVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val fabricLoaderVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val emiVersion: String by project

plugins {
    id("com.github.gmazzo.buildconfig") version "4.0.4"
}

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // optional property for `gradle.properties`
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

dependencies {
    /**
     * loader
     * required here for the @Environment annotations and the mixin dependencies
     * do NOT use other classes from the Fabric loader
     */
    modCompileOnly("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // compile time
    modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion") // required for jei plugin
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion") // required for rei plugin
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer
    modCompileOnly("dev.emi:emi-xplat-intermediary:$emiVersion+$minecraftVersion:api") // required for emi plugin
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    useJavaOutput()
}
