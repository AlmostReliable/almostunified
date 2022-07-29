import java.text.SimpleDateFormat
import java.util.*

val license: String by project
val minecraftVersion: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val forgeMinVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    java
    idea
}

allprojects {
    repositories {
        flatDir {
            name = "extra-mods"
            dir(file("extra-mods-$minecraftVersion"))
        }
        mavenLocal()
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.blamejared.com")
        maven("https://maven.shedaniel.me")
        maven("https://dvs1.progwml6.com/files/maven/")
        maven("https://www.cursemaven.com") {
            content {
                includeGroup("curse.maven")
            }
        }
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}


subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withJavadocJar()
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
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.processResources {
        val resourceTargets = listOf<String>("META-INF/mods.toml", "pack.mcmeta", "fabric.mod.json")

        val replaceProperties = mapOf<String, String>(
            "version" to project.version as String,
            "license" to license,
            "modId" to modId,
            "modName" to modName,
            "minecraftVersion" to minecraftVersion,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "forgeMinVersion" to forgeMinVersion,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }
}
