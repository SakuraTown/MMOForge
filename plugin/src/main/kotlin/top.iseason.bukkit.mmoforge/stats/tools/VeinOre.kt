/*
 * Description:
 * @Author: Iseason2000
 * @Date: 2022/4/9 下午8:27
 *
 */

package top.iseason.bukkit.mmoforge.stats.tools

import net.Indyuce.mmoitems.stat.data.DoubleData
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.scheduler.BukkitTask
import top.iseason.bukkit.mmoforge.event.MMOBlockBreakEvent
import top.iseason.bukkit.mmoforge.listener.MMOListener
import top.iseason.bukkit.mmoforge.uitls.getVeinBlocks
import top.iseason.bukkit.mmoforge.uitls.isOre
import top.iseason.bukkittemplate.utils.bukkit.SchedulerUtils.submit

object VeinOre : MMOAttribute(
    "VEIN_ORE",
    Material.CHAINMAIL_CHESTPLATE,
    "Vein ore",
    "&7■ &f矿物连锁: &a{value} &f个",
    arrayOf("连锁采集矿物"),
    arrayOf("tool")
) {

    @EventHandler(ignoreCancelled = true)
    fun onMMOBlockBreakEvent(event: MMOBlockBreakEvent) {
        val player = event.player
        if (!event.block.type.isOre()) return
        val level = event.getMMOData<DoubleData>(stat)?.value ?: return
        val veinBlocksIterator = getVeinBlocks(event.block, level.toInt()).iterator()
        var task: BukkitTask? = null
        //降低负担,每tick处理10个
        task = submit(period = 1L) {
            repeat(10) {
                if (!veinBlocksIterator.hasNext()) {
                    task?.cancel()
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