# MMOForge 使用说明

> 本插件基于MMOItems制作，为MMOItems的附属插件

## 依赖

插件版本为MMOForge使用的API版本，最好大于等于版本号

* MMOItems 6.8.2+
* MythicLib 1.4.1+
* VaultAPI 1.7+
* placeholderapi 2.11.2+(非必须)

mmoforge 别名 `mf`, `mforge`, `mmof`

## 配置

配置存放于服务端根目录的 `plugins\MMOForge` 下

结构如下

MMOForge
│ config.yml #插件主配置
│
├───enchants #自定义属性相关配置
│ auto_torch.yml
│ harvester.yml
│ ore_fortune.yml
│ sakura_soul_bound.yml
│ scope_miner.yml
│ silk_touch.yml
│ smelt_ore.yml
│ smelt_shovel.yml
│ speed_up.yml
│ vein_log.yml
│ vein_ore.yml
│
├───materials # 强化|突破的材料语言设置
│ forge_exp.yml
│ limit_level.yml
│
└───ui #自定义UI
break_through.yml
forge.yml
refine.yml

### MMOItems 配置

如果你想让某个MMOItems 的物品可以强化、突破、精炼，需要在物品配置中添加键 forge-attribute 、具体看以下示例

~~~ yaml
LONG_SWORD:
  base:
    material: IRON_SWORD
    name: '&fLong Sword'
    attack-speed: 1.6
    attack-damage:
      base: 6
      scale: 1.2
      spread: 0.1
      max-spread: 0.3
      
    forge-attribute: # 本插件支持的属性名
      star: 3  # 星级, 必选，否则视为不可强化、精炼、突破的物品
      
      ## 以下选项非必须，如果不存在会使用config.yml 中的配置
      refine: 0 # 精炼等级
      limit: 0  # 突破等级
      forge: 0  # 强化等级
      max-refine: 5 # 最大精炼等级
      max-limit: 20  # 最大突破等级
      max-forge: 100  # 最大强化等级
      
      gain-refine: # 强化增益，具体看下面
        '1':
          ATTACK_DAMAGE: 1%
        '2':
          ATTACK_DAMAGE: 2%
        '3':
          ATTACK_DAMAGE: 3%
        '4':
          ATTACK_DAMAGE: 4%
        '5':
          ATTACK_DAMAGE: 5%
      gain-limit: # 突破增益，具体看下面
        '1':
          ATTACK_DAMAGE: 1%
        '2':
          ATTACK_DAMAGE: 2%
        '3':
          ATTACK_DAMAGE: 3%
        '4':
          ATTACK_DAMAGE: 4%
        '5':
          ATTACK_DAMAGE: 5%
      limit-type: # 突破需要消耗的mmoitem物品id
        '1':
        - material:STEEL_INGOT
        '2':
        - material:STEEL_INGOT
        '3':
        - material:STEEL_INGOT
        '4':
        - material:STEEL_INGOT
        '5':
        - material:STEEL_INGOT
        - material:UNCOMMON_WEAPON_ESSENCE
      gain-forge: # 强化增益，具体看下面
        '1':
          ATTACK_DAMAGE: 1%
        '2':
          ATTACK_DAMAGE: 2%
        '3':
          ATTACK_DAMAGE: 3%
        '4':
          ATTACK_DAMAGE: 4%
        '5':
          ATTACK_DAMAGE: 5%

~~~

如果你需要指定某个MMOItems物品为强化材料，提供强化经验，需要在物品配置添加forge-exp键，具体如下

~~~ yaml
STEEL_INGOT:
  base:
    material: IRON_INGOT
    name: '&fSteel Ingot'
    disable-repairing: true
    disable-crafting: true
    forge-exp: 100 # 提供100点突破经验
~~~

如果你需要指定某个MMOItems物品为突破材料，需要在物品配置添加limit-level键，具体如下

~~~ yaml
STEEL_INGOT:
  base:
    material: IRON_INGOT
    name: '&fSteel Ingot'
    disable-repairing: true
    disable-crafting: true
    limit-level: 4 #可以突破精炼等级为4的物品
~~~

### 支持强化的属性

