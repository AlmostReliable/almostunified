# Changelog

All notable changes to this project will be documented in this file.

## Unreleased
- /

## [1.2.2] - 2024-10-23

- fixed crash on empty recipe JSONs

## [1.2.1] - 2024-10-22

- added logging for cases where items are assigned to multiple unification tags
- added logging for cases where the recipe type can't be found
- added skipping logic for recipes with invalid recipe types
- added Turkish translation ([#102](https://github.com/AlmostReliable/almostunified/pull/102))
- fixed crash when runtime isn't loaded ([#101](https://github.com/AlmostReliable/almostunified/issues/101))
- fixed newly created custom tags not being considered for unification
- fixed runtime not being available when items are assigned to multiple unification tags

## [1.2.0] - 2024-10-06

- added support for custom ingredient types
- added support for NeoForge compound ingredients
- added API endpoint for registering custom ingredient unifiers
- added unification helper methods to convert tags to items
- fixed recipe viewer integration endpoints for Fabric
- fixed unnecessary memory usage for debug handler
- fixed Mekanism recipe unifier using wrong recipe keys
- fixed EnderIO Sag Mill recipe output unification causing serialization failures

## [1.1.0] - 2024-09-27

- added `end` stone variant to config defaults
- added debug option to toggle logging invalid tag warnings, false by default
- added logging for potentially broken recipes caused by unification
- added support for Extended Industrialization ([#92](https://github.com/AlmostReliable/almostunified/pull/92))
- fixed unification for EnderIO outputs
- removed EnderIO unifier since it's fully supported by the generic unifier

## [1.0.0] - 2024-08-22

Initial 1.21.1 port.

<!-- Versions -->
[1.2.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.2
[1.2.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.1
[1.2.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.0
[1.1.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.1.0
[1.0.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.0.0
