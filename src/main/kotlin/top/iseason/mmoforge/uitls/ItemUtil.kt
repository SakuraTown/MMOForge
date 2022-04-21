/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/25 下午8:44
 *
 */

package top.iseason.mmoforge.uitls

import com.entiv.core.utils.RandomUtils
import io.lumine.mythic.lib.api.item.ItemTag
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.EnchantListData
import net.Indyuce.mmoitems.stat.data.type.Mergeable
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.ItemStat
import net.Indyuce.mmoitems.stat.type.NameData
import net.Indyuce.mmoitems.stat.type.StatHistory
import net.Indyuce.mmoitems.stat.type.Upgradable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.config.MainConfig.getUpgradeInfoByString
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.stats.MMOForgeData
import top.iseason.mmoforge.uitls.kparser.ExpressionParser
import java.util.*
import java.util.regex.Pattern

val parser = ExpressionParser()


/**
 * 获取ItemStack某个RPG NBT
 */
fun ItemStack.getRPGData(tag: String): Int = NBTItem.get(this).getInteger(tag)

/**
 * 设置ItemStack 某个RPG NBT数值
 */
fun ItemStack.setRPGData(
    tag: String,
    level: Int,
    modifyNBT: (NBTItem) -> Unit = {},
    modifyMMO: (LiveMMOItem) -> Unit = {}
): ItemStack {
    val liveMMOItem = LiveMMOItem(this)
    modifyMMO(liveMMOItem)
    val nbtItem = liveMMOItem.newBuilder().buildNBT()
//    val name = liveMMOItem.getData(ItemStats.NAME).toString()
    nbtItem.addTag(ItemTag(tag, level))
    modifyNBT(nbtItem)
    return nbtItem.toItem()
}

/**
 * 修改 ItemStack 某个RPG NBT数值，正负数都可以
 */
fun ItemStack.modifyRPGData(tag: String, value: Int) = setRPGData(tag, getRPGData(tag) + value)

/**
 * 增加精炼等级
 */
fun ItemStack.addRefine(level: Int) =
    setRPGData(MainConfig.REFINE_TAG, getRPGData(MainConfig.REFINE_TAG) + level) {
        val rawLevel = getRPGData(MainConfig.REFINE_TAG)
        val statHistory =
            it.getStatHistory(ItemStats.NAME) ?: StatHistory(it, ItemStats.NAME, it.getData(ItemStats.NAME))
        val originalData = ((statHistory.originalData as NameData).cloneData() as NameData)
        originalData.addSuffix((rawLevel + level).toRoman())
        statHistory.registerModifierBonus(MainConfig.RefineUUID, originalData)
        it.setStatHistory(ItemStats.NAME, statHistory)
        for (i in 1..level) {
            it.addAttribute(MainConfig.RefineUUID, MainConfig.refineGain.getLevelMap(rawLevel + i), 1)
        }
    }


/**
 * 增加强化等级
 */
fun ItemStack.addForge(level: Int) =
    setRPGData(MainConfig.FORGE_TAG, getRPGData(MainConfig.FORGE_TAG) + level) {
        for (i in 1..level) {
            it.addAttribute(MainConfig.ForgeUUID, MainConfig.forgeGain.getLevelMap(i), 1)
        }
    }

/**
 * 增加强化限制等级
 */
fun ItemStack.addLimit(level: Int): ItemStack {
    val rawLevel = getRPGData(MainConfig.LIMIT_TAG)
    return setRPGData(MainConfig.LIMIT_TAG, rawLevel + level) {
        for (i in 1..level) {
            it.addAttribute(MainConfig.LimitUUID, MainConfig.limitGain.getLevelMap(rawLevel + i), 1)
        }
    }
}

