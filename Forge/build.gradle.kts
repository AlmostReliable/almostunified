plugins {
    java
    eclipse
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("org.spongepowered.mixin") version ("0.7-SNAPSHOT")
    `maven-publish`
}

val minecraftVersion: String by project
val mixinVersion: String by project
val forgeVersion: String by project
val modName: String by project
val modAuthor: String by project
val modId: String by project
val mappingsChannel: String by project
val mappingsVersion: String by project
val jeiVersion: String by project
val almostlibVersion: String by project


val baseArchiveName = "${modName}-forge-${minecraftVersion}"

base {
    archivesName.set(baseArchiveName)
}

minecraft {
    mappings(mappingsChannel, "${mappingsVersion}-${minecraftVersion}")

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            taskName("Client")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
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

    compileOnly( fg.deobf("mezz.jei:jei-${minecraftVersion}:${jeiVersion}:api"))
    runtimeOnly( fg.deobf("mezz.jei:jei-${minecraftVersion}:${jeiVersion}"))

    // used for testing only
    runtimeOnly(fg.deobf("curse.maven:JAOPCA-266936:3802370"))
    runtimeOnly(fg.deobf("curse.maven:IE-231951:3755665"))
    runtimeOnly(fg.deobf("curse.maven:cofh-69162:3803484"))
    runtimeOnly(fg.deobf("curse.maven:thermalfoundation-222880:3803495"))
    runtimeOnly(fg.deobf("curse.maven:thermalexp-69163:3803489"))
    runtimeOnly(fg.deobf("curse.maven:mekanism-268560:3810540"))
    runtimeOnly(fg.deobf("curse.maven:nihilo-400012:3810814"))
    runtimeOnly(fg.deobf("curse.maven:industrialforegoing-266515:3848558"))
    runtimeOnly(fg.deobf("curse.maven:titanium-287342:3819942")) // for industrialforegoing

    annotationProcessor("org.spongepowered:mixin:${mixinVersion}:processor")

    /**
     * Test dependencies
     */
    testImplementation(project(":Common"))
    testImplementation(commonTests)
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}-common.mixins.json")
}

tasks.withType<JavaCompile> {
    source(project(":Common").sourceSets.main.get().allSource)
}

tasks.processResources {
    from(project(":Common").sourceSets.main.get().resources)
}

tasks {
    jar {
        finalizedBy("reobfJar")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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
