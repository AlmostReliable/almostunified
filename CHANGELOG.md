# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [0.3.6] - 2023-03-10

### Changed
- decent performance improvements ([#35])
 - depending on the amount of recipes, this can improve the load times by around 50-70%

<!-- Links -->
[#35]: https://github.com/AlmostReliable/almostunified/pull/35

## [0.3.5] - 2023-02-08

### Fixed
- runtime not available on logical client

## [0.3.4] - 2023-02-01

### Added
- lookup API for mod developers to integrate Almost Unified into their mod

## [0.3.3] - 2022-12-20

### Added
- German translation
- proper Fabric default mod priorities
- missing default tags for Fabric

### Removed
- Forge styled tags from Fabric defaults

### Changed
- improved tooltip information

## [0.3.2] - 2022-12-05

### WARNING
This update will reset your `duplicates.json` config because of a new option. It is automatically backed up as `duplicates.json.bak` inside the same folder.
Please make sure to apply your custom settings again.

### Added
- config option to sanitize recipe JSONs

### Changed
- improved duplicate checks for recipes with implicit counts of 1

## [0.3.0] - 2022-11-30

### Added
- support for Ad Astra! ([#24])

### Changed
- REI on Forge now uses a native plugin instead of the compat layer ([#26])

### Fixed
- automatic item hiding with REI ([#26])
  - this bumps the minimum REI version to 9.1.574
- Immersive Engineering Arc Furnace Slag not being unified ([#25])
- some Mekanism recipes not being completely unified

<!-- Links -->
[#24]: https://github.com/AlmostReliable/almostunified/pull/24
[#25]: https://github.com/AlmostReliable/almostunified/issues/25
[#26]: https://github.com/AlmostReliable/almostunified/pull/26

## [0.2.6] - 2022-11-21

### Removed
- stone strata exclusions for recipe ingredients introduced in [#22]
  - it introduced some unwanted edge cases
  - this won't break any previous recipes

<!-- Links -->
[#22]: https://github.com/AlmostReliable/almostunified/issues/22

## [0.2.5] - 2022-11-21

### Added
- a way to obtain the `unify.json` config and the material list from the KubeJS binding

### Fixed
- hiding of tags that only consisted of items with the same namespace ([#21])
- stone strata detection not being applied to ingredients ([#22])
- inconsistency in recipe duplication removal and dumps ([#23])

<!-- Links -->
[#21]: https://github.com/AlmostReliable/almostunified/issues/21
[#22]: https://github.com/AlmostReliable/almostunified/issues/22
[#23]: https://github.com/AlmostReliable/almostunified/issues/23

## [0.2.3] - 2022-11-07

### Info
This update brings back compatibility for JEI 9 and therefor fixes incompatibility with REI on Minecraft Forge.

### Added
- portuguese translation ([#18])

### Changed
- improved the placement for the recipe indicator icon in JEI

### Fixed
- misaligned tooltips in JEI

<!-- Links -->
[#18]: https://github.com/AlmostReliable/almostunified/pull/18

## [0.2.2] - 2022-11-01

### Added
- config option to completely ignore items from unification and hiding

### Changed
- improved stone strata detection by making use of `forge:ores_in_ground` tag
  - this also allows pack devs to fix stone stratas for mods that don't support it yet

## [0.2.1] - 2022-10-28

### Added
- config backup system
  - when your config has invalid entries, it will be backed up and a new config will be generated
  - the new config will try to apply as many of the old settings as possible
  - new backups will overwrite old backups
- proper ingredient hiding for REI
  - hidden ingredients will no longer show up in recipes
  - this bumps the minimum REI version to 9.1.558

### Changed
- tag priority overrides now use `ResourceLocation`s internally to automatically validate config entries

## [0.2.0] - 2022-10-19

### Added
- priority overrides
  - allows to define a priority mod for a specific tag
  - the new option is automatically added to the `unify.json` config on the next run and is empty by default

### Fixed
- recipe indicator tooltip on lower resolutions exceeding the screen

## [0.1.2] - 2022-10-17

### Info
This is the initial port for 1.19.2 and KubeJS v6. Older versions are not supported anymore.

### Added
- Amethyst Imbuement recipe compat ([#13])

<!-- Links -->
[#13]: https://github.com/AlmostReliable/almostunified/issues/13

## [0.1.1] - 2022-09-27

### Added
- server only mode ([#9], [#11])
  - on startup, there will be a `startup.json` config to activate it

<!-- Links -->
[#9]: https://github.com/AlmostReliable/almostunified/issues/9
[#11]: https://github.com/AlmostReliable/almostunified/pull/11

## [0.1.0] - 2022-09-24

### WARNING
This release removes the modification of recipe IDs! If you modify any recipes which were unified already and make use of the recipe ID that uses `almostunified` as the namespace, make sure to fix it.

### Changed
- recipe modifications are now visible in JEI/REI by a little icon ([#8])
  - this was previously only visible via the recipe ID (now removed)
  - ensures that modified recipes which cause issues are not reported to the original authors
- recipe modifications now have less priority
  - allows to cover more mods that directly inject recipes into the recipe manager
  - still runs before the `recipes` event of KubeJS

### Removed
- recipe ID modifications ([#4], [#8])

### Fixed
- guide books showing errors because of modified recipe IDs ([#4], [#8])

<!-- Links -->
[#4]: https://github.com/AlmostReliable/almostunified/issues/4
[#8]: https://github.com/AlmostReliable/almostunified/pull/8

## [0.0.8] - 2022-09-17

### Added
- `wires/{material}` tag to defaults
  - back up your `unify.json` config and let it regenerate to get the new defaults or add them yourself

### Fixed
- unnecessary handling of many duplicate links
- a typo in the log messages
- spaces in shaped recipe patterns not being handled correctly

## [0.0.7] - 2022-09-16

### Added
- more materials and `storage_blocks/raw_{material}` tag to defaults
  - back up your `unify.json` config and let it regenerate to get the new defaults or add them yourself
- `fabric:conditions` to default duplicate ignore list

### Changed
- ignore lists in `unify.json` and `duplicates.json` now support regular expressions
- default configs are now more platform specific

### Fixed
- a compat issue on Fabric when REI is present
- items being hidden when they are the only entry in a tag

## [0.0.6] - 2022-09-02

### Added
- more materials to defaults
  - back up your `unify.json` config and let it regenerate to get the new defaults or add them yourself

### Fixed
- stone strata detection for mods not following convention

## [0.0.5] - 2022-09-02

### Added
- `group` property to default ignored list
  - this ensures that crafting recipes with specific recipe book categories are correctly unified
  - back up your `duplicates.json` config and let it regenerate to get the new defaults or add them yourself
- `inputItems` and `outputItems` property to top level property scanning
  - allows unification for mods like `FTBIC`

### Fixed
- duplication matching for crafting recipes with a `group` property

## [0.0.4] - 2022-08-25

### Added
- Cucumber `shaped_tag` recipe type to default ignored types
  - if you are using Mystical Agriculture or other mods that require Cucumber, make the config change described [here][cucumber-shapedtag]

### Fixed
- shaped crafting recipe pattern matching ([#2])
  - previously shaped recipes were checked for exact equality after the unification
  - this caused issues with recipes using different definition keys for their patterns
  - this change requires a config adjustment described [here][pattern-matching]

<!-- Links -->
[#2]: https://github.com/AlmostReliable/almostunified/pull/2
[cucumber-shapedtag]: https://github.com/AlmostReliable/almostunified/wiki/Mod-Support#mystial-agriculture-cucumber
[pattern-matching]: https://github.com/AlmostReliable/almostunified/wiki/FAQ#why-are-shaped-crafting-recipes-not-unified

## [0.0.3] - 2022-08-24

### Changed
- all unified recipes are now using the Almost Unified namespace ([#1])
  - previously, only the duplicate recipes were modified to use the namespace
  - this ensures that modified recipes are easily identified and not reported to other mod authors

<!-- Links -->
[#1]: https://github.com/AlmostReliable/almostunified/pull/1

## [0.0.2] - 2022-08-23

### Added
- strict mode for recipe duplication removal

### Fixed
- recipe duplication error spam

## [0.0.1] - 2022-08-23

Initial 1.19 release!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[0.3.6]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.2-0.3.6-beta
[0.3.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.2-0.3.5-beta
[0.3.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.3.4-beta
[0.3.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.3.3-beta
[0.3.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.3.2-beta
[0.3.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.3.0-beta
[0.2.6]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.6-beta
[0.2.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.5-beta
[0.2.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.4-beta
[0.2.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.3-beta
[0.2.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.2-beta
[0.2.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.1-beta
[0.2.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.2.0-beta
[0.1.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.1.2-beta
[0.1.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.1.1-beta
[0.1.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.1.0-beta
[0.0.8]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.8-beta
[0.0.7]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.7-beta
[0.0.6]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.6-beta
[0.0.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.5-beta
[0.0.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.4-beta
[0.0.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.3-beta
[0.0.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.2-beta
[0.0.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19-0.0.1-beta
