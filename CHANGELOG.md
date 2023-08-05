# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## Unreleased

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
[0.5.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.20.1-0.5.0-beta
