# Quadra Gen Features

English | [中文](features.zh.md)

## 1. Project Overview

Quadra Gen is a Fabric world-generation mod that provides four independently configurable terrain environments within a single dimension.

It splits the Overworld or the Nether into four quadrants along the X/Z axes. Each quadrant can use the vanilla natural terrain of the current dimension, or flat terrain with a fixed biome and fixed layer height; either terrain type can also have its actual content generation turned off. Players can switch environments simply by crossing an axis, without having to create multiple dimensions for natural survival areas, empty testing areas, and flat design areas.

The four quadrants still belong to the same vanilla dimension. They share time, weather, difficulty, game rules, the world border, the player list, and the entity cap, and do not isolate redstone, fluids, block events, POI, raids, entity AI, or portals. What Quadra Gen changes is how new chunks are generated and the query results directly related to the terrain type — it does not split a dimension into four independent worlds.

The mod only needs to be installed on the multiplayer server; vanilla clients can connect directly. Singleplayer runs the same functionality through the integrated server, but it is disabled by default and needs to be explicitly enabled in the configuration.

## 2. Scope

Quadra Gen only affects the Overworld and the Nether, and the two dimensions can be enabled or disabled independently. The End, datapack-defined custom dimensions, and dimensions added by other mods are not affected. Generators whose underlying type is not vanilla Noise do not enable this feature; compatibility with third-party generators that extend the vanilla Noise type is not guaranteed.

Managed dimensions must use the vanilla natural terrain of the corresponding version as the underlying world type. The Overworld uses the vanilla Overworld Noise terrain, and the Nether uses the vanilla Nether Noise terrain. Existing flat worlds are not automatically brought under management, nor automatically converted.

Whether the feature is enabled is decided by four layers of switches together:

1. The master switch of the mod;
2. The singleplayer switch;
3. The independent switch of the Overworld or the Nether;
4. Whether the current dimension uses the supported vanilla natural terrain.

The configuration is read only once at mod startup; hot reload is not supported.

## 3. Quadrant Division

The quadrant origin is fixed at `(0, 0)` and cannot be moved or rotated. Coordinates where X or Z equals zero belong to the positive side:

| Config key | Coordinate range |
| --- | --- |
| `x_positive_z_positive` | `x >= 0, z >= 0` |
| `x_negative_z_positive` | `x < 0, z >= 0` |
| `x_negative_z_negative` | `x < 0, z < 0` |
| `x_positive_z_negative` | `x >= 0, z < 0` |

The two axes lie exactly on chunk boundaries, so each chunk belongs to exactly one quadrant, and two base terrain types are never spliced within a single chunk.

The Overworld and the Nether each have a complete four-quadrant configuration. The same coordinates can use different biomes, layer configurations, or terrain modes in the two dimensions.

## 4. The Four Terrain Modes

Each quadrant is defined by two properties:

- `generator` decides whether natural terrain or flat terrain is used;
- `clear_generated_content` decides whether to prevent that terrain from actually generating content.

The two combine into four modes:

| Mode | Biome | Theoretical terrain | Structure data | Actual content of new chunks |
| --- | --- | --- | --- | --- |
| Noise | Vanilla natural biome of the current dimension | Vanilla natural terrain | Vanilla structures | Complete vanilla terrain, structures, decoration, and worldgen mobs |
| Noise clear | Vanilla natural biome of the current dimension | Vanilla natural terrain | Vanilla structure data kept | Does not actively generate terrain, structure content, decoration, or worldgen mobs |
| Flat | Configured fixed biome | Configured fixed block layers | Does not create structures belonging to this quadrant | Only generates the configured block layers |
| Flat clear | Configured fixed biome | Configured fixed block layers | Does not create structures belonging to this quadrant | Generates no block layers or other world-generation content |

"Theoretical terrain" is the height and block column results given by the world generator for specified coordinates. It is a different concept from the blocks that actually exist in the chunk. The clear mode blocks content generation, but does not erase the theoretical information of the corresponding terrain type.

### 4.1 Noise

Noise quadrants keep the vanilla natural generation of the current dimension. Far from the axes, their biomes, terrain, caves, aquifers, surfaces, structures, decoration, and worldgen mobs are all decided by vanilla natural-generation rules.

The Overworld and the Nether each use their own natural-generation rules. The Nether does not gain the Overworld's height, sea level, sky, or mob behaviors just because it uses the same four-quadrant model.

### 4.2 Noise clear

Noise clear creates an "empty area that keeps natural world semantics". New chunks still save the vanilla natural biome, and keep the theoretical heights, theoretical block columns, and structure data of the vanilla natural terrain, but do not actively generate natural terrain blocks, structure blocks, decorative content, or worldgen mobs.

