/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/4 下午9:42
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.utils.RandomUtils
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import top.iseason.mmoforge.event.MMOBlockBreakEvent
import top.iseason.mmoforge.uitls.dropBlock

object SilkTouch : MMOAttribute(
    "SILK_TOUCH",
    Material.IRON_PICKAXE,
    "Silk Touch",
    "&7■ &f精准: &a# &f%",
    arrayOf("挖掘方块时有概率触发精准采集效果"),
    arrayOf("tool")
) {
    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        val itemInMainHand = event.handItem
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        if (RandomUtils.checkPercentage(level)) return
        if (event.player.gameMode == GameMode.CREATIVE) return
        player.dropBlock(event.block, itemInMainHand.clone().apply {
            addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
        })
        event.isDropItems = false
    }
}