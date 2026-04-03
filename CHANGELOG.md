# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

- added support for Create Sequenced Assembly ([ATM10#3990](https://github.com/AllTheMods/ATM-10/issues/3990))

## [1.3.0] - 2025-08-04

- added logic to ignore recipes with non-matching recipe conditions for NeoForge
    - this should get rid of some unfixable errors and warnings in your Almost Unified log
- improved config ignoring logic performance drastically
- improved general load performance
- improved Portuguese translation ([#122](https://github.com/AlmostReliable/almostunified/pull/122))
- removed built-in recipe unifier for Immersive Engineering
    - it's now part of the Immersive Engineering add-on [here](https://github.com/AlmostReliable/almostunified-ie)

## [1.2.7] - 2025-07-13

- added Hungarian translation ([#121](https://github.com/AlmostReliable/almostunified/pull/121))
- fixed duplicate checking for recipes with different recipe key counts
- removed built-in recipe unifiers for Modern Industrialization and Extended Industrialization

## [1.2.6] - 2025-05-06

- fixed rare case of missing recipes without a recipe type namespace

## [1.2.5] - 2025-04-29

- fixed spam of errors because the output resolver didn't use the correct inner key of result item stacks ([#116](https://github.com/AlmostReliable/almostunified/issues/116))

## [1.2.4] - 2025-04-27

- added Mexican Spanish translation ([#112](https://github.com/AlmostReliable/almostunified/pull/112))
- drastically improved startup performance ([Shadows-of-Fire](https://github.com/Shadows-of-Fire)@[#115](https://github.com/AlmostReliable/almostunified/pull/115))

## [1.2.3] - 2024-12-22

- fixed `c:ores_in_ground` block tag not being considered when no respective item tag is present
- fixed stone variant defaults not using the correct registry names
- improved performance for unification ([mezz](https://github.com/mezz)@[#97](https://github.com/AlmostReliable/almostunified/pull/97))

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
[1.3.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.3.0
[1.2.7]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.7
[1.2.6]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.6
[1.2.5]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.5
[1.2.4]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.4
[1.2.3]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.3
[1.2.2]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.2
[1.2.1]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.1
[1.2.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.2.0
[1.1.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.1.0
[1.0.0]: https://github.com/AlmostReliable/almostunified/releases/tag/v1.21.1-1.0.0