fun NBTItem.getForgeUpdateExp(): Int {
    val forgeLevel = getInteger(MainConfig.FORGE_TAG)
    val limitLevel = getInteger(MainConfig.LIMIT_TAG)
    val refineLevel = getInteger(MainConfig.REFINE_TAG)
    val starLevel = getInteger(MainConfig.QUALITY_TAG)
    var formula = "100*{forge}"
    MainConfig.forgeLevelMap.forEach { (l, f) ->
        formula = f
        if (forgeLevel >= l) {
            return@forEach
        }
    }
    val replace = formula.replace("{forge}", forgeLevel.toString())
        .replace("{star}", starLevel.toString())
        .replace("{limit}", limitLevel.toString())
        .replace("{refine}", refineLevel.toString())
    return parser.evaluate(replace).toInt()
}

fun NBTItem.getForgeRequireExp() = getForgeUpdateExp() - getInteger(MainConfig.FORGE_EXP_TAG)

fun NBTItem.getMaxForge() = (getInteger(MainConfig.LIMIT_TAG) + 1) * MainConfig.LimitRate

fun ItemStack.addExp(value: Int): ItemStack {
    var nbtItem = NBTItem.get(this)
    if (!nbtItem.hasTag(MainConfig.QUALITY_TAG)) {
        return this
    }
//    "==================".sendConsole()
//    "before forge ${nbtItem.getInteger(Config.FORGE_TAG)}".sendConsole()
//    "before limit ${nbtItem.getInteger(Config.LIMIT_TAG)}".sendConsole()
//    "before refine ${nbtItem.getInteger(Config.REFINE_TAG)}".sendConsole()
//    "before forge_exp ${nbtItem.getInteger(Config.FORGE_EXP_TAG)}".sendConsole()
    var exp = value
    var requireExp = nbtItem.getForgeRequireExp()
    val forge = nbtItem.getInteger(MainConfig.FORGE_TAG)
    if (forge >= nbtItem.getMaxForge()) return this
//    "before forge_exp_require $requireExp".sendConsole()
    var addedLevel = 0
    val temp = NBTItem.get(this)
    while (exp > requireExp) {
//        item = item.addForge(1)
//        nbtItem = NBTItem.get(item)
//        forge = nbtItem.getInteger(Config.FORGE_TAG)
        if (forge + addedLevel >= nbtItem.getMaxForge()) break
        temp.addTag(ItemTag(MainConfig.FORGE_TAG, forge + addedLevel))
        temp.addTag(ItemTag(MainConfig.FORGE_EXP_TAG, 0))
        exp -= requireExp
        requireExp = temp.getForgeRequireExp()
//        "ing forge_exp_require $requireExp".sendConsole()
        addedLevel++
    }
    if (addedLevel > 0) {
        nbtItem.addTag(ItemTag(MainConfig.FORGE_EXP_TAG, 0))
        nbtItem = NBTItem.get(nbtItem.toItem().addForge(addedLevel))
    }
    val remain =
        if (exp >= nbtItem.getForgeRequireExp()) nbtItem.getForgeUpdateExp()
        else nbtItem.getInteger(MainConfig.FORGE_EXP_TAG) + exp
    nbtItem.addTag(ItemTag(MainConfig.FORGE_EXP_TAG, remain))
//    "after forge ${nbtItem.getInteger(Config.FORGE_TAG)}".sendConsole()
//    "after limit ${nbtItem.getInteger(Config.LIMIT_TAG)}".sendConsole()
//    "after refine ${nbtItem.getInteger(Config.REFINE_TAG)}".sendConsole()
//    "after forge_exp ${nbtItem.getInteger(Config.FORGE_EXP_TAG)}".sendConsole()
//    "after forge_exp_require ${nbtItem.getForgeRequireExp()}".sendConsole()
    return nbtItem.toItem()
}

fun Map<Int, Map<ItemStat, String>>.getLevelMap(level: Int): Map<ItemStat, String> {
    val attributes = mutableMapOf<ItemStat, String>()
    forEach { (l, dataMap) ->
        if (l > level) return@forEach
        attributes.putAll(dataMap)
    }
    return attributes
}

/**
 * 方便地转换map
 */
