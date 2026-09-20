plugins {
    id("net.fabricmc.fabric-loom") version "1.18.2"
}

val modId: String by project
val modName: String by project
val modVersion: String by project
val modAuthor: String by project
val modDescription: String by project
val license: String by project
val githubUser: String by project
val githubRepo: String by project
val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val jeiVersion = providers.gradleProperty("almostgradle.recipeViewers.jei.version").get()

base.archivesName = "almostunified-fabric-$minecraftVersion"
version = modVersion

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

repositories {
    maven("https://maven.blamejared.com/") // JEI
}

// shared code and resources are taken from the root project, excluding everything that is NeoForge specific
val neoForgeOnlySources = listOf(
    "**/AlmostUnifiedNeoForge.java",
    "**/AlmostUnifiedPlatformNeoForge.java",
    "**/core/ConditionalRecipeLinkFactory.java",
    "**/core/RecipeLoadCondition.java",
    "**/core/NeoForgePlugin.java",
    "**/mixin/worldgen/ServerLifecycleHooksMixin.java",
    "**/mixin/runtime/NeoForgeRecipeManagerMixin.java",
    "**/unification/worldgen/WorldGenBiomeModifier.java",
)
val neoForgeOnlyResources = listOf(
    "META-INF/**",
    "almostunified.neoforge.mixins.json",
    "data/almostunified/neoforge/**",
)

val generatedBuildConfig = layout.buildDirectory.dir("generated/buildconfig")
val generateBuildConfig by tasks.registering {
    val outputDir = generatedBuildConfig
    val modIdValue = modId
    val modNameValue = modName
    val modVersionValue = "$minecraftVersion-$modVersion"
    inputs.property("modId", modIdValue)
    inputs.property("modName", modNameValue)
    inputs.property("modVersion", modVersionValue)
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("com/almostreliable/unified/BuildConfig.java").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.almostreliable.unified;

            public final class BuildConfig {
              public static final String MOD_ID = "$modIdValue";

              public static final String MOD_NAME = "$modNameValue";

              public static final String MOD_VERSION = "$modVersionValue";

              private BuildConfig() {
              }
            }
            """.trimIndent()
        )
    }
}

sourceSets.main {
    java {
        srcDir(rootProject.file("src/main/java"))
        srcDir(generatedBuildConfig)
        exclude(neoForgeOnlySources)
    }
}

tasks.compileJava {
    dependsOn(generateBuildConfig)
}

tasks.named("sourcesJar") {
    dependsOn(generateBuildConfig)
}

tasks.processResources {
    from(rootProject.file("src/main/resources")) {
        exclude(neoForgeOnlyResources)
    }
    val props = mapOf(
        "modId" to modId,
        "modName" to modName,
        "modAuthor" to modAuthor,
        "modDescription" to modDescription,
        "license" to license,
        "githubUser" to githubUser,
        "githubRepo" to githubRepo,
        "minecraftVersion" to minecraftVersion,
        "fabricLoaderVersion" to fabricLoaderVersion,
        "fabricApiVersion" to fabricApiVersion.substringBefore("+"),
        "jeiVersion" to jeiVersion,
        "version" to "$minecraftVersion-$modVersion",
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // recipe viewer
    compileOnly("mezz.jei:jei-$minecraftVersion-fabric-api:$jeiVersion")
    runtimeOnly("mezz.jei:jei-$minecraftVersion-fabric:$jeiVersion")
}

