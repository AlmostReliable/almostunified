# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [0.9.3] - 2024-04-18

### Added
- added support for Alloy Forgery

## [0.9.2] - 2024-03-17

### Fixed
- fixed automatic hiding of stone strata variants (bug introduced in 0.9.0)
- fixed a bug where stone stratas were not correctly identified (Fabric only)

## [0.9.1] - 2024-03-17

### Added
- added support for Blood Magic

### Fixed
- fixed `show_notification` recipe key not being ignored by default

## [0.9.0] - 2024-03-14

### Added
- added support for Applied Energistics
- added new debug file for potentially uncovered recipe types

### Changed
- reworked and refactored hiding list creation logic

## [0.8.1] - 2024-03-01

### Added
- added support for TerraFirmaCraft ([#69])

### Fixed
- fixed duplicate check for recipes with custom categories ([#66])
- fixed broken transfer handlers for unified items when using EMI ([#67])
  - this now uses a less restrictive hiding approach for EMI
  - unified items are visible in tag cycling until we find a proper solution

<!-- Links -->
[#66]: https://github.com/AlmostReliable/almostunified/issues/66
[#67]: https://github.com/AlmostReliable/almostunified/issues/67
[#69]: https://github.com/AlmostReliable/almostunified/pull/69

## [0.8.0] - 2024-02-21

## Added
- added integration for EMI
  - same features as for JEI/REI including hiding stacks from tags used in recipes
  - to disable it, you can use the flag for JEI/REI in the config
  - requires at least EMI version 1.1.2 

### Fixed
- fixed Integrated Dynamics unifier not properly targetting outputs

## [0.7.2] - 2023-11-21

## Added
- added support for Integrated Dynamics

## [0.7.1] - 2023-11-17

### Fixed
- fixed GregTech Modern compat to properly unify output ingredients

## [0.7.0] - 2023-09-23

### Warning
This update features a new config option inside the `unify.json`.<br>
Since it is a top-level option, this won't reset your config and all other options should
be preserved. However, an automatic backup will be created in case something goes wrong.

### Added
- added the ability to add items to existing or new tags
    - this allows you to alter tags without the requirement of using other tools such as KubeJS, CraftTweaker or
      datapacks
    - you can read more about it in the [wiki][custom-tags]
- added unification support for the Fabric NBT ingredient
- added the `AlmostUnified` KubeJS binding, info about that can be found in the [wiki][kubejs-binding]
- added support for EnderIO
- added support for GregTech Modern

### Removed
- removed the unnecessary mixin plugin

<!-- Links -->
[custom-tags]: https://github.com/AlmostReliable/almostunified/wiki/Unification-Config#custom-tags
[kubejs-binding]: https://github.com/AlmostReliable/almostunified/wiki/KubeJS

## [0.6.0] - 2023-08-10

### Warning
This update features new config options inside the `unify.json`.<br>
Since they are top-level options, this won't reset your config and all other options should
be preserved. However, an automatic backup will be created in case something goes wrong.

### Added
- unify tag validation
- tag inheritance ([#57])
    - a new milestone feature allowing dominant tags to inherit item and block tags of unified items
    - you can read more about it in the [wiki][tag-inheritance]
- support for Cyclic ([#54])

### Changed
- slightly improved overall performance
- publishing will also support NeoForge now

### Fixed
- a serious load order issue that caused some features not to work on the initial unification process
    - the bug was undiscovered for a long time since most packs force-reloaded after entering the world
- tag ownership log messages showing the wrong owner tag
- wrong Fabric conditions key ([#55])

<!-- Links -->
[#54]: https://github.com/AlmostReliable/almostunified/issues/54
[#55]: https://github.com/AlmostReliable/almostunified/pull/55
[#57]: https://github.com/AlmostReliable/almostunified/pull/57
[tag-inheritance]: https://github.com/AlmostReliable/almostunified/wiki/Unification-Config#tag-inheritance

## [0.5.0] - 2023-06-14

Initial 1.20.1 release!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[0.9.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.9.3
[0.9.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.9.2
[0.9.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.9.1
[0.9.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.9.0
[0.8.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.8.1
[0.8.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.8.0-beta
[0.7.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.7.2-beta
[0.7.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.7.1-beta
[0.7.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.7.0-beta
[0.6.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.6.0-beta
[0.5.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.5.0-beta
