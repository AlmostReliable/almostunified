val enabledPlatforms: String by project
val fabricLoaderVersion: String by project
val modId: String by project
val modName: String by project
val modPackage: String by project
val reiVersion: String by project
val jeiVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val junitVersion: String by project
val minecraftVersion: String by project

plugins {
    id("com.github.gmazzo.buildconfig") version ("3.1.0")
}

architectury {
    common(enabledPlatforms.split(","))
}

loom {
    if (project.findProperty("enableAccessWidener") == "true") { // Optional property for `gradle.properties` to enable access wideners.
        accessWidenerPath.set(file("src/main/resources/$modId.accesswidener"))
        println("Access widener enabled for project ${project.name}. Access widener path: ${loom.accessWidenerPath.get()}")
    }
}

dependencies {
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion") // required for common rei plugin
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer on jei plugin
    testCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // don't question this, it's required for compiling
    modCompileOnlyApi("mezz.jei:jei-$minecraftVersion-common:$jeiVersion") // required for common jei plugin
    modCompileOnly("mezz.jei:jei-$minecraftVersion-gui:$jeiVersion") // required for jei mixin

    // The Fabric loader is required here to use the @Environment annotations and to get the mixin dependencies.
    // Do NOT use other classes from the Fabric loader!
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // JUnit Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
}

// TODO reactivate when specific mod is not annoying anymore
//tasks {
//    withType<Test> {
//        useJUnitPlatform()
//    }
//}
