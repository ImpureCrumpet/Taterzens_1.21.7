# Upgrade to Minecraft 1.21.7 (Fabric)

This project is updated to run on Minecraft 1.21.7 with Fabric.

- Minecraft: 1.21.7
- Mappings: Mojang official (via Loom)
- Fabric Loader: >= 0.16.0
- Fabric API: 0.129.0+1.21.7

Dependency notes
- Polymer, SGUI, and Server Translations API are kept on the latest resolvable versions at time of update (their 1.21.7 artifacts were not available on their maven). If 1.21.7 builds become available, bump their versions accordingly.

Build config changes
- `gradle.properties`: set `minecraft_version=1.21.7`, `fabric_version=0.129.0+1.21.7`.
- `build.gradle`: use `officialMojangMappings()` in the `loom.layered` block.
- `src/main/resources/fabric.mod.json`: raise `fabricloader` to `>=0.16.0` and `fabric` to `>=0.129.0`.

References
- Yarn index for 1.21.7: https://maven.fabricmc.net/docs/yarn-1.21.7+build.1/index-files/index-1.html
- Previous release (1.21.6): https://github.com/samolego/Taterzens/releases/tag/1.16.2%2B1.21.6
