@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

val license: String by project
val minecraftVersion: String by project
val modVersion: String by project
val modId: String by project
val modName: String by project
val modDescription: String by project
val modAuthor: String by project
val modPackage: String by project
val autoServiceVersion: String by project
val junitVersion: String by project
val parchmentVersion: String by project
val fabricApiVersion: String by project
val neoforgeVersion: String by project
val jeiVersion: String by project
val reiVersion: String by project
val emiVersion: String by project
val githubRepo: String by project
val githubUser: String by project

plugins {
    id("architectury-plugin") version "3.4.+"
    id("dev.architectury.loom") version "1.7.+" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    java
    `maven-publish`
}

architectury {
    minecraft = minecraftVersion
}

/**
 * configurations for all projects including the root project
 */
allprojects {
    apply(plugin = "java")

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(21)
        }

        withType<GenerateModuleMetadata> {
            enabled = false
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
    }
}

/**
 * configurations for all projects except the root project
 */
subprojects {
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "maven-publish")

    base {
        archivesName.set("$modId-${project.name.lowercase()}")
        version = "$minecraftVersion-$modVersion"
    }

    repositories {
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.parchmentmc.org") // Parchment
        maven("https://maven.shedaniel.me") // REI
        maven("https://maven.blamejared.com/") // JEI
        maven("https://maven.terraformersmc.com/") // EMI
        mavenLocal()
    }

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")
    loom.silentMojangMappingsLicense()
    loom.createRemapConfigurations(sourceSets.getByName("test")) // create test implementations that allow remapping

    dependencies {
        /**
         * Minecraft
         * Kotlin accessor methods are not generated in this gradle
         * they can be accessed through quoted names instead
         */
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-1.21:$parchmentVersion@zip") // TODO: replace on aprchment update
        })

        /**
         * non-Minecraft dependencies
         */
        compileOnly(testCompileOnly("com.google.auto.service:auto-service:$autoServiceVersion")!!)
        annotationProcessor(testAnnotationProcessor("com.google.auto.service:auto-service:$autoServiceVersion")!!)
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    }

    tasks {
        val apiJar = register<Jar>("apiJar") {
            val remapJar = named<RemapJarTask>("remapJar")
            archiveClassifier.set("api")
            dependsOn(remapJar)
            from(zipTree(remapJar.get().archiveFile))
            include(modPackage.replace('.', '/') + "/api/**")
        }

        build {
            dependsOn(apiJar)
        }

        /**
         * resource processing for defined targets
         * will replace `${key}` with the specified values from the map below
         */
        processResources {
            val resourceTargets = listOf("META-INF/neoforge.mods.toml", "fabric.mod.json", "pack.mcmeta")

            val replaceProperties = mapOf(
                "version" to project.version as String,
                "license" to license,
                "modId" to modId,
                "modName" to modName,
                "minecraftVersion" to minecraftVersion,
                "modAuthor" to modAuthor,
                "modDescription" to modDescription,
                "fabricApiVersion" to fabricApiVersion,
                "neoforgeVersion" to neoforgeVersion,
                "jeiVersion" to jeiVersion,
                "reiVersion" to reiVersion,
                "emiVersion" to emiVersion,
                "githubUser" to githubUser,
                "githubRepo" to githubRepo
            )

            println("[Process Resources] Replacing resource properties for project '${project.name}': ")
            replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

            inputs.properties(replaceProperties)
            filesMatching(resourceTargets) {
                expand(replaceProperties)
            }
        }
    }

    /**
     * Maven publishing
     */
    publishing {
        publications {
            val mpm = project.properties["maven-publish-method"] as String
            println("[Publish Task] Publishing method for project '${project.name}': $mpm")
            register(mpm, MavenPublication::class) {
                artifactId = base.archivesName.get()
                from(components["java"])

                val apiJarTask = tasks.named<Jar>("apiJar")
                artifact(apiJarTask) {
                    classifier = "api"
                }
            }
        }

        /**
         * information on how to set up publishing
         * https://docs.gradle.org/current/userguide/publishing_maven.html
         */
        repositories {
            maven("file://${System.getenv("local_maven")}")
        }
    }

    /**
     * disabling the runtime transformer from Architectury
     * if the runtime transformer should be enabled again, remove this block and
     * add the following to the respective subproject:
     *
     * configurations {
     *     "developmentFabric" { extendsFrom(configurations["common"]) }
     *     "developmentForge" { extendsFrom(configurations["common"]) }
     * }
     */
    architectury {
        compileOnly()
    }
}

