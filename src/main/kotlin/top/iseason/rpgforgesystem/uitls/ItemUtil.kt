/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/1/25 下午8:44
 *
 */

package top.iseason.rpgforgesystem.uitls

import com.entiv.core.common.toColor
import com.entiv.core.utils.RandomUtils
import io.lumine.mythic.lib.api.item.ItemTag
import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.ItemStats
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.type.NameData
import net.Indyuce.mmoitems.stat.type.StatHistory
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import top.iseason.rpgforgesystem.Config
import java.util.regex.Pattern

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
    modifyMMO: (LiveMMOItem) -> Unit = {},
    modifyNBT: (NBTItem) -> Unit = {}
): ItemStack {
    val liveMMOItem = LiveMMOItem(this)
    modifyMMO(liveMMOItem)
    val name = liveMMOItem.getData(ItemStats.NAME).toString()
    val nbtItem = liveMMOItem.newBuilder().buildNBT()
    nbtItem.addTag(ItemTag(tag, level))
    modifyNBT(nbtItem)
    return nbtItem.toItem().apply { itemMeta = itemMeta.apply { displayName(Component.text(name.toColor())) } }
}

/**
 * 修改 ItemStack 某个RPG NBT数值，正负数都可以
 */
fun ItemStack.modifyRPGData(tag: String, value: Int) = setRPGData(tag, getRPGData(tag) + value)

val romanPattern: Pattern = Pattern.compile("^M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})\$")

/**
 * 设置精炼等级
 */
fun ItemStack.setRefine(level: Int) =
    setRPGData(Config.REFINE_TAG, level, modifyMMO = { it ->
        val statHistory =
            it.getStatHistory(ItemStats.NAME) ?: StatHistory(it, ItemStats.NAME, it.getData(ItemStats.NAME))
        val originalData = ((statHistory.originalData as NameData).cloneData() as NameData)
        originalData.addSuffix(level.toRoman())
        statHistory.registerModifierBonus(Config.RefineUUID, originalData)
        it.setStatHistory(ItemStats.NAME, statHistory)
    })

/**
 * 修改精炼等级,正负数都可以
 */
fun ItemStack.modifyRefine(value: Int) = setRefine(getRPGData(Config.REFINE_TAG) + value)

/**
 * 设置强化等级
 */
fun ItemStack.setForge(level: Int) = setRPGData(Config.FORGE_TAG, level, modifyMMO = {
    Config.forgeMap.forEach { (itemStat, value) ->
        val statHistory = it.getStatHistory(itemStat) ?: StatHistory(
            it,
            itemStat,
            it.getData(itemStat)
        )
        val doubleData = statHistory.getModifiersBonus(Config.ForgeUUID)
        //获取
        val raw = if (doubleData != null) (doubleData as DoubleData) else DoubleData(0.0)
        raw.add(formatForgeString(value))
        //设置
        statHistory.registerModifierBonus(Config.ForgeUUID, raw)
        it.setStatHistory(itemStat, statHistory)
    }
})

/**
 * 修改强化等级,正负数都可以
 */
fun ItemStack.modifyForge(value: Int) = setForge(getRPGData(Config.FORGE_TAG) + value)

/**
 * 设置强化限制等级
 */
fun ItemStack.setLimit(level: Int) = setRPGData(Config.LIMIT_TAG, level)

/**
 * 修改强化限制等级,正负数都可以
 */
fun ItemStack.modifyLimit(value: Int) = setLimit(getRPGData(Config.LIMIT_TAG) + value)


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