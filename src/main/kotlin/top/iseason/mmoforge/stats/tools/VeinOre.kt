/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:27
 *
 */

package top.iseason.mmoforge.stats.tools

import com.entiv.core.common.submit
import com.entiv.core.utils.bukkit.isOre
import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import top.iseason.mmoforge.event.MMOBlockBreakEvent
import top.iseason.mmoforge.listener.MMOListener
import top.iseason.mmoforge.uitls.getVeinBlocks

object VeinOre : MMOAttribute(
    "VEIN_ORE",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein ore",
    "&7■ &f矿物连锁: &a# &f个",
    arrayOf("连锁采集矿物"),
    arrayOf("tool")
) {

    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        if (!event.block.type.isOre()) return
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        val veinBlocksIterator = getVeinBlocks(event.block, level.toInt()).iterator()

        //降低负担,每tick处理10个
        submit(period = 1L) {
            repeat(10) {
                if (!veinBlocksIterator.hasNext()) {

                    cancel()
                    return@submit
                }
                val block = veinBlocksIterator.next()
                MMOListener.lockEvent(event.parent, block)
                player.breakBlock(block)
                MMOListener.unLockEvent(event.parent, block)
            }
        }
    }
}