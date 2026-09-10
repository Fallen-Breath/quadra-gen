# Quadra Gen

[![License](https://img.shields.io/github/license/Fallen-Breath/quadra-gen.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://github.com/Fallen-Breath/quadra-gen/actions/workflows/gradle.yml/badge.svg)](https://github.com/Fallen-Breath/quadra-gen/actions/workflows/gradle.yml)

[English](README.md) | 中文

## 功能

Quadra Gen 是一个为创造服设计的 mod，能让单个维度的四个象限拥有各自独立的地形生成逻辑。
它让单个维度中同时存在自然地形和超平坦地形，还支持保留群系与结构范围、但不生成任何方块和实体的模式，以满足玩家的各种地形需求。

在默认配置下，四个象限的表现如下：

| 象限     | 默认模式    | 默认结果                                                                            |
|----------|-------------|-------------------------------------------------------------------------------------|
| `+X, +Z` | 普通世界     | 当前维度的普通原版地形                                                              |
| `-X, +Z` | 普通世界（清空） | 保留原版群系、理论地形和结构信息的空地                                           |
| `-X, -Z` | 超平坦      | 空 `minecraft:the_void` 超平坦地形                                                 |
| `+X, -Z` | 超平坦      | 一层白色染色玻璃；主世界为 `minecraft:plains`，下界为 `minecraft:nether_wastes`     |

主世界和下界拥有相互独立的设置。末地和自定义维度不受影响。

支持的 Mod 平台：Fabric。无需额外依赖。纯服务端 mod，客户端无需安装即可进服。

支持的 Minecraft 版本：

- 1.14.4、1.15.2、1.16.5、1.17.1、1.18.2、1.19.4
- 1.20.1、1.20.2、1.20.4、1.20.6
- 1.21.1、1.21.3、1.21.4、1.21.5、1.21.8、1.21.10、1.21.11
- 26.1.2、26.2、……

更详细的功能与行为说明见 [docs/features.zh.md](docs/features.zh.md)

## 使用

1. 下载 Quadra Gen 的发布版 mod jar，放入服务器的 `mods` 文件夹中。
2. 若要自定义各维度/象限的布局，请在创建世界前准备好或编辑 `config/quadragen/config.json`。若配置文件不存在，Quadra Gen 会自动创建一份默认的配置并启用。
3. 启动服务器并创建世界，即可享受 Quadra Gen 生成的，不同象限不同地形的世界。

默认配置大致如下：

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
    /* 与 overworld 相同 */
  }
}
```

配置文件格式详情见 [docs/config.zh.md](docs/config.zh.md)

## 说明

### 存档兼容性

在已有世界上启用 Quadra Gen 时，只有新生成的区块才会按象限配置生成。已完成的区块不会被重建、清空或转换。

下表总结了预期的行为：

| 情况                       | Quadra Gen 操作 | 已有区块                  | 新区块 / 未完成的生成                                     |
|----------------------------|-----------------|---------------------------|-----------------------------------------------------------|
| 已存在的自然地形存档       | 安装 Mod        | 保持不变                  | 按 Quadra Gen 当前象限配置生成                             |
| 已存在的原版超平坦存档     | 安装 Mod        | 保持不变                  | 保持不变。Quadra Gen 不支持超平坦存档，如需使用请先手动转换为自然地形存档 |
| 使用 Quadra Gen 生成的世界 | 移除 Mod        | 保持不变，且可被正常读取  | 按照原版逻辑生成                                           |
| 使用 Quadra Gen 生成的世界 | 更改配置        | 保持不变                  | 按 Quadra Gen 的新配置生成                                 |

Quadra Gen 不会修改存档格式，因此经 Quadra Gen 处理过的存档有着与原版无异的兼容性。不过，仍建议在操作前做好备份。

### 游戏内行为

一些值得注意的表现：

- 四个象限共享同一个维度状态。时间、天气、游戏规则、刷怪上限等机制仍是全维度共享的。
- 坐标轴仅作为地形生成边界，不是游戏逻辑边界。实体、流体、红石等游戏机制均可自由跨过坐标轴。
- 坐标轴这一地形生成边界并非完全严格。原版结构和装饰可以有限度地跨过坐标轴。
- 超平坦象限不会生成结构、湖泊。
