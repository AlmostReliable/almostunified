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
[![License][license_badge]][license]

[Discord] | [Wiki] | [CurseForge] | [Modrinth]

</div>

## **📖 Wiki**
For an in-depth explanation of the mod, its functionality, config descriptions, FAQs and more, check out the [wiki].

## **🔧 Manual Installation**
1. Download the latest **mod jar** from the [releases], from [CurseForge] or [Modrinth].
2. Install Minecraft [Forge] or [Fabric].
3. Drop the **jar file** into your mods folder.

## **🖥️ Dev Environment Setup**
1. Clone the repository
2. Import into IntelliJ (VSCode and Eclipse are not tested)
3. Run
  - `gradle -> common -> Tasks -> fabric -> genSources`
  - `gradle -> fabric -> Tasks -> fabric -> genSources`
  - `gradle -> forge -> Tasks -> loom -> genSources`
  - `gradle -> Tasks -> buildconfig -> generateBuildConfig`
4. Restart the IDE

The `common` module uses [fabric-loom]. This allows to use [ParchmentMC][parchment].<br>
Do not use Fabric related features inside the `common` module!

## **💚 Credits**
This project is using the [MultiLoader Template] by [Jared].<br>
The logo was made by [mo_shark].

## **🎓 License**
This project is licensed under the [GNU Lesser General Public License v3.0][license].

<!-- Badges -->
[version_badge]: https://img.shields.io/github/v/release/AlmostReliable/almostunified?include_prereleases&style=flat-square
[version_link]: https://github.com/AlmostReliable/almostunified/releases/latest
[total_downloads_cf_badge]: http://cf.way2muchnoise.eu/full_633823.svg?badge_style=flat
[total_downloads_mr_badge]: https://img.shields.io/modrinth/dt/sdaSaQEz?color=5da545&label=Modrinth&style=flat-square
[workflow_status_badge]: https://img.shields.io/github/actions/workflow/status/AlmostReliable/almostunified/build.yml?branch=1.19&style=flat-square
[workflow_status_link]: https://github.com/AlmostReliable/almostunified/actions
[license_badge]: https://img.shields.io/github/license/AlmostReliable/almostunified?style=flat-square

<!-- Links -->
[minecraft]: https://www.minecraft.net/
[discord]: https://discord.com/invite/ThFnwZCyYY
[wiki]: https://github.com/AlmostReliable/almostunified/wiki
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/almost-unified
[modrinth]: https://modrinth.com/mod/almost-unified
[releases]: https://github.com/AlmostReliable/almostunified/releases
[forge]: http://files.minecraftforge.net/
[fabric]: https://fabricmc.net/
[fabric-loom]: https://github.com/FabricMC/fabric-loom
[parchment]: https://parchmentmc.org/
[multiLoader template]: https://github.com/jaredlll08/MultiLoader-Template
[jared]: https://github.com/jaredlll08
[mo_shark]: https://www.curseforge.com/members/mo_shark
[license]: LICENSE
