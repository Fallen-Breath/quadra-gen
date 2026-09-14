# Quadra Gen Features

English | [中文](features.zh.md)

## 1. Project Overview

Quadra Gen is a Fabric world-generation mod that provides four independently configurable terrain environments within a single dimension.

Core model:

- The Overworld or the Nether is divided into four quadrants along the X/Z axes, with the origin fixed at `(0, 0)`;
- Each quadrant picks one terrain type: the vanilla natural terrain of the current dimension, or flat terrain made of a fixed biome and fixed block layers;
- Either terrain type can have its actual content generation turned off, which yields empty terrain that keeps the terrain semantics.

The four quadrants still belong to the same vanilla dimension. They share time, weather, difficulty, game rules, the world border, the player list, and the mob cap; redstone, fluids, block events, POI, raids, entity AI, and portals are not isolated per quadrant either. Quadra Gen changes only how new chunks are generated and the query results directly related to the terrain type.

Installation requirements:

- Multiplayer: the server must have the mod, and the client installation is optional. Clients without it can join directly, and installing it improves client-side details such as horizon height;
- Singleplayer: the same functionality runs through the integrated server, but it is disabled by default and must be explicitly enabled in the configuration.

## 2. Scope

The Overworld and the Nether are supported, and each can be enabled or disabled independently. The End, datapack-defined custom dimensions, and dimensions added by other mods are not affected.

A managed dimension must use the vanilla natural terrain of the corresponding version as its underlying world type: the Overworld uses the vanilla Overworld Noise terrain, and the Nether uses the vanilla Nether Noise terrain. Generators whose underlying type is not vanilla Noise do not enable this feature; compatibility with third-party generators that extend the vanilla Noise type is not guaranteed.

A dimension is managed only when all of the following hold. If any condition is unmet, the mod does not manage that dimension at all:

1. The master switch is enabled;
2. In singleplayer, the singleplayer switch is enabled;
3. The independent switch of that dimension is enabled;
4. That dimension uses a supported vanilla natural terrain.

The configuration is read only once at mod startup; hot reload is not supported. See the [configuration reference](config.md) for its fields and defaults.

A save whose underlying world type is the vanilla Flat one is neither brought under management automatically nor converted automatically. See section 13.

## 3. Quadrant Division

The quadrant origin is fixed at `(0, 0)` and cannot be moved or rotated. Coordinates where X or Z equals zero belong to the positive side:

| Config key | Coordinate range |
| --- | --- |
| `x_positive_z_positive` | `x >= 0, z >= 0` |
| `x_negative_z_positive` | `x < 0, z >= 0` |
| `x_negative_z_negative` | `x < 0, z < 0` |
| `x_positive_z_negative` | `x >= 0, z < 0` |

The two axes lie exactly on chunk boundaries, so each chunk belongs entirely to one quadrant, and two base terrain types are never spliced within a single chunk.

The Overworld and the Nether each have a complete four-quadrant configuration. The same coordinates can use different biomes, block layers, or terrain modes in the two dimensions.

## 4. The Four Terrain Modes

A quadrant is defined by two properties:

- `generator`: `noise` for the vanilla natural terrain, `flat` for flat terrain;
- `clear_generated_content`: whether to prevent that terrain from actually generating content.

The two properties combine into four modes:

| Mode | Biome | Theoretical terrain | Structure data | Actual content of new chunks |
| --- | --- | --- | --- | --- |
| Noise | Vanilla natural biome of the current dimension | Vanilla natural terrain | Vanilla structures | Complete vanilla terrain, structures, decoration, and worldgen mobs |
| Noise clear | Vanilla natural biome of the current dimension | Vanilla natural terrain | Vanilla structure data kept | No terrain, structure content, decoration, or worldgen mobs |
| Flat | Configured fixed biome | Configured fixed block layers | No structures belonging to this quadrant | Only the configured block layers |
| Flat clear | Configured fixed biome | Configured fixed block layers | No structures belonging to this quadrant | No block layers or other world-generation content |

In this section, "theoretical terrain" means the height and block column results that the world generator gives for specified coordinates. It describes the generation basis of the corresponding terrain type and is unrelated to the blocks that actually exist in the chunk. Clear prevents content from being placed, but does not change the theoretical results of that terrain type.

