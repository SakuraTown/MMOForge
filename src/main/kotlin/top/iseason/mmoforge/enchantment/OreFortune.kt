/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/7 下午4:57
 *
 */

package top.iseason.mmoforge.enchantment

import com.entiv.core.utils.RandomUtils
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDropItemEvent
import top.iseason.mmoforge.uitls.getMMOData
import top.iseason.mmoforge.uitls.isOre

object OreFortune : MMOEnchant(
    "ORE_FORTUNE",
    Material.DIAMOND_PICKAXE,
    "Ore Fortune",
    "&7■ &f矿物时运 &a#",
    arrayOf("挖掘矿物时有概率凋落物增加"),
    arrayOf("tool")
) {
    @EventHandler
    fun onBlockDropItemEvent(event: BlockDropItemEvent) {
        if (event.isCancelled) return
        val type = event.blockState.type
        if (!type.isOre()) return
        val items = event.items
        if (items.size != 1) return
        val item = items[0]
        if (type == item.itemStack.type) return
        val itemInMainHand = event.player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val data = itemInMainHand.getMMOData<DoubleData>(stat) ?: return
        items[0].itemStack.apply { amount *= RandomUtils.calculateFortune(data.value.toInt()) }
    }
}
