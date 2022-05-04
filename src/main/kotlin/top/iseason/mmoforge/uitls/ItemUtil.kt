/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/25 下午8:44
 *
 */

package top.iseason.mmoforge.uitls

import com.entiv.core.common.toColor
import com.entiv.core.utils.RandomUtils
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.data.EnchantListData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.ItemStat
import net.Indyuce.mmoitems.stat.type.StatHistory
import net.Indyuce.mmoitems.stat.type.Upgradable
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import top.iseason.mmoforge.config.MainConfig
import top.iseason.mmoforge.config.MainConfig.getUpgradeInfoByString
import top.iseason.mmoforge.stats.ForgeStat
import top.iseason.mmoforge.stats.MMOForgeData
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

fun Map<Int, Map<ItemStat, String>>.getLevelMap(level: Int): Map<ItemStat, String> {
    val attributes = mutableMapOf<ItemStat, String>()
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
        val rawData = (originalData as DoubleData).cloneData() as DoubleData
        rawData.merge(forgeData)
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
    val limit = data.limit
    for (i in limit + 1..limit + times) {
        addAttribute(MainConfig.LimitUUID, data.limitGain.getLevelMap(i), 1, data.limitGain != MainConfig.limitGain)
    }

}


/**
 * 根据数据强化物品
 * @param data 数据，主要提供突破的内容和星级
 * @param times 强化的次数
 */
fun LiveMMOItem.forge(data: MMOForgeData, times: Int) {
    val forge = data.forge
    for (i in forge + 1..forge + times) {
        addAttribute(MainConfig.ForgeUUID, data.forgeGain.getLevelMap(i), 1, data.forgeGain != MainConfig.forgeGain)
    }
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
inline fun <reified T : StatData> ItemStack.getMMOData(stat: ItemStat): T? {
    if (type.isAir) return null
    val nbt = NBTItem.get(this)
    return if (!nbt.hasType()) null else LiveMMOItem(nbt).getData(stat) as? T
}

/**
 * 从NBT获取强化数据
 */
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

fun ItemMeta.setName(name: String) {
    setDisplayName(name.toColor())
}