### 4.1 Noise

Noise quadrants keep the vanilla natural generation of the current dimension: biomes, terrain, caves, aquifers, surfaces, structures, decoration, and worldgen mobs are all decided by vanilla rules.

The Overworld and the Nether each use their own natural-generation rules. The Nether does not gain the Overworld's height, sea level, sky, or mob behaviors just because it uses the same four-quadrant model.

### 4.2 Noise clear

Noise clear is empty terrain that keeps natural world semantics: new chunks still store the vanilla natural biome and keep the theoretical heights, theoretical block columns, and structure data of the vanilla natural terrain, but generate no natural terrain blocks, structure content, decoration, or worldgen mobs.

The kept structure data continues to take part in vanilla mechanics such as structure-range mob spawning, maps, and explored-structure counts, regardless of whether the chunk is currently empty.

### 4.3 Flat

A Flat quadrant consists of a fixed biome and an ordered list of block layers.

- The first layer starts at the minimum buildable height of the current dimension; no separate starting height is configurable. When the same configuration is used for a different Minecraft version or a different dimension, the actual starting Y changes with the world height of that version and dimension.
- Each layer has a block ID and a positive integer thickness, and the total thickness must not exceed the buildable height of the current dimension.
- The layer configuration expresses the default state of blocks and cannot specify block state properties such as facing or waterlogging.
- Layer placement follows the rules of the vanilla flat generator of the corresponding version, including layers that vanilla places in a later generation step (such as layers of blocks that are not complete solid cubes, for example glass). Those layers are still placed, and height queries do not lose them because of placement order.
- Flat quadrants do not generate the structures, lakes, or ordinary biome decoration of vanilla flat worlds.
- An empty layer list is a valid configuration meaning a completely empty Flat, and it does not generate the initial platform that comes with the vanilla Void preset.
- Height and block column queries are based on the complete configured layers, independent of the chunk's generation progress.

### 4.4 Flat clear

Flat clear stores the configured biome and the complete theoretical layers but places none of them. It suits cases that need a fixed biome and a fixed theoretical height while requiring new chunks to be completely empty.

`clear_generated_content` only controls whether generated content is placed. It does not change the biome, the theoretical height, or the sea level semantics, and it does not turn Flat clear into Noise clear.

## 5. Boundaries of Clear

Clear suppresses content during generation instead of generating in full and then deleting. Emptying a chunk therefore does not come with the creation and removal of large numbers of blocks, block entities, fluid ticks, or entities.

Clear has no retroactive cleaning ability. The following content is not deleted or stopped because the quadrant it is in has clear enabled:

- Blocks and entities in completed chunks;
- Content placed by players or commands;
- Entities produced at runtime, including natural mob spawning, spawners, and breeding;
- Redstone, fluids, random ticks, scheduled ticks, and block events;
- Entities and runtime changes crossing in from adjacent areas.

Changing the configuration also does not reshape completed chunks. Configuration changes only affect chunks that have not yet gone through the corresponding generation processes, and the world-level theoretical queries answered according to the current configuration.

## 6. Biome and Terrain Queries

### 6.1 Chunk Biomes

A chunk stores the real biome its quadrant should have: the two Noise modes store the natural biome, and the two Flat modes store the configured biome. After players enter a generated chunk, vanilla systems that read chunk biomes, such as weather and natural mob spawning, therefore see results consistent with the quadrant.

### 6.2 Biome Locating

For coordinates that have not been generated yet, biome locating answers according to the quadrant instead of treating the whole dimension as the underlying Noise world. The search covers both natural biomes and biomes that only exist in Flat configurations, while keeping the vanilla search order and sampling of the corresponding Minecraft version.

| Minecraft version | Behavior |
| --- | --- |
| 1.14.4–1.15.2 | No extensible server-side biome-locating entry point, and Quadra Gen provides no extra command |
| 1.16.5–1.18.2 | Search Noise and Flat biomes with the 2D nearest-first rules of the corresponding version |
| 1.19.4 and above | Find Noise and Flat biomes with the 3D search rules of the corresponding version |