/**
 * configurations for all subprojects except the common project
 */
subprojects {
    if (project.path == ":Common") {
        return@subprojects
    }

    apply(plugin = "com.github.johnrengelman.shadow")

    /**
     * add the outputs of the common test source set to the test source set classpath
     */
    sourceSets.named("test") {
        val cst = project(":Common").sourceSets.getByName("test")
        this.compileClasspath += cst.output
        this.runtimeClasspath += cst.output
    }

    extensions.configure<LoomGradleExtensionAPI> {
        runs {
            create("test_client") {
                name("Testmod Client")
                client()
                source(sourceSets.test.get())
                property("fabric-api.gametest", "true")
                property("neoforge.gameTestServer", "true")
                property("neoforge.enabledGameTestNamespaces", "testmod")
                property("$modId.gametest.testPackages", "testmod.*")
                property("$modId.configDir", rootProject.projectDir.toPath().resolve("testmod_configs").toString())
            }

            create("gametest") {
                name("Gametest")
                server()
                source(sourceSets.test.get())
                property("fabric-api.gametest", "true")
                property("neoforge.gameTestServer", "true")
                property("neoforge.enabledGameTestNamespaces", "testmod")
                property("$modId.gametest.testPackages", "testmod.*")
                property("$modId.configDir", rootProject.projectDir.toPath().resolve("testmod_configs").toString())
            }

            forEach {
                val dir = "../run/${project.name.lowercase()}_${it.environment}"
                println("[Run Config] ${project.name} '${it.name}' directory: $dir")
                it.runDir(dir)
                // allows DCEVM hot-swapping when using the JBR (https://github.com/JetBrains/JetBrainsRuntime)
                it.vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
            }
        }

        /**
         * "main" matches the default mod name
         * since `compileOnly()` is being used in Architectury, the local mods for the
         * loaders need to be set up too
         * otherwise, they won't recognize :Common.
         */
        with(mods.maybeCreate("main")) {
            fun Project.sourceSets() = extensions.getByName<SourceSetContainer>("sourceSets")
            sourceSet(sourceSets().getByName("main"))
            sourceSet(project(":Common").sourceSets().getByName("main"))
        }
    }

    val common by configurations.creating
    val shadowCommon by configurations.creating // don't use shadow from the plugin, IDEA shouldn't index this
    configurations {
        "compileClasspath" { extendsFrom(common) }
        "runtimeClasspath" { extendsFrom(common) }
    }

    with(components["java"] as AdhocComponentWithVariants) {
        withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
    }

    tasks {
        named<ShadowJar>("shadowJar") {
            exclude("architectury.common.json")
            configurations = listOf(shadowCommon)
            archiveClassifier.set("dev-shadow")
        }

        named<RemapJarTask>("remapJar") {
            inputFile.set(named<ShadowJar>("shadowJar").get().archiveFile)
            dependsOn("shadowJar")
            archiveClassifier.set(null as String?)
            injectAccessWidener.set(true)
        }

        named<Jar>("jar") {
            archiveClassifier.set("dev")
        }

        named<Jar>("sourcesJar") {
            val commonSources = project(":Common").tasks.named<Jar>("sourcesJar")
            dependsOn(commonSources)
            from(commonSources.get().archiveFile.map { zipTree(it) })
            archiveClassifier.set("sources")
        }
    }
}
