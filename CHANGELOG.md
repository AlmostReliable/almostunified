# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [0.4.2] - 2023-04-22

### Fixed
- tag ownerships not being in sync on some recipes
- automatic item hiding not working with all ownership tags
- Immersive Engineering catalysts not being unified
- Immersive Engineering secondary outputs not being unified

## [0.4.1] - 2023-04-19

### Fixed
- crash due to unloaded ownership tags on dedicated servers

## [0.4.0] - 2023-04-18

### WARNING
This update features a new config option inside the `unify.json` called `tagOwnerships`.<br>
Since it's a top-level option, this won't reset your config and all other options should be preserved. However,
an automatic backup will be created in case something goes wrong.

### Added
- tag ownerships
  - a new milestone feature allowing tags to be converted to other tags
  - this allows unifying inconsistent tags like `forge:coals` and `forge:gems/coal`
  - you can read more about it in the [wiki][tag-ownerships]

### Changed
- improved stone strata lookup speed

### Fixed
- some Mekanism recipes not being unified
- log spam on multiple preferred tags
- JEI indicator not showing anymore with new JEI versions

<!-- Links -->
[tag-ownerships]: https://github.com/AlmostReliable/almostunified/wiki/Unification-Config#tag-ownerships

## [0.3.8] - 2023-04-06

### Added
- JEI support since it was updated to 1.19.4

### Fixed
- stone strata fallback variant for clean stone

## [0.3.7] - 2023-04-02

Initial 1.19.4 release!

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[0.4.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.4-0.4.2-beta
[0.4.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.4-0.4.1-beta
[0.4.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.4-0.4.0-beta
[0.3.8]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.4-0.3.8-beta
[0.3.7]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.19.4-0.3.7-beta
