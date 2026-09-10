# Configuration File

English | [中文](config.zh.md)

## Configuration File Reference

Quadra Gen's configuration file is located at `config/quadragen/config.json`. If the file does not exist, the mod creates a default configuration automatically at startup.

The configuration is read once at server startup. Changes take effect after restarting the server; hot reload is not supported. The configuration is validated strictly: unknown fields, missing fields, wrong types, or invalid values all cause the mod to report an error and refuse to load.

### Activation conditions

Quadra Gen takes effect for a dimension only when all of the following hold:

1. The root-level [`enabled`](#enabled) is `true`
2. The world is not a singleplayer integrated server, or [`enabled_in_singleplayer`](#enabled_in_singleplayer) is `true`
3. The dimension is the Overworld or the Nether, and the corresponding dimension's `enabled` is `true`
4. The dimension uses the vanilla natural-terrain (Noise) generator of the corresponding version

If any condition is not met, the dimension keeps vanilla behavior and Quadra Gen does not intervene.

### Root configuration

This is the root json object of the configuration file

```json
{
    "schema_version": 1,
    "enabled": true,
    "enabled_in_singleplayer": false,

    // dimension configurations. See the sections below
    "overworld": {/* Overworld configuration */},
    "nether": {/* Nether configuration */}
}
```

#### schema_version

The format version of the configuration file. It must match the version required by the mod, otherwise the mod refuses to load the configuration

- Type: `int`
- Default: `1`

#### enabled

The master switch of the mod. When set to `false`, Quadra Gen does not take effect in any dimension

- Type: `bool`
- Default: `true`

#### enabled_in_singleplayer

Whether Quadra Gen takes effect in the singleplayer integrated server

Quadra Gen is designed for multiplayer servers, so it is disabled in singleplayer by default. Set it to `true` to use it in singleplayer worlds

- Type: `bool`
- Default: `false`

#### overworld, nether

The dimension configurations of the Overworld and the Nether. The two dimensions are independent and can be enabled and configured separately

The End and custom dimensions are not supported, and Quadra Gen does not read or use any configuration related to them

- Type: [`Dimension configuration`](#dimension-configuration)
- Default: `enabled: true`; quadrants use the [default layout](#quadrants)
---

### Dimension configuration

```json
{
    "enabled": true,
    "quadrants": {
        // the configuration of all four quadrants, all required. See the sections below
        "x_positive_z_positive": {/* Quadrant configuration */},
        "x_negative_z_positive": {/* Quadrant configuration */},
        "x_negative_z_negative": {/* Quadrant configuration */},
        "x_positive_z_negative": {/* Quadrant configuration */}
    }
}
```

#### enabled

The switch of this dimension. When set to `false`, the dimension keeps vanilla world generation

- Type: `bool`
- Default: `true`

#### quadrants

The configuration of the four quadrants. All four quadrants must be present

The correspondence between keys and coordinate ranges:

| Key | Coordinate range |
| --- | --- |
| `x_positive_z_positive` | `x >= 0, z >= 0` |
| `x_negative_z_positive` | `x < 0, z >= 0` |
| `x_negative_z_negative` | `x < 0, z < 0` |
| `x_positive_z_negative` | `x >= 0, z < 0` |

The X/Z axes align with chunk boundaries, so each chunk belongs to exactly one quadrant

The default quadrant layout is as follows (the `flat` column corresponds to the [`flat`](#flat) configuration):

| Quadrant | `generator` | `clear_generated_content` | `flat` |
| --- | --- | --- | --- |
| `(+X,+Z)` | `noise` | `false` | — |
| `(-X,+Z)` | `noise` | `true` | — |
| `(-X,-Z)` | `flat` | `false` | `minecraft:the_void`, empty layers |
| `(+X,-Z)` | `flat` | `false` | Overworld: `minecraft:plains`, one layer of white stained glass; Nether: `minecraft:nether_wastes`, one layer of white stained glass |

In Minecraft 1.14.4–1.15.2, the default Nether biome is `minecraft:nether`.

- Type: `Map[str, Quadrant configuration]`

---

### Quadrant configuration

```json
{
    "generator": "noise",
    "clear_generated_content": false
}
```

#### generator

The terrain generator used by this quadrant. Available values:

| Value | Description |
| --- | --- |
| `noise` | The vanilla natural terrain of the current dimension (normal world) |
| `flat` | Flat terrain with a fixed biome and fixed block layers |

- Type: `str`
- Default: see the [default layout](#quadrants)
#### clear_generated_content

Whether to prevent this quadrant from placing new world-generation content (terrain blocks, structure content, decoration, worldgen entities, etc.)

When set to `true`, new chunks no longer generate the content of the corresponding terrain, but biomes, theoretical terrain, structure data, and terrain-related queries still follow the corresponding mode. It only affects the generation of new content and never clears or deletes anything that already exists

- Type: `bool`
- Default: see the [default layout](#quadrants)
#### flat

The flat configuration. Required when `generator` is `flat`; must not be present when `generator` is `noise`

- Type: [`Flat configuration`](#flat-configuration)

---

### Flat configuration

```json
{
    "biome": "minecraft:plains",
    "layers": [
        {
            "block": "minecraft:white_stained_glass",
            "count": 1
        }
    ]
}
```

#### biome

The fixed biome ID used by this quadrant. Must be a valid registered biome ID

- Type: `str`
- Default: see the [default layout](#quadrants)
#### layers

The ordered list of block layers from bottom to top. The first layer starts at the dimension's minimum build height, and each following layer sits directly on top of the previous one

An empty list is valid and produces a completely empty Flat area, without the initial platform of the vanilla Void preset

Layer placement follows the vanilla Flat generator of the corresponding version

- Type: `List[Layer configuration]`
- Default: see the [default layout](#quadrants)
#### layers[].block

The block ID of this layer. Must be a valid registered block ID

- Type: `str`

#### layers[].count

The thickness of this layer, i.e. how many times this block is repeated consecutively. Must be a positive integer

The total thickness of all layers must not exceed the dimension's buildable height, otherwise the mod reports an error

- Type: `int`