~~~ tex
ITEM_DAMAGE
CUSTOM_MODEL_DATA
MAX_DURABILITY
BLOCK_ID
REQUIRED_POWER
MIN_XP
MAX_XP
ENCHANTS
REQUIRED_LEVEL
ATTACK_DAMAGE
ATTACK_SPEED
CRITICAL_STRIKE_CHANCE
CRITICAL_STRIKE_POWER
SKILL_CRITICAL_STRIKE_CHANCE
SKILL_CRITICAL_STRIKE_POWER
BLOCK_POWER
BLOCK_RATING
BLOCK_COOLDOWN_REDUCTION
DODGE_RATING
DODGE_COOLDOWN_REDUCTION
PARRY_RATING
PARRY_COOLDOWN_REDUCTION
COOLDOWN_REDUCTION
RANGE
MANA_COST
STAMINA_COST
ARROW_VELOCITY
PVE_DAMAGE
PVP_DAMAGE
BLUNT_POWER
BLUNT_RATING
WEAPON_DAMAGE
SKILL_DAMAGE
PROJECTILE_DAMAGE
MAGIC_DAMAGE
PHYSICAL_DAMAGE
DEFENSE
DAMAGE_REDUCTION
FALL_DAMAGE_REDUCTION
PROJECTILE_DAMAGE_REDUCTION
PHYSICAL_DAMAGE_REDUCTION
FIRE_DAMAGE_REDUCTION
MAGIC_DAMAGE_REDUCTION
PVE_DAMAGE_REDUCTION
PVP_DAMAGE_REDUCTION
UNDEAD_DAMAGE
LIFESTEAL
SPELL_VAMPIRISM
ARMOR
ARMOR_TOUGHNESS
MAX_HEALTH
MAX_MANA
KNOCKBACK_RESISTANCE
MOVEMENT_SPEED
RESTORE_HEALTH
RESTORE_FOOD
RESTORE_SATURATION
RESTORE_MANA
RESTORE_STAMINA
SOULBINDING_CHANCE
SOULBOUND_BREAK_CHANCE
SOULBOUND_LEVEL
ITEM_COOLDOWN
MAX_CONSUME
SUCCESS_RATE
PICKAXE_POWER
NOTE_WEIGHT
RANDOM_UNSOCKET
REPAIR
REPAIR_PERCENT
KNOCKBACK
RECOIL
DEATH_DOWNGRADE_CHANCE
DURABILITY
BROWSER_IDX
HEALTH_REGENERATION
MAX_HEALTH_REGENERATION
MANA_REGENERATION
MAX_MANA_REGENERATION
STAMINA_REGENERATION
MAX_STAMINA_REGENERATION
MAX_STAMINA
MAX_STELLIUM
ADDITIONAL_EXPERIENCE
REQUIRED_DEXTERITY
ADDITIONAL_DEXTERITY
REQUIRED_STRENGTH
ADDITIONAL_STRENGTH
REQUIRED_INTELLIGENCE
ADDITIONAL_INTELLIGENCE
ADDITIONAL_EXPERIENCE_ENCHANTING
PROFESSION_ENCHANTING
ADDITIONAL_EXPERIENCE_SMITHING
PROFESSION_SMITHING
ADDITIONAL_EXPERIENCE_WOODCUTTING
PROFESSION_WOODCUTTING
ADDITIONAL_EXPERIENCE_FARMING
PROFESSION_FARMING
ADDITIONAL_EXPERIENCE_FISHING
PROFESSION_FISHING
ADDITIONAL_EXPERIENCE_ALCHEMY
PROFESSION_ALCHEMY
ADDITIONAL_EXPERIENCE_MINING
PROFESSION_MINING
ADDITIONAL_EXPERIENCE_SMELTING
PROFESSION_SMELTING
FORGE_EXP
LIMIT_LEVEL
SILK_TOUCH
ORE_FORTUNE
VEIN_ORE
VEIN_LOG
SCOPE_MINER
SMELT_ORE
SMELT_SHOVEL
HARVESTER
SPEED_UP
ABILITY
~~~

以上一般都适合double类型的参数，大小写不敏感

对于数字类型的属性强化格式为

~~~ text
格式 => 示例(基础值为200)
 +5 => 205
 -5 => 195
  s5 => 5
 n5 => -5
 5% => 10
+5% => 210
-5% => 190
 n5% => -10
 [3,5] => 203 - 205 的高斯分布区间
~~~

但是有以下特殊类型

ENCHANTS: 附魔id1 等级变化,附魔id2 等级变化 ...

举例: enchants: sharpness +2,knockback +1

ABILITY: 技能类型1,触发方式1:属性1,修改1:属性2,修改2 技能类型2,触发方式2:属性1,修改1:属性2,修改2

举例: ability: FIREBOLT,RIGHT_CLICK:damage,+1|ignite,+1 ICE_CRYSTAL,RIGHT_CLICK:cooldown,-1

此处匹配MMOItems物品配置中的2个技能，如果存在相同匹配则取第一个,这是由于MMOItem对技能储存的无序匿名特性的无奈处理方式

~~~ yaml
    ability:
      ability1:
        type: FIREBOLT
        mode: RIGHT_CLICK
        damage: 15.0
        ignite: 3.0
        cooldown: 5.0
      ability2:
        type: ICE_CRYSTAL
        mode: RIGHT_CLICK
        damage: 3.0
        cooldown: 5.0
        amplifier: 2.0
        duration: 3.0
~~~

### 命令

~~~ text
主命令 mmoforge、缩写mf、mforge、mmof
玩家使用，默认具有权限
权限节点 mmoforge.mmoforge.break、mmoforge.mmoforge.forge、mmoforge.mmoforge.refine

子命令，输入 mmoforge 可查看
- break  打开突破界面
- forge  打开强化界面
- refine  打开精炼界面

~~~

### 其他属性

插件自带了一些MMOItems的属性, 具体使用方式为在MMOitems的物品配置中添加某个键并设置值即可
如果你不需要可以在 本插件的enchants配置文件夹中修改

~~~ yaml

STEEL_INGOT:
  base:
    material: IRON_INGOT
    name: '&fSteel Ingot'
    auto-torch: true # 挖掘方块时在亮度不足时自动插火把
    ore-fortune: 3 # 挖掘矿物时有概率凋落物增加, 与原版的时运类似，但是兼容连锁等属性
    harvester: 5 # 范围播种, 可一次性采集大面积农作物
    scope_miner: 3 # 范围挖掘
    silk-touch: 0.5 # 概率精准采集
    smelt-ore: 0.5 # 概率熔炼矿物
    smelt-shovel: 0.5 # 铲子挖掘东西时概率熔炼物品
    speed-up: 2 # 挖矿时获得速度药水效果加成
    vein-log: 10 # 连锁伐木
    vein-ore: 10 # 连锁挖矿
~~~

### 公式

经验、金币公式支持的操作符如下

~~~ text
+
-
*
/
^
E
u
sin()
cos()
tan()
asin()
acos()
atan()
sinh()
cosh()
tanh()
log2()
log10()
ln()
log,
sqrt()
exp()

~~~

