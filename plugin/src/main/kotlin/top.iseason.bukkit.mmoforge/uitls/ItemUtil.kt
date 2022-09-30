/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/25 下午8:44
 *
 */

package top.iseason.bukkit.mmoforge.uitls

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.data.EnchantListData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.ItemStat
import net.Indyuce.mmoitems.stat.type.StatHistory
import net.Indyuce.mmoitems.stat.type.Upgradable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import top.iseason.bukkit.mmoforge.config.MainConfig
import top.iseason.bukkit.mmoforge.config.MainConfig.getUpgradeInfoByString
import top.iseason.bukkit.mmoforge.stats.MMOForgeData
import top.iseason.bukkit.mmoforge.stats.MMOForgeStat
import top.iseason.bukkittemplate.utils.bukkit.MessageUtils.toColor
import top.iseason.bukkittemplate.utils.other.RandomUtils
import java.util.*
import java.util.regex.Pattern

/**
 * 获取MMOItem的Double数据
 */
fun ItemStack.getMMODouble(tag: String): Double {
    val nbt = NBTItem.get(this) ?: return 0.0
    if (!nbt.hasType()) return 0.0
    return nbt.getDouble(tag)
}

fun Map<Int, Map<ItemStat<*, *>, String>>.getLevelMap(level: Int): Map<ItemStat<*, *>, String> {
    val attributes = mutableMapOf<ItemStat<*, *>, String>()
    forEach { (l, dataMap) ->
        if (l > level) return@forEach
        attributes.putAll(dataMap)
    }
    return attributes
}

/**
 * 根据内容更新属性
 * @param uuid 历史标签，用于区别其他途径增加的属性
 * @param attributes 属性更新公式
 * @param times 更新次数
 * @param isAppend 是否添加不存在的属性
 *
 */
fun LiveMMOItem.addAttribute(
    uuid: UUID,
    attributes: Map<ItemStat<*, *>, String>,
    times: Int,
    isAppend: Boolean = false
) {
    attributes.forEach { (itemStat, upgradeInfo) ->
        //是否追加
        if (!isAppend && !this.hasData(itemStat)) return@forEach
        val statHistory = StatHistory.from(this, itemStat)
        val originalData = statHistory.originalData
        val info = (itemStat as Upgradable).getUpgradeInfoByString(upgradeInfo)
        if (originalData is EnchantListData) {
            val enchantListData = originalData.cloneData() as EnchantListData
            val enchants: Set<Enchantment> = enchantListData.enchants
            val apply = (itemStat as Upgradable).apply(enchantListData.cloneData(), info, times) as EnchantListData
            if (!isAppend) {
                val enchants1 = apply.enchants
                val temp = enchants1.filter { !enchants.contains(it) }
                temp.forEach {
                    apply.addEnchant(it, 0)
                }
            }
            //附魔没有历史
//            statHistory.registerModifierBonus(uuid, apply)
//            this.setStatHistory(itemStat, statHistory)
            setData(itemStat, apply)
            return
        }
        val rawData = (originalData as DoubleData).cloneData()
        val forgeData =
            statHistory.getModifiersBonus(MainConfig.ForgeUUID) as? DoubleData ?: itemStat.clearStatData as DoubleData
        rawData.merge(forgeData)
        if (uuid != MainConfig.ForgeUUID) {
            val sd = statHistory.getModifiersBonus(uuid) as? DoubleData
            if (sd != null)
                rawData.merge(sd)
        }
        val raw = (itemStat as DoubleStat).apply(rawData, info, times) as DoubleData
        raw.value -= originalData.value
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
    val refine = data.refine
    for (i in refine + 1..refine + times) {
        addAttribute(MainConfig.RefineUUID, data.refineGain.getLevelMap(i), 1, data.refineGain != MainConfig.refineGain)
    }
}

/**
 * 根据数据突破物品
 * @param data 数据，主要提供突破的内容和星级
 * @param times 突破的次数
 */
fun LiveMMOItem.breakthrough(data: MMOForgeData, times: Int) {
    addAttribute(
        MainConfig.LimitUUID,
        data.limitGain.getLevelMap(data.star),
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
    addAttribute(
        MainConfig.ForgeUUID,
        data.forgeGain.getLevelMap(data.star),
        times,
        data.forgeGain != MainConfig.forgeGain
    )
}

/**
 * 由字符数字区间获取高斯分布的随机值
 * 比如 [1,5] 获取以3为对对称轴的高斯分布随机值
 */
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

/**
 * 从物品获取MMO数据
 */
inline fun <reified T : StatData> ItemStack.getMMOData(stat: ItemStat<*, *>): T? {
    if (type.isAir) return null
    val nbt = NBTItem.get(this)
    return if (!nbt.hasType()) null else LiveMMOItem(nbt).getData(stat) as? T
}

/**
 * 从NBT获取强化数据
 */
fun NBTItem.getForgeData(): MMOForgeData? {
    if (!hasType()) return null
    if (!hasTag(MMOForgeStat.nbtPath)) return null
    val string = getString(MMOForgeStat.nbtPath)!!
    return try {
        MMOForgeData.fromString(string)
    } catch (_: Exception) {
        null
    }
}

fun ItemMeta.setName(name: String) {
    setDisplayName(name.toColor())
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