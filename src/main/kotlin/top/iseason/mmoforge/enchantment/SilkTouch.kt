/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午9:42
 *
 */

package top.iseason.mmoforge.enchantment

import com.entiv.core.utils.RandomUtils
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDropItemEvent
import top.iseason.mmoforge.uitls.getMMOData

object SilkTouch : MMOEnchant(
    "SILK_TOUCH",
    Material.IRON_PICKAXE,
    "Silk Touch",
    "&7■ &f精准: &a# %",
    arrayOf("挖掘方块时有概率触发精准采集效果"),
    arrayOf("tool")
) {
    @EventHandler
    fun onBlockDropItemEvent(event: BlockDropItemEvent) {
        val itemInMainHand = event.player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val data = itemInMainHand.getMMOData<DoubleData>(stat) ?: return
        if (RandomUtils.checkPercentage(data.value)) return
        if (event.items.size != 1) return
        val item = event.items[0]
        if (item.itemStack.type == event.blockState.type) return
        item.apply {
            itemStack.type = event.blockState.type
            itemStack.amount = 1
        }
    }
}