val enabledPlatforms: String by project
val fabricLoaderVersion: String by project
val modId: String by project
val modName: String by project
val modPackage: String by project
val reiVersion: String by project
val jeiVersion: String by project
val kubejsVersion: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val junitVersion: String by project
val minecraftVersion: String by project

plugins {
    id("com.github.gmazzo.buildconfig") version ("4.0.4")
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
    // loader
    // required here for the @Environment annotations and the mixin dependencies
    // Do NOT use other classes from the Fabric loader!
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    // compile time mods
    modCompileOnly("dev.latvian.mods:kubejs:$kubejsVersion") // required for common kubejs plugin | common has remapping issues
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:$reiVersion") // required for common rei plugin
    compileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // required to disable rei compat layer on jei plugin
    testCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:9.+") // don't question this, it's required for compiling
    modCompileOnly("mezz.jei:jei-$minecraftVersion-lib:$jeiVersion") // required for common jei plugin and mixin
    modCompileOnly("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion") // required for common jei plugin and mixin

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    useJavaOutput()
}

// TODO reactivate when specific mod is not annoying anymore
//tasks {
//    withType<Test> {
//        useJUnitPlatform()
//    }
//}