inline fun <T, U, reified C> Map<T, U>.toTypeMap(): Map<C, U> {
    val mutableMapOf = mutableMapOf<C, U>()
    forEach { (k, v) ->
        mutableMapOf[k as C] = v
    }
    return mutableMapOf
}

/**
 * 根据内容更新属性
 * @param uuid 历史标签，用于区别其他途径增加的属性
 * @param attributes 属性更新公式
 * @param times 更新次数
 * @param isAppend 是否添加不存在的属性
 *
 */
fun LiveMMOItem.addAttribute(uuid: UUID, attributes: Map<ItemStat, String>, times: Int, isAppend: Boolean = false) {
    attributes.forEach { (itemStat, upgradeInfo) ->
        //是否追加
        if (!isAppend && !this.hasData(itemStat)) return
        val statHistory = StatHistory.from(this, itemStat)
        val originalData = statHistory.originalData
        val forgeData = statHistory.getModifiersBonus(MainConfig.ForgeUUID) ?: itemStat.clearStatData
        val info = (itemStat as Upgradable).getUpgradeInfoByString(upgradeInfo)
        if (originalData is EnchantListData) {
            val enchantListData = originalData.cloneData() as EnchantListData
            enchantListData.merge(forgeData)
            val enchants: Set<Enchantment> = enchantListData.enchants
            val apply = (itemStat as Upgradable).apply(enchantListData, info, times) as EnchantListData
            if (!isAppend) {
                val enchants1 = apply.enchants
                val temp = enchants1.filter { !enchants.contains(it) }
                temp.forEach {
                    apply.addEnchant(it, 0)
                }
            }
            statHistory.registerModifierBonus(uuid, apply)
            this.setStatHistory(itemStat, statHistory)
//            setData(itemStat, apply)
            return
        }
        val rawData = (originalData as Mergeable).cloneData() as Mergeable
        rawData.merge(forgeData)
        val raw = (itemStat as Upgradable).apply(rawData, info, times)
        statHistory.registerModifierBonus(uuid, raw)
        this.setStatHistory(itemStat, statHistory)
    }
}

/**
 * 根据数据精炼物品
 * @param data 数据，主要提供精炼的内容和星级
 * @param times 精炼的次数
 */
fun LiveMMOItem.refine(data: MMOForgeData, times: Int) {
    val refineGain = data.refineGain[data.star] ?: MainConfig.refineGain[data.star] ?: emptyMap()
    addAttribute(
        MainConfig.RefineUUID,
        refineGain,
        times,
        data.refineGain != MainConfig.refineGain
    )

}

/**
 * 根据数据突破物品
 * @param data 数据，主要提供突破的内容和星级
 * @param times 突破的次数
 */
fun LiveMMOItem.breach(data: MMOForgeData, times: Int) {
    val limitGain = data.limitGain[data.star] ?: MainConfig.limitGain[data.star] ?: emptyMap()
    addAttribute(
        MainConfig.LimitUUID,
        limitGain,
        times,
        data.limitGain != MainConfig.limitGain
    )

}


/**
 * 根据数据强化物品
 * @param data 数据，主要提供突破的内容和星级
 * @param times 强化的次数
 */
fun LiveMMOItem.forge(data: MMOForgeData, times: Int) {
    val forgeGain = data.forgeGain[data.star] ?: MainConfig.forgeGain[data.star] ?: emptyMap()
    addAttribute(
        MainConfig.ForgeUUID,
        forgeGain,
        times,
        data.forgeGain != MainConfig.forgeGain
    )

}


/**
 * 整数转罗马数字
 */
fun Int.toRoman(): String {
    val num = if (this < 0) return "" else this
    val m = arrayOf("", "M", "MM", "MMM")
    val c = arrayOf("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM")
    val x = arrayOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
    val i = arrayOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")
//    val sign = if (num > 0) "+" else if (num < 0) "-" else ""
    return m[num / 1000] + c[num % 1000 / 100] + x[num % 100 / 10] + i[num % 10]
}

