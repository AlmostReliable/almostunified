import java.text.SimpleDateFormat
import java.util.*

val modId: String by project
val modName: String by project
val modVersion: String by project
val modAuthor: String by project
val modDescription: String by project
val license: String by project
val extraModsDirectory: String by project
val minecraftVersion: String by project
val forgeMinVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    java
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "eclipse")
    apply(plugin = "idea")

    version = "$minecraftVersion-$modVersion"

    repositories {
        maven("https://maven.parchmentmc.org/")
        maven("https://maven.shedaniel.me")
        maven("https://dvs1.progwml6.com/files/maven/")
        maven("https://maven.saps.dev/minecraft")
        maven("https://maven.blamejared.com/")
        flatDir {
            name = extraModsDirectory
            dir(file("$extraModsDirectory-$minecraftVersion"))
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    tasks {
        jar {
            manifest {
                attributes(
                    "Specification-Title" to modName,
                    "Specification-Vendor" to modAuthor,
                    "Specification-Version" to archiveVersion,
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to archiveVersion,
                    "Implementation-Vendor" to modAuthor,
                    "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                    "Timestamp" to System.currentTimeMillis(),
                    "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                    "Build-On-Minecraft" to minecraftVersion
                )
            }
        }
        processResources {
            val resourceTargets = listOf("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json")

            val replaceProperties = mapOf(
                "version" to project.version as String,
                "modId" to modId,
                "modName" to modName,
                "modAuthor" to modAuthor,
                "modDescription" to modDescription,
                "license" to license,
                "minecraftVersion" to minecraftVersion,
                "forgeMinVersion" to forgeMinVersion,
                "githubUser" to githubUser,
                "githubRepo" to githubRepo
            )

            inputs.properties(replaceProperties)
            filesMatching(resourceTargets) {
                expand(replaceProperties)
            }
        }
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(17)
        }
        withType<GenerateModuleMetadata> {
            enabled = false
        }
    }
}
