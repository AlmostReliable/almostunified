# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [0.1.2] - 2022-09-28

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

## [0.0.9] - 2022-09-17

### Added
- `wires/{material}` tag to defaults
  - back up your `unify.json` config and let it regenerate to get the new defaults or add them yourself

### Fixed
- unnecessary handling of many duplicate links
- a typo in the log messages
- spaces in shaped recipe patterns not being handled correctly

## [0.0.8] - 2022-09-16

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

## [0.0.7] - 2022-09-02

### Added
- more materials to defaults
  - back up your `unify.json` config and let it regenerate to get the new defaults or add them yourself

### Fixed
- stone strata detection for mods not following convention

## [0.0.6] - 2022-09-02

### Added
- `group` property to default ignored list
  - this ensures that crafting recipes with specific recipe book categories are correctly unified
  - back up your `duplicates.json` config and let it regenerate to get the new defaults or add them yourself
- `inputItems` and `outputItems` property to top level property scanning
  - allows unification for mods like `FTBIC`

### Fixed
- duplication matching for crafting recipes with a `group` property

## [0.0.5] - 2022-08-25

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

## [0.0.4] - 2022-08-24

### Changed
- all unified recipes are now using the Almost Unified namespace ([#1])
  - previously, only the duplicate recipes were modified to use the namespace
  - this ensures that modified recipes are easily identified and not reported to other mod authors

<!-- Links -->
[#1]: https://github.com/AlmostReliable/almostunified/pull/1

## [0.0.3] - 2022-08-23

### Added
- strict mode for recipe duplication removal

### Fixed
- recipe duplication error spam

## [0.0.2] - 2022-08-23

### Fixed
- multiple duplicates are not removed after unification

## [0.0.1] - 2022-08-21

Initial beta release!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[0.1.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.1.2-beta
[0.1.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.1.1-beta
[0.1.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.1.0-beta
[0.0.9]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.9-beta
[0.0.8]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.8-beta
[0.0.7]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.7-beta
[0.0.6]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.6-beta
[0.0.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.5-beta
[0.0.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.4-beta
[0.0.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.3-beta
[0.0.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.2-beta
[0.0.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.1-beta