At each sampling point, the two Flat modes use the configured biome and the two Noise modes use the natural biome. Clear does not affect biome-locating results.

### 6.3 Height and Block Column Queries

- Noise and Noise clear return the theoretical results of the natural terrain;
- Flat and Flat clear return the theoretical results of the configured layers;
- Clear does not turn the theoretical results into all air;
- The theoretical height of an empty Flat is the minimum buildable height of the corresponding version.

The theoretical terrain can differ from the chunk's actual heightmap: the former is the generation basis of the corresponding terrain type, while the latter describes the blocks that really exist in the world.

## 7. Structures and Structure Locating

What structure data is kept:

- Noise: vanilla structure content is kept in full and generated;
- Noise clear: the positions, ranges, and associated data of structures are kept, but no structure blocks, loot containers, spawners, or related worldgen mobs are placed;
- Flat and Flat clear: no structures of their own are produced, and coordinates in these quadrants are not treated as valid structure sources merely because the underlying world is Noise.

Structure locating follows the structure data actually kept:

- Noise structures and the structures kept in Noise clear can be located;
- A Flat quadrant does not report a structure that was not actually built there;
- Eyes of Ender, exploration maps, dolphin treasure hunting, and villager map trades report the same results as the structure-locating commands.

Noise structures near the axes may partially extend into an adjacent quadrant. Adjacent chunks keep the associated data needed to recognize such a structure, so structure-range mob spawning, maps, and explored-structure counts are not abruptly cut off at the axes. This cross-boundary association is not equivalent to Flat quadrants having structure generation enabled.

## 8. Quadrant Boundaries

Base terrain has a strict boundary: Flat or clear chunks do not generate Noise base terrain, surfaces, or caves, and ordinary Noise chunks (`generator` set to `noise` with clear disabled) do not treat Flat or clear chunks as cave sources. Caves do not continuously carve into ordinary Noise areas from distant incompatible quadrants.

Structures and decoration follow the vanilla neighbor-generation range without per-block hard clipping. Limited cross-boundary content such as tree edges or structure corners may appear near the axes; its range is decided by the vanilla generation behavior of the corresponding version, and it does not form a channel that continuously pollutes other quadrants from a distance.

