/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午11:20
 *
 */

package top.iseason.mmoforge.enchantment

import io.lumine.mythic.lib.api.item.ItemTag
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder
import net.Indyuce.mmoitems.stat.data.DoubleData
import net.Indyuce.mmoitems.stat.data.type.StatData
import net.Indyuce.mmoitems.stat.type.DoubleStat
import org.bukkit.Material
import org.bukkit.event.Listener

abstract class EnchantmentStat(
    id: String?,
    material: Material?,
    name: String?,
    lore: Array<out String>?,
    types: Array<out String>?
) : DoubleStat(
    id, material, name,
    lore, types
), Listener {
    abstract val nbtKey: String
    abstract val loreKey: String
    abstract val loreFormat: String
    override fun whenApplied(builder: ItemStackBuilder, statData: StatData) {
        val var3 = (statData as DoubleData).value.toInt()
        builder.addItemTag(ItemTag(nbtKey, var3))
        builder.lore.insert(loreKey, formatNumericStat(var3.toDouble(), "#", "" + var3))
    }

}