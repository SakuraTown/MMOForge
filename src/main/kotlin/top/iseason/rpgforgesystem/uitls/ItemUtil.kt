/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/25 下午8:44
 *
 */

package top.iseason.rpgforgesystem.uitls

import com.entiv.core.utils.RandomUtils
import io.lumine.mythic.lib.api.item.ItemTag
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import net.Indyuce.mmoitems.stat.type.NameData
import net.Indyuce.mmoitems.stat.type.StatHistory
import org.bukkit.inventory.ItemStack
import top.iseason.rpgforgesystem.configs.MainConfig
import top.iseason.rpgforgesystem.uitls.kparser.ExpressionParser
import java.util.*

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
            it.addAttribute(MainConfig.RefineUUID, MainConfig.refineMap.getLevelMap(rawLevel + i))
        }
    }


/**
 * 增加强化等级
 */
fun ItemStack.addForge(level: Int) =
    setRPGData(MainConfig.FORGE_TAG, getRPGData(MainConfig.FORGE_TAG) + level) {
        for (i in 1..level) {
            it.addAttribute(MainConfig.ForgeUUID, MainConfig.forgeMap)
        }
    }

/**
 * 增加强化限制等级
 */
fun ItemStack.addLimit(level: Int): ItemStack {
    val rawLevel = getRPGData(MainConfig.LIMIT_TAG)
    return setRPGData(MainConfig.LIMIT_TAG, rawLevel + level) {
        for (i in 1..level) {
            it.addAttribute(MainConfig.LimitUUID, MainConfig.forgeLimitMap.getLevelMap(rawLevel + i))
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

fun Map<Int, Map<DoubleStat, String>>.getLevelMap(level: Int): Map<DoubleStat, String> {
    val attributes = mutableMapOf<DoubleStat, String>()
    forEach { (l, dataMap) ->
        if (l > level) return@forEach
        attributes.putAll(dataMap)
    }
    return attributes
}

fun LiveMMOItem.addAttribute(uuid: UUID, attributes: Map<DoubleStat, String>) {
    attributes.forEach { (itemStat, data) ->
        val statHistory = getStatHistory(itemStat) ?: StatHistory(
            this,
            itemStat,
            this.getData(itemStat)
        )
        val doubleData = statHistory.getModifiersBonus(uuid)
        //获取
        val raw = if (doubleData != null) (doubleData as DoubleData) else DoubleData(0.0)
        if (!data.contains("%")) {
            raw.add(formatForgeString(data))
        } else {
            //百分比,基于基础值+强化值
            val percent = data.replace("%", "").toDouble() / 100.0
            val base = (statHistory.originalData as DoubleData).value
            val forge = statHistory.getModifiersBonus(MainConfig.ForgeUUID) ?: DoubleData(0.0)
            raw.add((base + (forge as DoubleData).value) * percent)
        }
        //设置
        statHistory.registerModifierBonus(uuid, raw)
        this.setStatHistory(itemStat, statHistory)
    }
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

fun formatForgeString(value: String): Double {
    val split = value.split("-")
    if (split.size == 2) {
        return try {
            RandomUtils.getGaussian(split[0].toDouble(), split[1].toDouble())
        } catch (e: Exception) {
            0.0
        }
    }
    return try {
        return value.toDouble()
    } catch (e: Exception) {
        0.0
    }
}