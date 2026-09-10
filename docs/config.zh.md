# 配置文件

[English](config.md) | 中文

## 配置文件详解

Quadra Gen 的配置文件位于 `config/quadragen/config.json`。若文件不存在，Mod 会在启动时自动创建一份默认配置。

配置仅在服务器启动时读取一次，修改后需要重启服务器才会生效，不支持热重载。配置会经过严格校验：未知字段、缺失字段、错误类型或非法值都会导致 Mod 报错并拒绝加载。

### 生效条件

Quadra Gen 对某个维度生效需要同时满足：

1. 根级 [`enabled`](#enabled) 为 `true`
2. 不是单人游戏的内置服务器，或 [`enabled_in_singleplayer`](#enabled_in_singleplayer) 为 `true`
3. 该维度是主世界或下界，且对应维度的 `enabled` 为 `true`
4. 该维度使用对应版本的原版自然地形（Noise）生成器

不满足任一条件时，该维度保持原版行为，Quadra Gen 不会介入。

### 根配置

这是配置文件中的根 json 对象

```json
{
    "schema_version": 1,
    "enabled": true,
    "enabled_in_singleplayer": false,

    // 维度配置。详见以下各节
    "overworld": {/* 主世界配置 */},
    "nether": {/* 下界配置 */}
}
```

#### schema_version

配置文件的格式版本。必须与 Mod 当前要求的版本一致，否则 Mod 会拒绝加载配置

- 类型：`int`
- 默认值：`1`

#### enabled

Mod 的总开关。设为 `false` 时，Quadra Gen 不会对任何维度生效

- 类型：`bool`
- 默认值：`true`

#### enabled_in_singleplayer

是否在单人游戏的内置服务器（integrated server）中生效

Quadra Gen 面向多人服务器设计，因此单人游戏默认不启用。若要在单人世界中使用，请设为 `true`

- 类型：`bool`
- 默认值：`false`

#### overworld, nether

主世界和下界的维度配置。两个维度相互独立，可分别开关与配置象限

末地和自定义维度不受支持，Quadra Gen 不会读取或使用与它们相关的任何配置

- 类型：[`维度配置`](#维度配置)
- 默认值：`enabled: true`，象限采用[默认布局](#quadrants)
---

### 维度配置

```json
{
    "enabled": true,
    "quadrants": {
        // 四个象限的配置，缺一不可。详见以下各节
        "x_positive_z_positive": {/* 象限配置 */},
        "x_negative_z_positive": {/* 象限配置 */},
        "x_negative_z_negative": {/* 象限配置 */},
        "x_positive_z_negative": {/* 象限配置 */}
    }
}
```

#### enabled

该维度的开关。设为 `false` 时，该维度保持原版世界生成

- 类型：`bool`
- 默认值：`true`

#### quadrants

四个象限的配置。四个象限必须全部提供，缺一不可

键名与坐标范围的对应关系如下：

| 键名 | 坐标范围 |
| --- | --- |
| `x_positive_z_positive` | `x >= 0, z >= 0` |
| `x_negative_z_positive` | `x < 0, z >= 0` |
| `x_negative_z_negative` | `x < 0, z < 0` |
| `x_positive_z_negative` | `x >= 0, z < 0` |

X/Z 坐标轴与区块边界重合，因此每个区块只会属于一个象限

默认象限布局如下（`flat` 栏对应 [`flat`](#flat) 配置）：

| 象限 | `generator` | `clear_generated_content` | `flat` |
| --- | --- | --- | --- |
| `(+X,+Z)` | `noise` | `false` | — |
| `(-X,+Z)` | `noise` | `true` | — |
| `(-X,-Z)` | `flat` | `false` | `minecraft:the_void`，空层 |
| `(+X,-Z)` | `flat` | `false` | 主世界 `minecraft:plains`，一层白色染色玻璃；下界 `minecraft:nether_wastes`，一层白色染色玻璃 |

Minecraft 1.14.4–1.15.2 的下界默认群系为 `minecraft:nether`。

- 类型：`Map[str, 象限配置]`

---

### 象限配置

```json
{
    "generator": "noise",
    "clear_generated_content": false
}
```

#### generator

该象限使用的地形生成器。可用值：

| 值 | 描述 |
| --- | --- |
| `noise` | 当前维度的原版自然地形（普通世界） |
| `flat` | 固定群系、固定方块层的超平坦地形 |

- 类型：`str`
- 默认值：见[默认布局](#quadrants)
#### clear_generated_content

是否阻止该象限放置新的世界生成内容（地形方块、结构内容、装饰和世界生成实体等）

设为 `true` 时，新区块不再生成对应地形的内容，但群系、理论地形、结构信息和地形相关查询仍按对应模式工作。它只影响新内容的生成，不会清除或删除任何已存在的内容

- 类型：`bool`
- 默认值：见[默认布局](#quadrants)
#### flat

超平坦配置。当 `generator` 为 `flat` 时必填；当 `generator` 为 `noise` 时不允许出现此字段

- 类型：[`超平坦配置`](#超平坦配置)

---

### 超平坦配置

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

该象限使用的固定群系 ID。必须是已注册的有效群系 ID

- 类型：`str`
- 默认值：见[默认布局](#quadrants)
#### layers

从低到高依次排列的方块层列表。第一层从该维度的最低构建高度开始铺设，之后每层紧接上一层

空列表是合法的，将生成完全空旷的纯空超平坦区域，且不会生成原版 Void 预设附带的初始平台

层放置行为与对应版本的原版超平坦生成器一致

- 类型：`List[层配置]`
- 默认值：见[默认布局](#quadrants)
#### layers[].block

该层的方块 ID。必须是已注册的有效方块 ID

- 类型：`str`

#### layers[].count

该层的厚度，即该方块连续铺设的层数。必须为正整数

所有层厚度之和不能超过该维度的可建造高度，否则 Mod 会报错

- 类型：`int`

