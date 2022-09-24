pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}

val modName: String by extra
val minecraftVersion: String by extra
rootProject.name = "$modName-$minecraftVersion"
include("Common", "Fabric", "Forge")