This lets players build their own environment in the empty area while continuing to use the local natural biome and the vanilla mechanics brought by valid structure ranges. For example, a Nether Noise clear area can keep the structure range of a Nether fortress; after players build a platform that satisfies the conditions, the related structure mob-spawning rules still have their basis.

### 4.3 Flat

A Flat quadrant consists of a fixed biome and an ordered list of block layers. The first layer always starts at the minimum buildable height of the current dimension; no separate `base_y` is provided. When the same configuration is used for different Minecraft versions or different dimensions, the actual starting Y changes with the world height of that version and dimension.

Each layer has a block ID and a positive integer thickness. The current configuration expresses the default state of blocks and cannot specify block state properties such as facing or waterlogging. The total thickness of all layers must not exceed the buildable height of the current dimension.

Flat layers follow the layer-placement semantics of the vanilla flat generator of the corresponding version, including the handling of layers that cannot be placed directly during the base terrain stage. It does not additionally enable the structures, lakes, or normal biome decoration of vanilla flat worlds. An empty layer list is a valid completely-empty Flat, and it does not generate the initial platform that comes with the vanilla Void preset.

Height and block column queries of a Flat quadrant are based on the complete configured layers. Query results do not lose layers that are placed later in the generation process.

### 4.4 Flat clear

Flat clear saves the configured biome and the complete theoretical layers, but does not actually place any of them. It suits scenarios that need a fixed biome and fixed theoretical height while keeping new chunks completely empty.

`clear_generated_content` only controls whether generated content is placed. It does not turn Flat clear into Noise clear, nor does it change the Flat biome, theoretical height, or sea level semantics.

## 5. Boundaries of Clear

Clear is implemented by blocking the relevant generation processes of new chunks, not by generating complete content first and then deleting it. Therefore it does not create and remove large numbers of blocks, block entities, fluid ticks, or entities just to empty the chunk.

Clear has no retroactive cleaning ability. The following content is not deleted or stopped because the quadrant it is in has clear enabled:

- Blocks and entities in completed chunks;
- Content placed by players or commands;
- Entities produced at runtime by natural mob spawning, spawners, breeding, or other means;
- Redstone, fluids, random ticks, scheduled ticks, and block events;
- Entities or runtime changes crossing in from adjacent areas.

Changing the configuration also does not reshape completed chunks. Configuration changes only affect chunks that have not yet gone through the corresponding generation processes, and the world-level theoretical queries answered according to the current configuration.

## 6. Biome and Terrain Queries

Chunks save the real biome that their quadrant should have: the two Noise modes save the natural biome, and the two Flat modes save the configured biome. Therefore, after players enter a generated chunk, weather, natural mob spawning, and other vanilla systems that read chunk biomes can see correct results.

When locating biomes for coordinates that have not been generated yet, Quadra Gen also answers according to the quadrant, instead of treating the whole dimension as the underlying Noise world. The search can find both natural biomes and biomes that only exist in Flat configurations, while keeping the vanilla search order and sampling of the corresponding Minecraft version.

Biome-locating capabilities differ across versions:

| Minecraft version | Behavior |
| --- | --- |
| 1.14.4–1.15.2 | These versions have no server-side biome-locating feature for mods to extend, and Quadra Gen does not provide an extra command |
| 1.16.5–1.18.2 | Search Noise and Flat biomes with the 2D nearest-first rules of the corresponding version |
| 1.19.4 and above | Find Noise and Flat biomes with the 3D search rules of the corresponding version |

At each sampling point, the two Flat modes use the configured biome, and the two Noise modes use the natural biome. Clear does not affect biome locating.

Height and block column queries also follow the quadrant type:

- Noise and Noise clear return the theoretical results of the natural terrain;
- Flat and Flat clear return the theoretical results of the configured layers;
- Clear does not change the theoretical results into "all air";
- The theoretical height of an empty Flat falls back to the minimum build Y of the corresponding version.

This means the theoretical height of Noise clear or Flat clear can differ from the actual heightmap of the chunk: the theoretical height describes the generation basis of the corresponding terrain type, while the actual heightmap describes the blocks that really exist in the world.

## 7. Structures and Structure Locating

Noise and Noise clear keep the structure data that the vanilla natural terrain should have. Noise generates structure content normally; Noise clear only keeps the positions, ranges, and associated data of structures, and does not actively place structure blocks, loot containers, spawners, or related worldgen mobs.

Flat and Flat clear do not create structures of their own. Therefore coordinates in these quadrants are not treated as valid structure sources merely because the underlying world is Noise.

Structure locating follows the structure data actually kept:

- Noise structures can be located;
- Structures kept in Noise clear can be located;
- A Flat quadrant does not report a structure that was not actually built there;
- Eyes of Ender, exploration maps, dolphin treasure hunting, and villager map trades follow the same results as the structure-locating commands.

