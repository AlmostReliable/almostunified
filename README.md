<div align="center">
<h1>Almost Unified</h1>

A [Minecraft] mod which aims to unify materials.

[![Version][version_badge]][version_link]
[![Total Downloads][total_downloads_badge]][curseforge]
[![Workflow Status][workflow_status_badge]][workflow_status_link]
[![License][license_badge]][license]

[Discord] | [CurseForge]

</div>

## **🔧 Manually Installation**
1. Download the latest **mod jar** from [CurseForge] or the latest [releases].
2. Install Minecraft [Forge] or [Fabric].
3. Drop the mod **jar** into your mods folder.

## **🖥️ Setup dev environment**
- Clone the repository
- Import into Intellij (VSCode and Eclipse are not tested, we recommend using Intellij)
- Run 
    - `gradle -> common -> Tasks -> genSources`
    - `gradle -> fabric -> Tasks -> genSources`
    - `gradle -> forge -> Tasks -> forgegradle runs -> genIntellijRuns`
- Have fun!

The `common` module uses `fabric-loom`, this makes it possible to use parchment. Please do not use fabric related features in the `common` module.

## **💚 Credits**
This project is using the [MultiLoader Template] from [Jared].

## **🎓 License**
This project is licensed under the [GNU Lesser General Public License v3.0][license].

<!-- Badges -->
[version_badge]: https://img.shields.io/github/v/release/AlmostReliable/almostunified?style=flat-square
[version_link]: https://github.com/AlmostReliable/almostunified/releases/latest
[total_downloads_badge]: http://cf.way2muchnoise.eu/full_633823.svg?badge_style=flat
[workflow_status_badge]: https://img.shields.io/github/workflow/status/AlmostReliable/almostunified/CI?style=flat-square
[workflow_status_link]: https://github.com/AlmostReliable/almostunified/actions
[license_badge]: https://img.shields.io/github/license/AlmostReliable/almostunified?style=flat-square

<!-- Links -->
[minecraft]: https://www.minecraft.net/
[discord]: https://discord.com/invite/ThFnwZCyYY
[releases]: https://github.com/AlmostReliable/almostunified/releases
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/almost-unified
[wiki]: https://github.com/AlmostReliable/almostunified/wiki
[forge]: http://files.minecraftforge.net/
[fabric]: https://fabricmc.net/
[changelog]: CHANGELOG.md
[license]: LICENSE
[Jared]: https://github.com/jaredlll08
[MultiLoader Template]: https://github.com/jaredlll08/MultiLoader-Template
