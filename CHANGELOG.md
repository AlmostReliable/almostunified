# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

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
[0.0.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.5-beta
[0.0.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.4-beta
[0.0.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.3-beta
[0.0.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.2-beta
[0.0.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.18-0.0.1-beta
