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
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
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
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return
        val itemInMainHand = event.player.equipment.itemInMainHand
        if (itemInMainHand.type.isAir) return
        val level = itemInMainHand.getMMOData<DoubleData>(stat)?.value ?: return
        if (RandomUtils.checkPercentage(level)) return
        event.block.breakNaturally(itemInMainHand.clone().apply {
            addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
        })
    }
}