plugins {
    java
    eclipse
    `maven-publish`
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("org.spongepowered.mixin") version ("0.7.+")
}

val minecraftVersion: String by project
val mixinVersion: String by project
val forgeVersion: String by project
val modId: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val extraModsDirectory: String by project
val jeiVersion: String by project

val baseArchiveName = "${modId}-forge-${minecraftVersion}"

base {
    archivesName.set(baseArchiveName)
}

minecraft {
    // TODO: change this when updating to 1.19.2
    mappings(mappingsChannel, "1.18.2-${mappingsVersion}-${minecraftVersion}")

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Client")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            jvmArg("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArg("-XX:+AllowEnhancedClassRedefinition")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Server")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            jvmArg("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArg("-XX:+AllowEnhancedClassRedefinition")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }
    }
}

sourceSets.main.get().resources.srcDir("src/generated/resources")

// from millions of solutions, this is the only one which works... :-)
val commonTests: SourceSetOutput = project(":Common").sourceSets["test"].output

dependencies {
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")
    compileOnly(project(":Common"))

    compileOnly(fg.deobf("mezz.jei:jei-${minecraftVersion}-common:${jeiVersion}"))
    implementation(fg.deobf("mezz.jei:jei-${minecraftVersion}-forge:${jeiVersion}"))

    fileTree("$extraModsDirectory-$minecraftVersion") { include("**/*.jar") }
        .forEach { f ->
            val sepIndex = f.nameWithoutExtension.lastIndexOf('-');
            if(sepIndex == -1) {
                throw IllegalArgumentException("Invalid mod name: ${f.nameWithoutExtension}")
            }
            val mod = f.nameWithoutExtension.substring(0, sepIndex);
            val version = f.nameWithoutExtension.substring(sepIndex + 1);
            println("Extra mod $mod with version $version detected")
            runtimeOnly(fg.deobf("$extraModsDirectory:$mod:$version"))
        }

    annotationProcessor("org.spongepowered:mixin:${mixinVersion}:processor")

    /**
     * Test dependencies
     */
    testImplementation(project(":Common"))
    testImplementation(commonTests)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}-common.mixins.json")
}

tasks {
    jar {
        finalizedBy("reobfJar")
    }
    withType<JavaCompile> {
        source(project(":Common").sourceSets.main.get().allSource)
    }
    withType<Test> {
        useJUnitPlatform()
    }
    processResources {
        from(project(":Common").sourceSets.main.get().resources)
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            artifactId = baseArchiveName
            artifact(tasks.jar)
        }
    }

    repositories {
        maven("file://${System.getenv("local_maven")}")
    }
}