In Minecraft 26.2 and above, vanilla surface generation consults the actual biomes of the target chunk and its surrounding chunks. Noise chunks next to the axes may therefore be locally affected by the biomes of an adjacent Flat quadrant (only when those biomes also belong to the current dimension's natural biome set). The other target versions do not have this behavior.

The runtime world state has no quadrant walls: entities, player-placed blocks, fluids, explosions, redstone, POI, raids, and portals can all cross the axes.

## 9. Sea Level Semantics

### 9.1 During World Generation

The two Noise modes always use the vanilla natural sea level of the current dimension, keeping natural terrain, aquifers, surfaces, and structure calculations consistent. The two Flat modes do not enter these Noise generation processes; they only handle the configured layers by the vanilla flat rules of the corresponding version and do not fill water based on the sea level. Clear does not change the sea level semantics.

### 9.2 At Runtime

The two terrain types have their own sea levels: a Flat quadrant uses the sea level of the vanilla Flat world of the corresponding version, and a Noise quadrant uses the natural sea level of the dimension. Which one a check gets depends on whether it has a position:

- A check with explicit X/Z coordinates uses the sea level of the quadrant at that position, so a Flat quadrant behaves like a vanilla Flat world there, and a Noise quadrant like a vanilla natural world;
- A check without coordinates keeps the dimension-level natural value, and never infers the quadrant from the nearest player, the current task, or other implicit state.

The checks that use the sea level as a threshold include precipitation and weather, snow and freezing, and underwater and aquatic rules. `clear_generated_content` does not change the sea level semantics.

| Minecraft version | Runtime behavior |
| --- | --- |
| 1.14.4–1.21.1 | The two terrain types are not distinguished; every check keeps the dimension-level natural value |
| 1.21.3 and above | Checks with explicit coordinates follow the quadrant |
| 1.21.11 and above | On top of the previous row, the Nautilus spawn-height check also follows the quadrant |

In the target versions of 1.21.3 and above, the vanilla Flat sea level is `-63`; the Overworld and Nether Noise values come from their respective natural generation settings, usually `63` and `32`.

### 9.3 Client Visibility

A client receives one dimension-level sea level, so the client-side checks that read it keep using the natural value, even inside a Flat quadrant. The client-side behavior of the two terrain types is described in section 10.

## 10. Client-side Behavior

### 10.1 Vanilla Clients

A client without the mod receives a single advertised world type for the dimension, so it presents one client-side appearance for the whole dimension: Flat or Noise according to that type, with no switching at the quadrant axes until the next login, respawn, or dimension change.

The advertised world type decides the client-side Flat or Noise appearance, such as the horizon and the dark disc of the sky, and the void fog from 1.16.5. The sea level stays the natural one of the dimension: the save still records the underlying world type of the dimension as the vanilla Noise one, so a vanilla client inside a Flat quadrant keeps the natural sea level for its local precipitation and freezing checks (see 9.3).

When the mod is absent on the server, or the dimension is disabled or unsupported, the advertised world type is the vanilla one, and clients behave as in an ordinary vanilla world.

### 10.2 Advertised World Type

Each dimension advertises a world type to its clients through `advertised_world_type`. `flat` always advertises Flat, `noise` always advertises Noise, and `auto` advertises according to the quadrant the player is in at that moment: the two Flat modes advertise Flat, and the two Noise modes advertise Noise. The value is computed at login, respawn, and dimension change, and stays unchanged afterwards.

This advertisement affects clients only. Generation, mob spawning, and sea-level checks on the server do not depend on it.

### 10.3 Clients with the Mod Installed

Installing the mod on the client enables per-quadrant client-side behavior. The client requests the effective state of the current dimension at login and at respawn, and drops it on disconnect; the state is not re-sent per tick or per chunk.

The synchronized state carries whether the dimension is actually managed, which quadrants are Flat, and the sea level of each Flat quadrant. `clear_generated_content` is not part of it: the two Flat modes share one client-side appearance, and the two Noise modes share the other. Client-side logic then picks the value by the coordinates it queries, instead of following the single advertised world type:

| Client-side appearance | Versions |
| --- | --- |
| Horizon and dark disc of the sky | All supported versions |
| Void fog | 1.16.5 and above |
| Precipitation | 1.21.3 and above |

This per-quadrant behavior applies only to a dimension that is actually managed. A client on a server without the mod, or on a disabled or unsupported dimension, falls back to the behavior described in 10.1.

## 11. Mob Spawning

### 11.1 During World Generation

Noise quadrants keep the mob generation that happens during vanilla chunk generation; Noise clear, Flat, and Flat clear place no mobs while new chunks are generated.

### 11.2 Runtime Natural Spawning

Runtime natural spawning is still managed by vanilla uniformly, and shares one mob cap within the same dimension. Candidate results depend on the biome stored in the chunk, valid structure ranges, actual blocks, fluids, light, and heightmaps:

- Noise: identical to an ordinary vanilla natural world;
- Noise clear: usually no valid spawning positions while completely empty; after players build an environment, mobs can spawn according to the natural biome and structure ranges;
- Flat: uses the configured biome and the surface that was actually placed;
- Flat clear: uses the configured biome, but must still satisfy the vanilla spawning conditions in the real world.

### 11.3 Special Events and Version Differences

Phantoms, patrols, cats, village sieges, and wandering traders keep following the vanilla rules of the corresponding version. Empty layers, `minecraft:the_void`, or clear by themselves do not become extra phantom-disabling conditions; `minecraft:the_void` can still block ordinary biome mob spawning and wandering traders through vanilla biome rules.

| Minecraft version | Differences handled per quadrant |
| --- | --- |
| 1.14.4–1.15.2 | Vanilla flat worlds do not run patrols or village sieges, and slime spawning carries the suppression specific to flat worlds (only one in four checks continues). Quadra Gen keeps these differences in Flat quadrants, and Noise clear still counts as the Noise type |
| 1.16.5 and above | The vanilla Noise and Flat Overworld use the same set of special spawners, so the differences above no longer apply, and whether these events can spawn is decided by the biome and the actual environment |

Existing entities can cross quadrants freely. The mod does not delete, freeze, or reclassify an entity because it enters another terrain mode.

## 12. Initial Spawn Point

Quadra Gen only adjusts the initial spawn selection of a brand-new Overworld. The spawn point of existing worlds is not recalculated, and the Nether does not take part in this feature.

Selection rules:

- If an ordinary Noise quadrant exists, the center of the vanilla spawn search is moved into the nearest ordinary Noise quadrant, and the vanilla flow then looks for a specific safe position;
- To keep the search range from crossing the axes, 1.14.4–1.17.1 reserve 16 chunks at the axes, and 1.18.2 and above reserve 5 chunks;
- A safe non-clear Flat quadrant is currently not used as a fallback. If no ordinary Noise quadrant exists, the vanilla search center is kept.

The mod does not create a spawn platform for an all-empty configuration, and does not log a special all-empty warning. If there is no fluid-free surface with a full top collision shape below the final spawn position, the bonus chest is disabled to avoid trying to generate it at an obviously unsafe position; the rest of the spawn search rules are still decided by the vanilla of the corresponding version.

## 13. Save Files and Existing Worlds

### 13.1 Data Format

Chunks generated by Quadra Gen keep using the vanilla chunk, biome, structure, and entity data formats. The save still records the underlying world type of the corresponding dimension as the vanilla Noise one, and no Quadra Gen-specific generator, biome, or chunk data is required.

Therefore:

- Only new chunks and chunks whose relevant generation processes are unfinished are affected;
- Completed chunks are not actively rewritten by installing, uninstalling, or changing the configuration;
- Complete chunks generated with the mod can be read by the same version of vanilla;
- After removing the mod, existing chunks stay as they are, and new chunks return to the ordinary Noise generation of the current dimension;
- After removing the mod, unfinished chunks may be completed by the vanilla Noise flow, resulting in a partial mix;
- The current configuration still affects biome, theoretical terrain, and sea level queries for ungenerated coordinates, but the historical configuration used when old chunks were generated cannot be recovered.

### 13.2 Upgrading Old Chunks

In Minecraft 1.18.2 and above, when encountering old chunks that vanilla is upgrading or retrogening, the complete vanilla upgrade flow runs first, without re-laying or clearing according to the current quadrant. This preserves the vanilla old-bedrock handling, terrain connection, and boundary updates.

### 13.3 Vanilla Flat Worlds

Old saves whose underlying world type is the vanilla Flat one are not directly supported. To use Quadra Gen while keeping an existing flat design area, stop the server, make a full backup, and convert the underlying world type of the target dimension to the same-version vanilla Noise while offline. The mod itself does not modify the dimension settings recorded in the save, nor does it perform this conversion.

## 14. Supported Versions

The project currently covers the following Minecraft versions:

- 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4;
- 1.20.1, 1.20.2, 1.20.4, 1.20.6;
- 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11;
- 26.1.2, 26.2, 26.3.

Except for the version differences listed below, all versions provide the same four-quadrant configuration model and terrain semantics. World height, Nether biome names, biome-locating methods, runtime sea levels, client-side appearance, and part of the vanilla special mob-spawning behaviors change with Minecraft itself, and Quadra Gen follows the vanilla Noise or Flat behavior of the corresponding version.

| Difference | Versions | Details |
| --- | --- | --- |
| World height and minimum buildable height | Varies with vanilla | Decides the starting Y and the layer height limit of Flat |
| Default Nether biome ID | 1.14.4–1.15.2 | Uses the `minecraft:nether` ID of that time |
| Biome locating | None in 1.14.4–1.15.2; 2D in 1.16.5–1.18.2; 3D in 1.19.4 and above | See 6.2 |
| Reserved chunks at the axes for the initial spawn | 16 in 1.14.4–1.17.1; 5 in 1.18.2 and above | See section 12 |
| Runtime sea level | Per quadrant from 1.21.3; also covers the Nautilus from 1.21.11 | See 9.2 |
| Client void fog | 1.16.5 and above | See 10.3 |
| Client precipitation | 1.21.3 and above | See 10.3 |
| Neighbor biome reference in surface generation | 26.2 and above | See section 8 |
| Special spawners and slime suppression | 1.14.4–1.15.2 | See 11.3 |
