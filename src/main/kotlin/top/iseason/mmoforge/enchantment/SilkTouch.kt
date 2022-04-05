/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午9:42
 *
 */

package top.iseason.mmoforge.enchantment

import io.lumine.mythic.lib.api.item.NBTItem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent

object SilkTouch : MMOEnchant(
    "SILK_TOUCH",
    Material.IRON_PICKAXE,
    "Silk Touch",
    "&7■ &f精准: &a# %",
    arrayOf("挖掘方块时有概率触发精准采集效果"),
    arrayOf("tool")
) {
    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        val itemInMainHand = event.player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val data = NBTItem.get(itemInMainHand).getDouble(nbtKey)
        if (data == 0.0) return
        println(data)
    }
}