fun formatForgeString(value: String): Double? {
    val matcher = Pattern.compile("\\[(.+),(.+)]").matcher(value)
    if (!matcher.find()) return null
    return try {
        val fist = matcher.group(1).toDouble()
        val second = matcher.group(2).toDouble()
        RandomUtils.getGaussian(fist, second)
    } catch (_: NumberFormatException) {
        null
    }

}

inline fun <reified T : StatData> ItemStack.getMMOData(stat: ItemStat): T? {
    if (type.isAir) return null
    val nbt = NBTItem.get(this)
    return if (!nbt.hasType()) null else LiveMMOItem(nbt).getData(stat) as? T
}

/**
 * 判断是否是工具
 */
fun Material.isTool() = when {
    isPickaxe() -> true
    isAxe() -> true
    isShovel() -> true
    isHoe() -> true
    isOtherTool() -> true
    else -> false
}

/**
 * 是否是镐子
 */
fun Material.isPickaxe() = when (this) {
    Material.WOODEN_PICKAXE,
    Material.STONE_PICKAXE,
    Material.IRON_PICKAXE,
    Material.GOLDEN_PICKAXE,
    Material.DIAMOND_PICKAXE,
    Material.NETHERITE_PICKAXE -> true
    else -> false
}

/**
 * 是否是斧子
 */
fun Material.isAxe() = when (this) {
    Material.WOODEN_AXE,
    Material.STONE_AXE,
    Material.IRON_AXE,
    Material.GOLDEN_AXE,
    Material.DIAMOND_AXE,
    Material.NETHERITE_AXE -> true
    else -> false
}

/**
 * 是否是铲子
 */
fun Material.isShovel() = when (this) {
    Material.WOODEN_SHOVEL,
    Material.STONE_SHOVEL,
    Material.IRON_SHOVEL,
    Material.GOLDEN_SHOVEL,
    Material.DIAMOND_SHOVEL,
    Material.NETHERITE_SHOVEL -> true
    else -> false
}

/**
 * 是否是锄头
 */
fun Material.isHoe() = when (this) {
    Material.WOODEN_HOE,
    Material.STONE_HOE,
    Material.IRON_HOE,
    Material.GOLDEN_HOE,
    Material.DIAMOND_HOE,
    Material.NETHERITE_HOE -> true
    else -> false
}

/**
 * 是否是除了镐子、斧子、铲子、锄头之外的工具
 */
fun Material.isOtherTool() = when (this) {
    Material.FISHING_ROD,
    Material.FLINT_AND_STEEL,
    Material.COMPASS,
    Material.CLOCK,
    Material.SHEARS,
    Material.SPYGLASS,
    Material.LEAD,
    Material.NAME_TAG -> true
    else -> false
}

fun Material.isOre() = when (this) {
    Material.COAL_ORE,
    Material.COPPER_ORE,
    Material.IRON_ORE,
    Material.GOLD_ORE,
    Material.REDSTONE_ORE,
    Material.LAPIS_ORE,
    Material.DIAMOND_ORE,
    Material.EMERALD_ORE,
    Material.DEEPSLATE_COAL_ORE,
    Material.DEEPSLATE_COPPER_ORE,
    Material.DEEPSLATE_IRON_ORE,
    Material.DEEPSLATE_GOLD_ORE,
    Material.DEEPSLATE_REDSTONE_ORE,
    Material.DEEPSLATE_LAPIS_ORE,
    Material.DEEPSLATE_DIAMOND_ORE,
    Material.DEEPSLATE_EMERALD_ORE,
    Material.NETHER_GOLD_ORE,
    Material.NETHER_QUARTZ_ORE,
    Material.ANCIENT_DEBRIS,
    Material.AMETHYST_CLUSTER -> true
    else -> false
}

fun NBTItem.getForgeData(): MMOForgeData? {
    if (!hasType()) return null
    if (!hasTag(ForgeStat.nbtPath)) return null
    val string = getString(ForgeStat.nbtPath)!!
    return try {
        MMOForgeData.fromString(string)
    } catch (_: Exception) {
        null
    }
}