Noise structures near the axes may partially extend into an adjacent quadrant. Adjacent chunks keep the associated data needed to recognize such a structure, so structure-range mob spawning, maps, and explored-structure counts are not abruptly cut off at the axes. This cross-boundary association is not equivalent to Flat quadrants having structure generation enabled.

## 8. Quadrant Boundaries

The base terrain mode of chunks has a strict boundary: Flat or clear chunks do not generate Noise base terrain, surfaces, or caves, and ordinary Noise chunks do not treat Flat or clear chunks as cave sources. Caves do not continuously carve into ordinary Noise areas from distant incompatible quadrants.

Structures and decoration follow the vanilla neighbor-generation range without per-block hard clipping. Therefore tree edges, structure corners, or other limited cross-boundary content may appear near the axes. The affected range is decided by the vanilla generation behavior of the corresponding version, and no channel is formed for distant Noise areas to continuously pollute other quadrants.

In Minecraft 26.2, vanilla surface generation consults the actual biomes of the target chunk and its neighboring chunks. Noise chunks next to the axes may therefore be locally affected by adjacent Flat biomes; this behavior is not known in the other target versions.

The runtime world state has no quadrant walls. Entities, player-placed blocks, fluids, explosions, redstone, POI, raids, and portals can all cross the axes.

## 9. Sea Level Semantics

During world generation, the two Noise modes always use the vanilla natural sea level of the current dimension, so that natural terrain, aquifers, surfaces, and structure calculations stay consistent. The two Flat modes do not enter these Noise generation processes; they only process the configured layers according to the vanilla flat rules of the corresponding version, and do not fill water based on the sea level. Clear does not change the sea level semantics.

At runtime, different quadrants in the same dimension can use different sea levels only when a check carries explicit X/Z coordinates. Coordinate-free dimension-level queries keep returning the underlying Noise value, and do not guess the quadrant from the nearest player, the current task, or other implicit state.

Runtime behavior across versions:

| Minecraft version | Runtime behavior |
| --- | --- |
| 1.14.4–1.21.1 | The vanilla runtime world interfaces of these versions do not provide different sea level values between Noise and Flat; the related checks keep the vanilla value |
| 1.21.3 and above | Server-side weather, ice and snow, aquatic mob spawning, part of aquatic AI, phantom spawning, and phantom AI with explicit coordinates use the Noise or Flat sea level according to the quadrant |
| 1.21.11 and above | On top of the previous row, the Nautilus spawn-height check also follows the quadrant |

In the current 1.21.3+ target versions, the vanilla Flat sea level is `-63`; the Overworld and Nether Noise values come from their respective natural generation settings, usually `63` and `32`.

Vanilla clients still only receive one dimension-level sea level, and Quadra Gen does not extend the network protocol. Therefore authoritative server-side checks with explicit coordinates can follow the quadrant, while client-side weather rendering and third-party logic that directly reads the dimension-level sea level may still use the Noise value.

## 10. Mob Spawning

Noise quadrants keep the mob generation during vanilla chunk generation. Noise clear, Flat, and Flat clear do not actively place mobs during the generation of new chunks.

Runtime natural mob spawning is still managed by vanilla uniformly, and shares one cap within the same dimension. Candidate results depend on the biome saved in the current chunk, valid structure ranges, actual blocks, fluids, light, and heightmaps:

- Noise is the same as an ordinary vanilla natural world;
- Noise clear usually has no valid spawning positions when completely empty; after players build the environment, mobs can spawn according to the natural biome and structure ranges;
- Flat uses the configured biome and the actually placed surface;
- Flat clear uses the configured biome, but must still satisfy the vanilla spawning conditions in the real world.

Phantoms, patrols, cats, village sieges, and wandering traders keep following the vanilla rules of the corresponding version. Empty layers, `minecraft:the_void`, or clear by themselves do not become extra phantom-disabling conditions. `minecraft:the_void` can still block ordinary biome mob spawning and wandering traders through vanilla biome rules.

In Minecraft 1.14.4–1.15.2, vanilla flat worlds do not run patrols and village sieges. Quadra Gen keeps this difference in the Flat quadrants of these two versions; Noise clear still belongs to the Noise type. From Minecraft 1.16.5 on, the vanilla Noise and Flat Overworld use the same set of special spawners, so these events are no longer disabled per quadrant, and whether they can actually spawn is decided by the biome and the actual environment.

Existing entities can cross quadrants freely. The mod does not delete, freeze, or reclassify an entity because it enters another terrain mode.

## 11. Initial Spawn Point

Quadra Gen only adjusts the initial spawn selection of a brand-new Overworld. The spawn point of existing worlds is not recalculated, and the Nether does not take part in this feature.

