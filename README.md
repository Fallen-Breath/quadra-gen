# Quadra Gen

[![License](https://img.shields.io/github/license/Fallen-Breath/quadra-gen.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://github.com/Fallen-Breath/quadra-gen/actions/workflows/gradle.yml/badge.svg)](https://github.com/Fallen-Breath/quadra-gen/actions/workflows/gradle.yml)

## Feature

Quadra Gen divides the Overworld and the Nether into four terrain quadrants along the X and Z axes.
Each quadrant can use vanilla Noise terrain or a configurable Flat terrain.
Either type can also be configured as `clear`, keeping its biome and terrain-query behavior while preventing newly generated terrain content from being placed.

This provides natural terrain, an empty natural-biome area, an empty Flat area, and a building platform in one dimension.
Crossing an axis changes the terrain environment without changing dimensions.

The axes follow chunk boundaries. Coordinates where X or Z equals zero belong to the positive side, so each chunk has exactly one quadrant:

| Quadrant | Default mode | Default result                                                                                                    |
|----------|--------------|-------------------------------------------------------------------------------------------------------------------|
| `+X, +Z` | Noise        | Normal vanilla terrain for the current dimension                                                                  |
| `-X, +Z` | Noise clear  | Empty terrain with vanilla Noise biomes, theoretical terrain, and structure data                                  |
| `-X, -Z` | Flat         | Empty `minecraft:the_void` Flat terrain                                                                           |
| `+X, -Z` | Flat         | One layer of white stained glass; `minecraft:plains` in the Overworld and `minecraft:nether_wastes` in the Nether |

The Overworld and Nether have independent settings. The End and custom dimensions are not modified.
Vanilla clients can join a multiplayer server without installing Quadra Gen.
Singleplayer support is available through the integrated server but is disabled by default.

Supported Minecraft versions:

- 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, and 1.19.4
- 1.20.1, 1.20.2, 1.20.4, and 1.20.6
- 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, and 1.21.11
- 26.1.2 and 26.2

Supported Mod Platform: Fabric. No extra dependency is needed.

## Usage

1. Download the Quadra Gen jar matching your Minecraft version and place it in the server's `mods` folder.
2. If you want to customize the layout, prepare or edit `config/quadragen/config.json` before creating the world.
   The complete default file is shown below. If the file does not exist yet, start the server once to let Quadra Gen create it, stop the server, and then edit it.
3. Start the server and create the world.

Leaving the default configuration unchanged is also valid: start the server directly and Quadra Gen will create the file automatically. Preparing the configuration before world creation gives the most predictable result. Quadra Gen can also run on an existing compatible world, but it does not regenerate or rearrange chunks that already exist.

For singleplayer, install the jar in the instance's `mods` folder and set `enabled_in_singleplayer` to `true` before starting the world.

The generated default configuration has the following format:

```json5
{
  "schema_version": 1,
  "enabled": true,
  "enabled_in_singleplayer": false,
  "overworld": {
    "enabled": true,
    "quadrants": {
      "x_positive_z_positive": {
        "generator": "noise",
        "clear_generated_content": false
      },
      "x_negative_z_positive": {
        "generator": "noise",
        "clear_generated_content": true
      },
      "x_negative_z_negative": {
        "generator": "flat",
        "clear_generated_content": false,
        "flat": {
          "biome": "minecraft:the_void",
          "layers": []
        }
      },
      "x_positive_z_negative": {
        "generator": "flat",
        "clear_generated_content": false,
        "flat": {
          "biome": "minecraft:plains",
          "layers": [
            {
              "block": "minecraft:white_stained_glass",
              "count": 1
            }
          ]
        }
      }
    }
  },
  "nether": {
    /* the same as overworld */
  }
}
```

To customize a Flat quadrant, change its `biome` and replace `layers` with the desired ordered list.
Each layer has a block ID and a positive `count`; the first layer starts at that dimension's minimum build height.
An empty list creates a completely empty Flat area and does not create the vanilla Void preset platform.

To create a clear quadrant, set `clear_generated_content` to `true`.
A Noise clear quadrant keeps vanilla Noise biome, theoretical terrain, and structure behavior while generating no new terrain content.
A Flat clear quadrant keeps its configured biome and theoretical layers while placing no layers.

The configuration is read at startup and validated strictly. Keep all four quadrant entries, use valid registered biome and block IDs,
and do not add a `flat` section to a Noise quadrant.

## Notes

### Save compatibility

Quadra Gen is safest when its configuration is prepared before a new world is created.
It can be enabled on an existing world, but only newly generated or unfinished chunks are affected.
Completed chunks are not rebuilt, cleared, or converted,
so a world opened with Quadra Gen may contain old vanilla terrain next to newly generated quadrants.

The following table summarizes the expected save behavior:

| Situation                                                     | Existing chunks                                             | New chunks / unfinished generation                                                                                         |
|---------------------------------------------------------------|-------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Compatible Noise-based world, Mod installed                   | Kept as they are                                            | Generated according to the current quadrant configuration                                                                  |
| World generated with Quadra Gen, Mod remains installed        | Kept as they are                                            | Continue using the current configuration                                                                                   |
| World generated with Quadra Gen, Mod removed                  | Kept and readable by the matching vanilla version           | New chunks generate as ordinary vanilla Noise terrain; unfinished chunks may be completed as Noise and look mixed          |
| Configuration changed while the world already contains chunks | Kept as they are                                            | Later unfinished or not-yet-generated areas use the new configuration, which may create seams                              |
| Existing vanilla Flat world with Quadra Gen added             | Kept as they are, but not managed as a supported base world | Do not expect Quadra Gen quadrant generation; convert the dimension to the matching vanilla Noise world type offline first |

Quadra Gen does not write a private generator format or automatically edit `level.dat`; generated chunks remain in vanilla-compatible formats, but removing the mod also removes the quadrant rules for future generation.

Always make a full backup before adding the mod to an important world, removing it, changing the underlying world type, or changing quadrant settings.

### In-game behavior

- All four quadrants share one dimension state. Time, weather, difficulty, game rules, world border, and the dimension's mob cap are shared rather than duplicated.
- Entities, fluids, redstone, POI, raids, portals, scheduled ticks, and player-built blocks can cross the axes. The axes are terrain-generation boundaries, not physical walls.
- Vanilla structures and decoration can extend a limited distance across an axis. Small structure edges, tree parts, or similar boundary effects are expected; distant terrain should not continuously leak into another quadrant.
- Noise clear preserves structure information and biome behavior, but an empty area usually cannot spawn mobs until players build a suitable physical environment. Empty terrain or `minecraft:the_void` does not by itself disable phantoms.
- Flat quadrants do not generate their own structures, lakes, or normal biome features. Their configured biome still affects biome-based behavior.
