<h1 align="center">
    <a href="https://github.com/AlmostReliable/almostunified"><img src=https://i.imgur.com/3b7Gjkn.png" alt="Preview" width=200></a>
    <p>Almost Unified</p>
</h1>

<div align="center">

A [Minecraft] mod to unify resources.

[![Version][version_badge]][version_link]
[![Total Downloads CF][total_downloads_cf_badge]][curseforge]
[![Total Downloads MR][total_downloads_mr_badge]][modrinth]

[![Workflow Status][workflow_status_badge]][workflow_status_link]
![License][license_badge]
[![Discord][discord_badge]][discord]
[![Wiki][wiki_badge]][wiki]

</div>

## **📖 Information**
For an in-depth explanation of the mod, its functionality, config descriptions, FAQs and more, check out the [wiki].

## **🔧 Manual Installation**
1. Download the latest **mod jar** from the [releases], from [CurseForge] or [Modrinth].
2. Install Minecraft [NeoForge] or [Fabric].
3. Drop the **jar file** into your mods folder.

## **🔗 Depending on the Mod**

### Maven
Every release of this project is built on and published to the [BlameJared] maven.

```groovy
repositories {
    maven {
        url = 'https://maven.blamejared.com'
        name = 'BlameJared Maven'
    }
}
```

### Common [![Maven][maven_common_badge]][maven_common_link]
```groovy
dependencies {
    modCompileOnly("com.almostreliable.mods:almostunified-common:<version>:api")
}
```

### Fabric [![Maven][maven_fabric_badge]][maven_fabric_link]
```groovy
dependencies {
    modCompileOnly("com.almostreliable.mods:almostunified-fabric:<version>:api")
    modRuntimeOnly("com.almostreliable.mods:almostunified-fabric:<version>")
}
```

### NeoForge [![Maven][maven_neoforge_badge]][maven_neoforge_link]
```groovy
dependencies {
    compileOnly("com.almostreliable.mods:almostunified-neoforge:<version>:api")
    runtimeOnly("com.almostreliable.mods:almostunified-neoforge:<version>")
}
```

## **💚 Credits**
The logo was made by [mo_shark].

## **🎓 License**
This project is All Rights Reserved. Forks should only be created with the intent of submitting changes to the upstream
repository via pull requests.

The license applies exclusively to the project's source code. Versions published on CurseForge and Modrinth may be used
for gameplay and modpack creation, provided they are downloaded directly from these platforms. GitHub releases are
intended for private use or as a fallback if CurseForge and Modrinth are unavailable.

The project's API can be freely used in other mods, as long as this project is not an included dependency. This includes
custom inclusion methods like Jar-in-Jar.

Redistribution of builds or rehosting is strictly prohibited.

<!-- Badge Images -->
[version_badge]: https://img.shields.io/github/v/release/almostreliable/almostunified?include_prereleases&filter=v1.21.1-*&style=for-the-badge&logo=github&color=%23181717
[total_downloads_cf_badge]: https://img.shields.io/curseforge/dt/633823?style=for-the-badge&logo=curseforge&label=CurseForge&color=%23F16436
[total_downloads_mr_badge]: https://img.shields.io/modrinth/dt/sdaSaQEz?style=for-the-badge&logo=modrinth&label=Modrinth&color=%2300AF5C
[workflow_status_badge]: https://img.shields.io/github/actions/workflow/status/almostreliable/almostunified/build.yml?branch=1.21.1&style=for-the-badge
[license_badge]: https://img.shields.io/badge/License-ARR-ffa200?style=for-the-badge
[discord_badge]: https://img.shields.io/discord/917251858974789693?color=5865f2&label=Discord&logo=discord&style=for-the-badge
[wiki_badge]: https://img.shields.io/badge/Read%20the-Wiki-ba00ff?style=for-the-badge
[maven_common_badge]: https://img.shields.io/maven-metadata/v?color=C71A36&label=Latest%20version&logo=Latest%20version&metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fcom%2Falmostreliable%2Fmods%2Falmostunified-common%2Fmaven-metadata.xml&style=flat-square
[maven_fabric_badge]: https://img.shields.io/maven-metadata/v?color=C71A36&label=Latest%20version&logo=Latest%20version&metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fcom%2Falmostreliable%2Fmods%2Falmostunified-fabric%2Fmaven-metadata.xml&style=flat-square
[maven_neoforge_badge]: https://img.shields.io/maven-metadata/v?color=C71A36&label=Latest%20version&logo=Latest%20version&metadataUrl=https%3A%2F%2Fmaven.blamejared.com%2Fcom%2Falmostreliable%2Fmods%2Falmostunified-neoforge%2Fmaven-metadata.xml&style=flat-square

<!-- Badge Links -->
[version_link]: https://github.com/AlmostReliable/almostunified/releases/latest
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/almostunified
[modrinth]: https://modrinth.com/mod/almostunified
[workflow_status_link]: https://github.com/AlmostReliable/almostunified/actions
[discord]: https://discord.com/invite/ThFnwZCyYY
[wiki]: https://github.com/AlmostReliable/almostunified/wiki
[maven_common_link]: https://maven.blamejared.com/com/almostreliable/mods/almostunified-common/
[maven_fabric_link]: https://maven.blamejared.com/com/almostreliable/mods/almostunified-fabric/
[maven_neoforge_link]: https://maven.blamejared.com/com/almostreliable/mods/almostunified-neoforge/

<!-- Links -->
[minecraft]: https://www.minecraft.net/
[releases]: https://github.com/AlmostReliable/almostunified/releases
[neoforge]: https://neoforged.net/
[fabric]: https://fabricmc.net/
[blamejared]: https://maven.blamejared.com
[mo_shark]: https://www.curseforge.com/members/mo_shark