If any quadrant is an ordinary Noise quadrant, the mod moves the center of the vanilla spawn search into the nearest ordinary Noise quadrant, then hands back to the vanilla flow to find a specific safe position. To keep the search range from crossing the axes, 1.14.4–1.17.1 reserve 16 chunks, and 1.18.2+ reserve 5 chunks.

The current feature does not fall back to a safe non-clear Flat quadrant. If no ordinary Noise quadrant exists, the vanilla search center is kept. The mod does not create a spawn platform for an all-empty configuration, nor does it log a special all-empty warning.

If there is no fluid-free surface with a full top collision shape below the final spawn position, the bonus chest is disabled, to avoid trying to generate it at an obviously unsafe position. The rest of the spawn search rules are still decided by the vanilla of the corresponding version.

## 12. Configuration

The configuration file is located at `config/quadragen/config.json`, and the current schema version is `1`. When the configuration file does not exist, the mod creates the default file.

The configuration consists of the following hierarchy:

```text
Global configuration
├── schema_version
├── enabled
├── enabled_in_singleplayer
├── overworld
│   ├── enabled
│   └── four quadrants
└── nether
    ├── enabled
    └── four quadrants
```

Each quadrant must declare `generator` and `clear_generated_content`. When `flat` is chosen, the fixed biome and the complete layer list must also be provided; when `noise` is chosen, no Flat configuration may be carried.

The configuration is validated strictly:

- Unknown fields, missing fields, and wrong data types are rejected;
- `schema_version` must equal the current version;
- All four quadrants are required;
- `generator` can only be `noise` or `flat`;
- Biomes and blocks must be valid and registered IDs;
- Each layer thickness must be a positive integer;
- The total layer height must not exceed the buildable range of the corresponding dimension.

Biomes, blocks, and layer heights need to be validated against the actual dimension, so the validation only completes when the corresponding dimension is enabled and the world type is supported. Disabled or unsupported dimensions do not get their Flat content parsed.

The default configuration enables the master switch and both dimensions, and disables singleplayer support. The two dimensions use the same mode layout:

| Quadrant | Default mode | Overworld configuration | Nether configuration |
| --- | --- | --- | --- |
| `(+X,+Z)` | Noise | Vanilla Overworld natural terrain | Vanilla Nether natural terrain |
| `(-X,+Z)` | Noise clear | An empty area keeping Overworld natural semantics | An empty area keeping Nether natural semantics |
| `(-X,-Z)` | Flat | `minecraft:the_void`, empty layers | `minecraft:the_void`, empty layers |
| `(+X,-Z)` | Flat | `minecraft:plains`, one layer of white stained glass | `minecraft:nether_wastes`, one layer of white stained glass |

In Minecraft 1.14.4–1.15.2, the default Nether biome ID is `minecraft:nether`, the ID used at that time.

## 13. Save Files and Existing Worlds

Chunks generated by Quadra Gen keep using the vanilla chunk, biome, structure, and entity data formats. The save still records the vanilla Noise world type of the corresponding dimension, and no Quadra Gen-specific generator, biome, or chunk data is required.

Therefore:

- The mod only affects new chunks and chunks whose relevant generation processes are unfinished;
- Completed chunks are not actively rewritten by installing, uninstalling, or changing the configuration;
- Complete chunks generated with the mod can be read by the same version of vanilla;
- After removing the mod, existing chunks stay as they are, and new chunks return to the ordinary Noise generation of the current dimension;
- Unfinished chunks may be completed by the vanilla Noise flow after the mod is removed, resulting in a partial mix;
- The current configuration still affects biome, theoretical terrain, and sea level queries for ungenerated coordinates; the historical configuration used when old chunks were generated cannot be recovered.

In Minecraft 1.18.2 and above, when encountering old chunks that vanilla is upgrading or retrogening, the complete vanilla upgrade flow is run first, without re-laying or clearing according to the current quadrant. This preserves the vanilla old-bedrock handling, terrain connection, and boundary updates.

Old servers created with the vanilla Flat world type are not directly supported. To use Quadra Gen while keeping the existing flat design area, stop the server, make a full backup, and convert the world type of the target dimension to the same-version vanilla Noise while offline. The mod itself does not modify `level.dat` or perform this conversion.

## 14. Supported Versions

The project currently covers the following Minecraft versions:

- 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4;
- 1.20.1, 1.20.2, 1.20.4, 1.20.6;
- 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11;
- 26.1.2, 26.2.

Except for the version differences explicitly listed in this document, all versions provide the same four-quadrant configuration model and terrain semantics. World height, Nether biome names, biome-locating methods, runtime sea levels, and part of the vanilla special mob-spawning behaviors change with Minecraft itself, and Quadra Gen follows the vanilla Noise or Flat behavior of the corresponding version.
