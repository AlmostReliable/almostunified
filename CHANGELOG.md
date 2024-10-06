# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## Unreleased

- added support for custom ingredient types
- added support for NeoForge compound ingredients
- added API endpoint for registering custom ingredient unifiers
- added unification helper methods to convert tags to items
- fixed recipe viewer integration endpoints for Fabric
- fixed unnecessary memory usage for debug handler
- fixed Mekanism recipe unifier using wrong recipe keys

## [1.1.0] - 2024-09-27

- added `end` stone variant to config defaults
- added debug option to toggle logging invalid tag warnings, false by default
- added logging for potentially broken recipes caused by unification
- added support for Extended Industrialization ([#92](https://github.com/AlmostReliable/almostunified/pull/92))
- fixed unification for EnderIO outputs
- removed EnderIO unifier since it's fully supported by the generic unifier

## [1.0.0] - 2024-08-22

Initial 1.21.1 port.

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[1.1.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.1.0
[1.0.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.0.0
